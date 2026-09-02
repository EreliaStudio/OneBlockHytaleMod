# Wiki structure

- `Home.md`: expedition index
- `LootList.md`: reverse index of item loot and its sources
- `expeditions/<ExpeditionId>.md`: one page per expedition

All expedition-to-expedition links are relative Markdown links, so the documentation works directly in a normal GitHub repository.

The pages are generated from `expeditions.json`. From the repository root, run:

```bash
python tools/generate_wiki.py expeditions.json --output wiki --clean
```

Use `--check` in automation to verify that the committed wiki is up to date without modifying it.
