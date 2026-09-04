import tempfile
import unittest
from pathlib import Path

from compile_achievements import update_language_files


class LanguageGenerationTest(unittest.TestCase):
    def test_regenerates_english_and_only_prunes_stale_localized_entries(self):
        with tempfile.TemporaryDirectory() as directory:
            languages = Path(directory)
            english = languages / "en-US/server.lang"
            french = languages / "fr-FR/server.lang"
            english.parent.mkdir()
            french.parent.mkdir()
            english.write_text(
                "items.example=Example\n\n"
                "achievement.definition.old.name=Old\n"
                "achievement.definition.old.title=Old\n\n"
                "achievement.ui.heading=Achievements\n",
                encoding="utf-8",
            )
            french.write_text(
                "items.example=Exemple\n"
                "achievement.definition.current.name=Nom traduit\n"
                "achievement.definition.current.title=Titre traduit\n"
                "achievement.definition.old.name=Ancien\n"
                "achievement.definition.old.title=Ancien\n",
                encoding="utf-8",
            )
            achievements = [{"id": "current", "name": "Current Name", "title": "Current Title"}]

            removed = update_language_files(languages, achievements)
            first_result = english.read_bytes()
            update_language_files(languages, achievements)

            self.assertEqual(first_result, english.read_bytes())
            self.assertIn("achievement.definition.current.name=Current Name", english.read_text(encoding="utf-8"))
            self.assertNotIn("achievement.definition.old.", english.read_text(encoding="utf-8"))
            self.assertIn("achievement.definition.current.name=Nom traduit", french.read_text(encoding="utf-8"))
            self.assertNotIn("achievement.definition.old.", french.read_text(encoding="utf-8"))
            self.assertEqual(2, removed["fr-FR"])


if __name__ == "__main__":
    unittest.main()
