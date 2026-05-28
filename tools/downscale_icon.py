"""Downscale one or more icon images to 64x64."""

import argparse
from pathlib import Path

from PIL import Image


def downscale(path: Path) -> None:
    img = Image.open(path)
    if img.size == (64, 64):
        print(f"  skip  {path.name} (already 64x64)")
        return
    img = img.resize((64, 64), Image.Resampling.LANCZOS)
    img.save(path)
    print(f"  done  {path.name}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("images", nargs="+", type=Path, help="PNG file(s) or folder(s) to downscale")
    args = parser.parse_args()

    paths: list[Path] = []
    for entry in args.images:
        if entry.is_dir():
            paths.extend(sorted(entry.glob("*.png")))
        else:
            paths.append(entry)

    for path in paths:
        downscale(path)


if __name__ == "__main__":
    main()
