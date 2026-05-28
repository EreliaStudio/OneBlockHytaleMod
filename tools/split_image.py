from pathlib import Path
import argparse

from PIL import Image


def crop_to_divisible_by_4(p_image: Image.Image) -> Image.Image:
    width, height = p_image.size

    new_width = width - (width % 4)
    new_height = height - (height % 4)

    if new_width == width and new_height == height:
        return p_image

    left = (width - new_width) // 2
    upper = (height - new_height) // 2
    right = left + new_width
    lower = upper + new_height

    print(f"Cropping image from {width}x{height} to {new_width}x{new_height}")

    return p_image.crop((left, upper, right, lower))


def split_image_into_16(p_image_path: Path) -> None:
    if not p_image_path.exists():
        raise FileNotFoundError(f"Image not found: {p_image_path}")

    with Image.open(p_image_path) as image:
        image = crop_to_divisible_by_4(image)

        width, height = image.size

        tile_width = width // 4
        tile_height = height // 4

        output_directory = p_image_path.parent
        base_name = p_image_path.stem
        extension = p_image_path.suffix

        index = 0

        for row in range(4):
            for column in range(4):
                left = column * tile_width
                upper = row * tile_height
                right = left + tile_width
                lower = upper + tile_height

                tile = image.crop((left, upper, right, lower))

                output_path = output_directory / f"{base_name}({index}){extension}"
                tile.save(output_path)

                print(f"Created: {output_path}")

                index += 1


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Split an image into 16 sub-images arranged as a 4x4 grid."
    )

    parser.add_argument(
        "image_path",
        type=Path,
        help="Path to the image to split."
    )

    args = parser.parse_args()

    split_image_into_16(args.image_path)


if __name__ == "__main__":
    main()