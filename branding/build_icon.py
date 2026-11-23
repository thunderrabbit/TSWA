#!/usr/bin/env python3
"""Build the Two Skies launcher icon as SVG masters and export every size.

Design approved by Rob 2026-11-23: an ocean with a sun rising on the
horizon, degree-C on the left and degree-F on the right of the sun, in
the app's Celsius cyan and Fahrenheit amber. Letters are Poppins Bold
outlines extracted with fontTools, so the SVG is fully self-contained and
every PNG export is a perfect scale of one master.

Android's adaptive icon has two layers on a 108dp canvas, of which the
launcher shows the central 72dp. The sky and sea are the background layer
and run past the visible area so any mask crops ocean, not navy; the sun,
its reflection, the waves and the letters are the foreground.

Needs cairosvg and fontTools: pip install cairosvg fonttools
"""
import math
import os

import cairosvg
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.ttLib import TTFont

HERE = os.path.dirname(os.path.abspath(__file__))
CLONE = os.environ.get("TSWA_CLONE", os.path.dirname(HERE))
FONT = os.path.join(HERE, "Poppins-Bold.ttf")

NAVY = "#182642"      # app palette: TswaColors.Navy (the web UI's --card2)
SKY_LOW = "#2a3f6b"   # sky lightening towards the horizon
CYAN = "#57c7ff"      # app accent (Celsius)
AMBER = "#ffb454"     # app warm accent (Fahrenheit)
SUN = "#ffe08a"       # paler than AMBER so the sun and the F stay apart
SEA_TOP = "#1f5f8b"
SEA_BOT = "#123a5c"
FOAM = "#8fd3ff"

CANVAS = 512
HORIZON = 330
SUN_R = 54
# Adaptive layers: 108dp canvas, central 72dp visible. Expressed as a
# viewBox around the 512 artwork: 512 * 108 / 72 = 768, offset by 128.
BLEED = 128


def letter(ch, cx, cy, height, colour):
    """Return (svg, top-left of the placed glyph) for ch centred at (cx, cy)."""
    font = TTFont(FONT)
    gs = font.getGlyphSet()
    gname = font.getBestCmap()[ord(ch)]
    pen = SVGPathPen(gs)
    gs[gname].draw(pen)
    glyf = font["glyf"][gname]
    s = height / (glyf.yMax - glyf.yMin)
    xc, yc = (glyf.xMin + glyf.xMax) / 2, (glyf.yMin + glyf.yMax) / 2
    w = (glyf.xMax - glyf.xMin) * s
    svg = (f'<path d="{pen.getCommands()}" fill="{colour}" '
           f'transform="translate({cx} {cy}) scale({s:.5f} {-s:.5f}) '
           f'translate({-xc} {-yc})"/>')
    return svg, (cx - w / 2, cy - height / 2)


def degree(top_left, colour):
    """Degree ring before the letter, upper left."""
    left, top = top_left
    return (f'<circle cx="{left - 30:.1f}" cy="{top + 14:.1f}" r="13" '
            f'fill="none" stroke="{colour}" stroke-width="10"/>')


