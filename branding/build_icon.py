#!/usr/bin/env python3
"""Build the TSWA launcher icon as SVG masters and export every size.

Design: concept A as approved by Rob 2026-11-22 - the C/F/K cycle letters
on the app's navy, arrows all counterclockwise in the app's tap order
(C -> F -> K -> C), degree signs before C and F, K bare. Letters are
Poppins Bold outlines extracted with fontTools, so the SVG is fully
self-contained and every PNG export is a perfect scale of one master.
"""
import math
import os

import cairosvg
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.ttLib import TTFont

HERE = os.path.dirname(os.path.abspath(__file__))
CLONE = os.environ.get("TSWA_CLONE", os.path.dirname(HERE))
FONT = os.path.join(HERE, "Poppins-Bold.ttf")

NAVY = "#182642"   # app palette: deepest panel navy in index.html
CYAN = "#57c7ff"   # app accent (Celsius)
AMBER = "#ffb454"  # app warm accent (Fahrenheit)
ICE = "#eef3fb"    # app text (Kelvin)

CANVAS = 512
CENTER = (256, 262)      # centre of the arrow ring
RING_R = 172
SHAFT_W = 25


def glyph(name):
    font = TTFont(FONT)
    gs = font.getGlyphSet()
    gname = font.getBestCmap()[ord(name)]
    pen = SVGPathPen(gs)
    gs[gname].draw(pen)
    glyf = font["glyf"][gname]
    return pen.getCommands(), glyf.xMin, glyf.xMax, glyf.yMin, glyf.yMax


def letter(name, cx, cy, height, colour):
    """Return (svg, placed bbox) for a letter centred at (cx, cy)."""
    d, x0, x1, y0, y1 = glyph(name)
    s = height / (y1 - y0)
    xc, yc = (x0 + x1) / 2, (y0 + y1) / 2
    w = (x1 - x0) * s
    svg = (f'<path d="{d}" fill="{colour}" '
           f'transform="translate({cx} {cy}) scale({s:.5f} {-s:.5f}) '
           f'translate({-xc} {-yc})"/>')
    bbox = (cx - w / 2, cy - height / 2, cx + w / 2, cy + height / 2)
    return svg, bbox


def degree(bbox, colour):
    """Degree ring before the letter, upper left, as Rob specified."""
    left, top, _, _ = bbox
    cx, cy, r, w = left - 36, top + 16, 14, 10.5
    return (f'<circle cx="{cx:.1f}" cy="{cy:.1f}" r="{r}" fill="none" '
            f'stroke="{colour}" stroke-width="{w}"/>')


def polar(a_deg):
    a = math.radians(a_deg)
    return (CENTER[0] + RING_R * math.cos(a), CENTER[1] + RING_R * math.sin(a))


def arrow(a_from, a_to, colour):
    """Counterclockwise arc arrow; travel is towards decreasing angle."""
    sx, sy = polar(a_from)
    ex, ey = polar(a_to)
    a = math.radians(a_to)
    tx, ty = math.sin(a), -math.cos(a)          # unit tangent, direction of travel
    nx, ny = math.cos(a), math.sin(a)           # unit radial
    tip = (ex + tx * 10, ey + ty * 10)
    base = (ex - tx * 30, ey - ty * 30)
    c1 = (base[0] + nx * 25, base[1] + ny * 25)
    c2 = (base[0] - nx * 25, base[1] - ny * 25)
    shaft_end = (ex + tx * 8, ey + ty * 8)
    pts = f"{tip[0]:.1f},{tip[1]:.1f} {c1[0]:.1f},{c1[1]:.1f} {c2[0]:.1f},{c2[1]:.1f}"
    return (f'<path d="M {sx:.1f} {sy:.1f} A {RING_R} {RING_R} 0 0 0 '
            f'{shaft_end[0]:.1f} {shaft_end[1]:.1f}" fill="none" '
            f'stroke="{colour}" stroke-width="{SHAFT_W}" stroke-linecap="round"/>'
            f'<polygon points="{pts}" fill="{colour}" stroke="{colour}" '
            f'stroke-width="9" stroke-linejoin="round"/>')


def artwork():
    parts = []
    c_svg, c_box = letter("C", 278, 150, 140, CYAN)
    f_svg, f_box = letter("F", 142, 362, 140, AMBER)
    k_svg, _ = letter("K", 374, 362, 140, ICE)
    parts.append(arrow(228, 176, CYAN))    # C -> F, down the left
    parts.append(arrow(108, 74, AMBER))    # F -> K, along the bottom
    parts.append(arrow(34, -30, ICE))      # K -> C, up the right
    parts.append(degree(c_box, CYAN))
    parts.append(degree(f_box, AMBER))
    parts += [c_svg, f_svg, k_svg]
    return "".join(parts)


def svg_doc(body, size=CANVAS):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" '
            f'height="{size}" viewBox="0 0 {CANVAS} {CANVAS}">{body}</svg>')


def render(svg, path, size):
    cairosvg.svg2png(bytestring=svg.encode(), write_to=path,
                     output_width=size, output_height=size)
    print("wrote", path, size)


def main():
    art = artwork()
    full = svg_doc(f'<rect width="512" height="512" fill="{NAVY}"/>{art}')
    fg = svg_doc(f'<g transform="translate(256 256) scale(0.66) '
                 f'translate(-256 -256)">{art}</g>')
    circ = svg_doc(
        '<clipPath id="c"><circle cx="256" cy="256" r="256"/></clipPath>'
        f'<g clip-path="url(#c)"><rect width="512" height="512" '
        f'fill="{NAVY}"/>{art}</g>')
    prev = svg_doc(
        '<clipPath id="p"><rect width="512" height="512" rx="114"/></clipPath>'
        f'<g clip-path="url(#p)"><rect width="512" height="512" '
        f'fill="{NAVY}"/>{art}</g>')

    brand_dir = os.path.join(CLONE, "branding")
    os.makedirs(brand_dir, exist_ok=True)
    for name, doc in (("tswa-icon.svg", full), ("tswa-icon-foreground.svg", fg)):
        with open(os.path.join(brand_dir, name), "w") as fh:
            fh.write(doc)
        print("wrote", os.path.join(brand_dir, name))

    res = os.path.join(CLONE, "app/src/main/res")
    densities = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}
    for dens, mult in densities.items():
        d = os.path.join(res, f"mipmap-{dens}")
        os.makedirs(d, exist_ok=True)
        render(full, os.path.join(d, "ic_launcher.png"), int(48 * mult))
        render(circ, os.path.join(d, "ic_launcher_round.png"), int(48 * mult))
        render(fg, os.path.join(d, "ic_launcher_foreground.png"), int(108 * mult))
    render(full, os.path.join(brand_dir, "icon-512.png"), 512)
    render(prev, os.path.join(HERE, "preview-512.png"), 512)


if __name__ == "__main__":
    main()
