"""Run with: python -m unittest discover -s tools -p 'test_*.py'."""
import json
from pathlib import Path
import re
import unittest
import zipfile

from expedition_catalog import (build_catalog, combined_drops, normalized_drops, reward_edges,
                                combine_edges, expected_yield, validate_expeditions)
from generate_expeditions import (build_java_defaults_block, build_java_dungeon_defaults_block,
                                  load_mob_assets)
from expedition_catalog import parse_rewards

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / 'mods/oneblock/src/main/resources'


class ExpeditionCatalogTests(unittest.TestCase):
    def test_duplicate_weights_are_combined_once_in_order(self):
        cfg = {'BaseDropPool': [{'ID': 'A', 'Weight': 2}, {'CustomID': 'B', 'Weight': 4}, {'ID': 'A', 'Weight': 4}]}
        self.assertEqual([('A', 1, 60), ('B', 1, 40)], normalized_drops(cfg))
        self.assertEqual(combined_drops(cfg), combined_drops({'BaseDropPool': combined_drops(cfg)}))

    def test_duplicate_quantities_must_agree(self):
        with self.assertRaisesRegex(ValueError, 'Conflicting quantities'):
            normalized_drops({'BaseDropPool': [{'ID': 'A', 'Quantity': 1}, {'ID': 'A', 'Quantity': 2}]})
        self.assertEqual([('A', 3, 100)], normalized_drops({'BaseDropPool': [{'ID': 'A', 'Quantity': 3}, {'ID': 'A', 'Quantity': 3}]}))

    def test_invalid_weights_and_quantities_are_rejected(self):
        for value in [0, -1, 1.5, float('nan'), True, 2147483648]:
            with self.assertRaises(ValueError):
                normalized_drops({'BaseDropPool': [{'ID': 'A', 'Weight': value}]})
        for value in [0, -1, 1.5, True]:
            with self.assertRaises(ValueError):
                normalized_drops({'BaseDropPool': [{'ID': 'A', 'Quantity': value}]})

    def test_pool_weight_sum_cannot_overflow_runtime_integer(self):
        with self.assertRaises(ValueError):
            normalized_drops({'BaseDropPool': [{'ID': 'A', 'Weight': 2147483647}, {'ID': 'B'}]})

    def test_expected_quantity_is_one_roll_per_block(self):
        self.assertAlmostEqual(7.5, expected_yield(10, 3, 25))

    def test_random_unlocks_count_bundles_not_crystal_copies(self):
        cfg = {'CompletionRewards': {'Random': [
            {'Weight': 1, 'Items': [{'Crystal': 'A'}, {'Crystal': 'A'}]},
            {'Weight': 2, 'Items': [{'Crystal': 'B'}]},
            {'Weight': 1, 'Items': [{'Crystal': 'A'}]}]}}
        self.assertEqual([('A', 50, 'Random unlock pool'), ('B', 50, 'Random unlock pool')], combine_edges(reward_edges(cfg)))

    def test_unknown_unlock_targets_are_rejected(self):
        with self.assertRaisesRegex(ValueError, 'Unknown unlock'):
            validate_expeditions({'A': {'Category': 'Dungeon', 'CompletionRewards': [{'Crystal': 'Missing'}]}})

    def test_generated_catalogue_and_runtime_are_current(self):
        raw = json.loads((ROOT / 'expeditions.json').read_text(encoding='utf-8-sig'))
        expeditions = validate_expeditions(raw)
        actual = json.loads((RESOURCES / 'ExpeditionAtlas.json').read_text(encoding='utf-8'))
        self.assertEqual(build_catalog(raw, load_mob_assets(ROOT)), actual, 'Regenerate expedition assets')
        normal, dungeons = [], []
        for identifier, cfg in expeditions.items():
            mandatory, bundles = parse_rewards(cfg)
            if cfg.get('Category') == 'Dungeon':
                dungeons.append((identifier, cfg['Waves'], mandatory, bundles))
            else:
                normal.append((identifier, cfg['Ticks'], cfg['BaseDropPool'], mandatory, bundles))
        java = ROOT / 'mods/oneblock/src/main/java/com/EreliaStudio/OneBlock'
        self.assertIn(build_java_defaults_block(normal), (java / 'OneBlockExpeditionDefaults.java').read_text(encoding='utf-8'))
        self.assertIn(build_java_dungeon_defaults_block(dungeons), (java / 'OneBlockDungeonDefaults.java').read_text(encoding='utf-8'))

    def test_existing_crystal_and_creature_assets_cover_catalogue(self):
        catalogue = json.loads((RESOURCES / 'ExpeditionAtlas.json').read_text())
        for e in catalogue['expeditions']:
            path = RESOURCES / 'Server/Item/Items/Crystal/Expedition' / (e['crystal'] + '.json')
            crystal = json.loads(path.read_text())
            self.assertTrue((RESOURCES / 'Common' / crystal['Icon']).is_file(), e['id'])
            for edge in e['children']:
                self.assertTrue((RESOURCES / 'Common/Icons/ItemsGenerated/Crystals' / ('OneBlock_Crystal_' + edge['target'] + '.png')).is_file())
        assets = ROOT / 'hytale-server/Assets.zip'
        if assets.exists():
            with zipfile.ZipFile(assets) as archive:
                paths = set(archive.namelist())
                roles = {Path(p).stem for p in paths if p.startswith('Server/NPC/Roles/') and p.endswith('.json')}
                for identifier, mob in catalogue['mobs'].items():
                    self.assertIn(identifier, roles)
                    self.assertIn('Common/' + mob['icon'], paths)

    def test_ui_templates_use_real_slices_and_localized_content(self):
        ui = RESOURCES / 'Common/UI/Custom'
        styles = (ui / 'OneBlockAtlasStyles.ui').read_text()
        self.assertIn('PatchStyle(TexturePath: "OneBlockAtlasPanel9Slice.png", Border: 24)', styles)
        content = '\n'.join(p.read_text(encoding='utf-8') for p in ui.glob('OneBlockAtlas*.ui'))
        self.assertNotIn('expedition-atlas-ui-proposition', content)
        self.assertNotIn('OneBlockHudFrameSlices/', content)
        keys = set(re.findall(r'%server\.(atlas\.[\w.]+)', content))
        language_keys = []
        for language in ['en-US', 'fr-FR', 'es-ES', 'sk-SK']:
            lang = (RESOURCES / f'Server/Languages/{language}/server.lang').read_text(encoding='utf-8')
            entries = re.findall(r'^(atlas\.[\w.]+)=', lang, re.MULTILINE)
            self.assertEqual(len(entries), len(set(entries)), language)
            self.assertTrue(keys <= set(entries), language)
            language_keys.append(set(entries))
        self.assertTrue(all(keys == language_keys[0] for keys in language_keys))


if __name__ == '__main__':
    unittest.main()
