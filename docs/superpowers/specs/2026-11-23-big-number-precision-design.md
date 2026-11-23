# Big numbers read only as precise as they are

## Why

The far-hot landmarks (mg #901) print as `10000000000000.0°C`,
`100000000000273.2K` or `18000000000491.7°R`. Nobody can count the
zeros, and the digits after the first few claim a precision no source
has: 10 trillion K is a rough measurement, so its +273.15 offset and
decimal are noise. Rob does not want the app to claim that.

## Goals

- Numbers of 5,000 and up show at most 3 significant figures.
- Numbers of a million and up read in words.
- Everyday numbers (weather, the converter's usual range, melting points
  in °C and K) look exactly as they do now.

## Non-goals

- Making the far-hot verdicts reachable on the big number (separate idea:
  tappable Landmarks rows).
- Rewording any verdict text. Tungsten's "6,192 F" and the Sun's
  "5,772 K" stay; the table showing 6,190°F and 5,770 K is the same
  quantity at a coarser precision.

## Approaches

1. **One magnitude rule in `Scale.format`** (picked). Simple, covers the
   big number, hints and Landmarks alike.
2. Rule 1 plus a precision field on each landmark. More honest below the
   cutoff, but more data; not needed once the cutoff sits at 5,000.
3. Entering astronomical entries in kelvin. Tidier source numbers, but
   does not fix the display by itself; dropped.

Cutoff: 1,000 rounded exact reference points (gold 1,064.18 → 1,060);
4,000 also rounded boron in °R for no gain. 5,000 keeps every melting
point exact in °C and K and puts the Sun at 5,500°C.

## The rule

Applied to the number in the scale being shown, after conversion, by
its absolute value:

| Shown value | Format | Examples |
|---|---|---|
| below 5,000 | one decimal, as now | `22.2°C`, `3422.0°C`, `4228.5°R` |
| 5,000 to under a million | 3 significant figures, comma grouped, no decimal | `5,500°C`, `9,930°F`, `20,000°C`, `180,000°R` |
| a million and up | 3 significant figures in words, trailing zeros dropped, space before the symbol | `1 million °C`, `1.8 million °R`, `15 million °C`, `100 billion °C`, `10 trillion °C`, `18 trillion °R` |

- Round first, then pick the row, so 999,999 reads `1 million`.
- Rounding stays half-up from the decimal string, as `format` does now.
- Words: million, billion, trillion, quadrillion. Above that is outside
  anything in the app; quadrillion keeps counting (`1,000 quadrillion`).
- `withSymbol = false` drops the symbol and its space.
- Grouping uses commas regardless of phone locale, matching the app's
  existing `Locale.US` number formatting.

## Testing

`ScaleTest` gains cases for each row, both boundaries (4,999.9 /
5,000; 999,999 / 1,000,000), every scale, and `withSymbol = false`.
Deploy to Rob's phone and check the Landmarks table in °C and °R.

## Left to the builder

Where the helper lives inside `Scale.kt`, and naming.