def wave(y, phase):
    d = f"M {-BLEED - 20 + phase} {y}"
    for _ in range((CANVAS + 2 * BLEED) // 128 + 2):
        d += " q 32 -14 64 0 t 64 0"
    return (f'<path d="{d}" fill="none" stroke="{FOAM}" stroke-width="9" '
            'stroke-linecap="round" opacity="0.8"/>')


def background():
    """Sky over sea, bled out past the 512 square for adaptive masks."""
    lo, span = -BLEED, CANVAS + 2 * BLEED
    return ('<defs><linearGradient id="sky" x1="0" y1="0" x2="0" y2="1">'
            f'<stop offset="0" stop-color="{NAVY}"/>'
            f'<stop offset="1" stop-color="{SKY_LOW}"/></linearGradient>'
            '<linearGradient id="sea" x1="0" y1="0" x2="0" y2="1">'
            f'<stop offset="0" stop-color="{SEA_TOP}"/>'
            f'<stop offset="1" stop-color="{SEA_BOT}"/></linearGradient></defs>'
            f'<rect x="{lo}" y="0" width="{span}" height="{HORIZON}" '
            'fill="url(#sky)"/>'
            f'<rect x="{lo}" y="{lo}" width="{span}" height="{BLEED}" '
            f'fill="{NAVY}"/>'
            f'<rect x="{lo}" y="{HORIZON}" width="{span}" '
            f'height="{CANVAS - HORIZON}" fill="url(#sea)"/>'
            f'<rect x="{lo}" y="{CANVAS}" width="{span}" height="{BLEED}" '
            f'fill="{SEA_BOT}"/>')


def foreground():
    p = []
    # rising sun, half above the horizon, with rays fanned over the top
    p.append(f'<clipPath id="above"><rect x="0" y="0" width="{CANVAS}" '
             f'height="{HORIZON}"/></clipPath>'
             f'<circle cx="256" cy="{HORIZON}" r="{SUN_R}" fill="{SUN}" '
             'clip-path="url(#above)"/>')
    for a in range(-80, 81, 32):
        r = math.radians(a - 90)
        x1, y1 = 256 + 70 * math.cos(r), HORIZON + 70 * math.sin(r)
        x2, y2 = 256 + 92 * math.cos(r), HORIZON + 92 * math.sin(r)
        p.append(f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" '
                 f'y2="{y2:.1f}" stroke="{SUN}" stroke-width="11" '
                 'stroke-linecap="round"/>')
    # the sun's reflection on the water
    for i, (w, y) in enumerate(((90, 352), (60, 378), (34, 402))):
        p.append(f'<rect x="{256 - w / 2}" y="{y}" width="{w}" height="9" '
                 f'rx="4.5" fill="{SUN}" opacity="{0.7 - i * 0.18:.2f}"/>')
    p.append(wave(392, 0))
    p.append(wave(440, 32))
    c_svg, c_pos = letter("C", 156, 184, 108, CYAN)
    f_svg, f_pos = letter("F", 376, 184, 108, AMBER)
    p += [degree(c_pos, CYAN), degree(f_pos, AMBER), c_svg, f_svg]
    return "".join(p)


def svg_doc(body, view=(0, 0, CANVAS, CANVAS), size=CANVAS):
    vb = " ".join(str(v) for v in view)
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" '
            f'height="{size}" viewBox="{vb}">{body}</svg>')


def render(svg, path, size):
    cairosvg.svg2png(bytestring=svg.encode(), write_to=path,
                     output_width=size, output_height=size)
    print("wrote", path, size)


def main():
    bg, fg_art = background(), foreground()
    adaptive = (-BLEED, -BLEED, CANVAS + 2 * BLEED, CANVAS + 2 * BLEED)
    full = svg_doc(bg + fg_art)
    bg_layer = svg_doc(bg, adaptive)
    fg_layer = svg_doc(fg_art, adaptive)
    circ = svg_doc(
        '<clipPath id="c"><circle cx="256" cy="256" r="256"/></clipPath>'
        f'<g clip-path="url(#c)">{bg}{fg_art}</g>')
    prev = svg_doc(
        '<clipPath id="p"><rect width="512" height="512" rx="114"/></clipPath>'
        f'<g clip-path="url(#p)">{bg}{fg_art}</g>')

    brand_dir = os.path.join(CLONE, "branding")
    os.makedirs(brand_dir, exist_ok=True)
    for name, doc in (("tswa-icon.svg", full),
                      ("tswa-icon-background.svg", bg_layer),
                      ("tswa-icon-foreground.svg", fg_layer)):
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
        render(bg_layer, os.path.join(d, "ic_launcher_background.png"),
               int(108 * mult))
        render(fg_layer, os.path.join(d, "ic_launcher_foreground.png"),
               int(108 * mult))
    render(full, os.path.join(brand_dir, "icon-512.png"), 512)
    render(prev, os.path.join(HERE, "preview-512.png"), 512)


if __name__ == "__main__":
    main()
