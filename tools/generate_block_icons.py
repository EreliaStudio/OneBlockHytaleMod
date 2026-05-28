"""Render OneBlock block icons as isometric 3D cubes from their block textures."""

from __future__ import annotations

import argparse
from pathlib import Path

import numpy as np
from PIL import Image


SIZE = 64
REPO_ROOT = Path(__file__).resolve().parent.parent

BLOCK_TEXTURE_DIR = (
    REPO_ROOT / "mods" / "oneblock" / "src" / "main" / "resources"
    / "Common" / "BlockTextures"
)
BLOCK_ICON_DIR = (
    REPO_ROOT / "mods" / "oneblock" / "src" / "main" / "resources"
    / "Common" / "Icons" / "ItemsGenerated" / "OneBlock"
)

# Isometric cube geometry parameters (all in 64px space)
# SIDE_H ≈ sqrt(FACE_W² + FACE_H²) keeps side faces near 1:1 aspect so the
# texture does not appear vertically squished.
FACE_W = 26   # half-width of top face diamond
FACE_H = 13   # half-height of top face diamond (2:1 iso)
SIDE_H = 29   # height of the vertical side faces  (≈ sqrt(26²+13²) ≈ 29)
CX = 32       # horizontal center
TOP_Y = 5     # y of the topmost vertex

# Precomputed brightness multipliers for the three faces
BRIGHTNESS_TOP = 1.15
BRIGHTNESS_LEFT = 0.85
BRIGHTNESS_RIGHT = 0.65


def _quad_sample(
    texture: np.ndarray,
    dst: np.ndarray,
    quad: tuple[tuple[int, int], tuple[int, int], tuple[int, int], tuple[int, int]],
    brightness: float,
) -> None:
    """Paint a quadrilateral face onto dst by sampling texture with bilinear interpolation.

    quad: (top-left, top-right, bottom-right, bottom-left) in dst pixel space.
    texture: HxWx4 RGBA array (will be sampled as a unit square).
    dst: SIZExSIZEx4 RGBA array, modified in-place.
    """
    (x0, y0), (x1, y1), (x2, y2), (x3, y3) = quad
    th, tw = texture.shape[:2]

    # Bounding box of the quad
    min_x = max(0, min(x0, x1, x2, x3))
    max_x = min(SIZE - 1, max(x0, x1, x2, x3))
    min_y = max(0, min(y0, y1, y2, y3))
    max_y = min(SIZE - 1, max(y0, y1, y2, y3))

    # Build pixel coordinate grids over the bounding box
    ys, xs = np.mgrid[min_y : max_y + 1, min_x : max_x + 1]
    xs = xs.astype(np.float64)
    ys = ys.astype(np.float64)

    # Bilinear interpolation to find (u, v) in [0,1]² for each output pixel.
    # The quad corners map to: TL→(0,0), TR→(1,0), BR→(1,1), BL→(0,1)
    # We solve the bilinear system: P = (1-s)(1-t)*TL + s(1-t)*TR + s*t*BR + (1-s)*t*BL
    # for s (u-axis) and t (v-axis).
    # This reduces to a quadratic in t; use the standard formula.

    ax = x0 - xs
    bx = (x1 - x0)
    cx = (x3 - x0)
    dx = (x0 - x1 + x2 - x3)

    ay = y0 - ys
    by_ = (y1 - y0)
    cy = (y3 - y0)
    dy = (y0 - y1 + y2 - y3)

    # t from: (cy + dy*s)*t + (ay + bx_s): this gets messy → use a simpler affine
    # approximation that is exact for parallelograms (which all three faces are).
    #
    # For a parallelogram (dx == dy == 0):
    # P = (1-t)*((1-s)*TL + s*TR) + t*((1-s)*BL + s*BR)
    # => P - TL = s*(TR-TL) + t*(BL-TL)
    # Solve 2×2 linear system:
    #   [TR-TL | BL-TL] * [s, t]^T = [P-TL]

    vx_s = float(x1 - x0)
    vy_s = float(y1 - y0)
    vx_t = float(x3 - x0)
    vy_t = float(y3 - y0)

    det = vx_s * vy_t - vx_t * vy_s
    if abs(det) < 1e-6:
        return

    px = xs - x0
    py = ys - y0

    s = (px * vy_t - py * vx_t) / det
    t = (py * vx_s - px * vy_s) / det

    # Mask pixels inside the parallelogram
    mask = (s >= 0) & (s <= 1) & (t >= 0) & (t <= 1)

    s_m = np.clip(s[mask], 0.0, 1.0)
    t_m = np.clip(t[mask], 0.0, 1.0)

    # Map (s, t) → texture pixel coordinates
    tx_f = s_m * (tw - 1)
    ty_f = t_m * (th - 1)
    tx_i = tx_f.astype(np.int32)
    ty_i = ty_f.astype(np.int32)

    # Bilinear sample
    tx_i1 = np.minimum(tx_i + 1, tw - 1)
    ty_i1 = np.minimum(ty_i + 1, th - 1)
    fx = (tx_f - tx_i)[:, np.newaxis]
    fy = (ty_f - ty_i)[:, np.newaxis]

    c00 = texture[ty_i, tx_i].astype(np.float64)
    c10 = texture[ty_i, tx_i1].astype(np.float64)
    c01 = texture[ty_i1, tx_i].astype(np.float64)
    c11 = texture[ty_i1, tx_i1].astype(np.float64)
    color = (c00 * (1 - fx) * (1 - fy) + c10 * fx * (1 - fy)
             + c01 * (1 - fx) * fy + c11 * fx * fy)

    # Apply brightness and write into dst
    color[:, :3] = np.clip(color[:, :3] * brightness, 0, 255)
    ys_m = ys[mask].astype(np.int32)
    xs_m = xs[mask].astype(np.int32)

    # Alpha-composite: dst = src + dst*(1-src_a)
    src_a = color[:, 3:4] / 255.0
    dst_px = dst[ys_m, xs_m].astype(np.float64)
    blended = color * src_a + dst_px * (1.0 - src_a)
    dst[ys_m, xs_m] = np.clip(blended, 0, 255).astype(np.uint8)


def render_icon(texture_path: Path) -> Image.Image:
    """Render a 64×64 isometric cube icon from a block face texture."""
    texture = Image.open(texture_path).convert("RGBA")
    tex = np.array(texture)

    icon = np.zeros((SIZE, SIZE, 4), dtype=np.uint8)

    # Cube vertex positions
    top    = (CX,          TOP_Y)
    right  = (CX + FACE_W, TOP_Y + FACE_H)
    bottom = (CX,          TOP_Y + FACE_H * 2)
    left   = (CX - FACE_W, TOP_Y + FACE_H)

    br_r   = (CX + FACE_W, TOP_Y + FACE_H + SIDE_H)
    br_l   = (CX - FACE_W, TOP_Y + FACE_H + SIDE_H)
    bot_b  = (CX,          TOP_Y + FACE_H * 2 + SIDE_H)

    # Top face: top → right → bottom → left  (TL=top, TR=right, BR=bottom, BL=left)
    _quad_sample(tex, icon, (top, right, bottom, left), BRIGHTNESS_TOP)

    # Right face: bottom → right → br_r → bot_b  (TL=bottom, TR=right, BR=br_r, BL=bot_b)
    _quad_sample(tex, icon, (bottom, right, br_r, bot_b), BRIGHTNESS_RIGHT)

    # Left face: left → bottom → bot_b → br_l  (TL=left, TR=bottom, BR=bot_b, BL=br_l)
    _quad_sample(tex, icon, (left, bottom, bot_b, br_l), BRIGHTNESS_LEFT)

    return Image.fromarray(icon, "RGBA")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--texture-dir", type=Path, default=BLOCK_TEXTURE_DIR)
    parser.add_argument("--output-dir", type=Path, default=BLOCK_ICON_DIR)
    parser.add_argument("--contact-sheet", type=Path, help="Optional contact-sheet PNG path")
    args = parser.parse_args()

    textures = sorted(args.texture_dir.glob("OneBlock_Block_*.png"))
    if not textures:
        print(f"No block textures found in {args.texture_dir}")
        return

    args.output_dir.mkdir(parents=True, exist_ok=True)
    rendered: list[tuple[str, Image.Image]] = []

    for tex_path in textures:
        # OneBlock_Block_Cave.png → OneBlock_Cave.png
        icon_name = tex_path.stem.replace("OneBlock_Block_", "OneBlock_") + ".png"
        icon_path = args.output_dir / icon_name
        icon = render_icon(tex_path)
        icon.save(icon_path)
        rendered.append((icon_name, icon))
        print(f"  done  {icon_name}")

    if args.contact_sheet:
        columns = 12
        cell = 80
        rows = (len(rendered) + columns - 1) // columns
        sheet = Image.new("RGBA", (columns * cell, rows * cell), "#121a27")
        for idx, (_, img) in enumerate(rendered):
            x = (idx % columns) * cell + 8
            y = (idx // columns) * cell + 8
            sheet.alpha_composite(img, (x, y))
        args.contact_sheet.parent.mkdir(parents=True, exist_ok=True)
        sheet.save(args.contact_sheet)
        print(f"  contact sheet -> {args.contact_sheet}")

    print(f"\nRendered {len(rendered)} block icon(s) to {args.output_dir}")


if __name__ == "__main__":
    main()
