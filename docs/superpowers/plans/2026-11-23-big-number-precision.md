# Big Number Precision Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Shown temperatures of 5,000 and up read at 3 significant figures, and a million and up in words.

**Architecture:** One magnitude rule inside `Scale.format`, applied to the converted value in the scale being shown. Every caller (big number, hints, Landmarks, weather) goes through it unchanged.

**Tech Stack:** Kotlin, `java.math.BigDecimal` / `MathContext`, JUnit 4.

**Spec:** `docs/superpowers/specs/2026-11-23-big-number-precision-design.md`

## Global Constraints

- Below 5,000 (absolute, after one-decimal rounding): one decimal, exactly as now.
- 5,000 to under a million: 3 significant figures, comma grouped, no decimal, symbol attached (`5,500°C`, `5,770K`).
- A million and up (after 3-figure rounding): words, trailing zeros dropped, space before the symbol (`1.8 million °R`).
- Words: million, billion, trillion, quadrillion; beyond that quadrillion keeps counting (`1,000 quadrillion`).
- Rounding half-up from the decimal string (`Double.toString`), as now. Commas regardless of locale.
- `withSymbol = false` drops the symbol and its space.

## Review Focus

- `Double.toString` gives `1.0E13` for big values: `BigDecimal` must parse it, output must never show `E`.
- 999,999 rounds to 1,000,000 and must read `1 million`, not `1,000,000`.
- 4,999.96 rounds to 5000.0 and must read `5,000°C`, not `5000.0°C`.
- A phone set to German must still show `20,000`, not `20.000`.
- Weather-range values (and absolute zero) must not change at all.

---

### Task 1: Magnitude rule in `Scale.format`

**Files:**
- Modify: `app/src/main/java/com/robnugen/tswa/temperature/Scale.kt:28-38`
- Test: `app/src/test/java/com/robnugen/tswa/temperature/ScaleTest.kt`

**Interfaces:**
- Produces: `Scale.format(celsius: Double, withSymbol: Boolean = true): String` (signature unchanged).

- [ ] **Step 1: Write the failing tests** in `ScaleTest`

```kotlin
@Test
fun belowFiveThousandKeepsOneDecimal() {
    assertEquals("3422.0°C", Scale.C.format(3_422.0))
    assertEquals("4228.5°R", Scale.R.format(2_076.0))
    assertEquals("4999.9°C", Scale.C.format(4_999.9))
}

@Test
fun fromFiveThousandShowsThreeFiguresWithCommas() {
    assertEquals("5,000°C", Scale.C.format(5_000.0))
    assertEquals("5,000°C", Scale.C.format(4_999.96))
    assertEquals("5,500°C", Scale.C.format(5_498.85))
    assertEquals("9,930°F", Scale.F.format(5_498.85))
    assertEquals("5,770K", Scale.K.format(5_498.85))
    assertEquals("10,400°R", Scale.R.format(5_498.85))
    assertEquals("36,500°R", Scale.R.format(20_000.0))
    assertEquals("180,000°R", Scale.R.format(99_726.85))
    assertEquals("999,000°C", Scale.C.format(999_499.0))
    assertEquals("20,000", Scale.C.format(20_000.0, withSymbol = false))
}

@Test
fun fromAMillionReadsInWords() {
    assertEquals("1 million °C", Scale.C.format(999_999.0))
    assertEquals("1 million °C", Scale.C.format(1_000_000.0))
    assertEquals("1.8 million °R", Scale.R.format(1_000_000.0))
    assertEquals("1.23 million °C", Scale.C.format(1_234_000.0))
    assertEquals("27 million °R", Scale.R.format(15_000_000.0))
    assertEquals("100 billion °C", Scale.C.format(100_000_000_000.0))
    assertEquals("10 trillion °C", Scale.C.format(10_000_000_000_000.0))
    assertEquals("10 trillion K", Scale.K.format(10_000_000_000_000.0))
    assertEquals("18 trillion °F", Scale.F.format(10_000_000_000_000.0))
    assertEquals("18 trillion", Scale.R.format(10_000_000_000_000.0, withSymbol = false))
    assertEquals("1,000 quadrillion °C", Scale.C.format(1e18))
}

@Test
fun bigNumbersIgnoreThePhoneLocale() {
    val saved = Locale.getDefault()
    try {
        Locale.setDefault(Locale.GERMANY)
        assertEquals("20,000°C", Scale.C.format(20_000.0))
        assertEquals("1.8 million °R", Scale.R.format(1_000_000.0))
    } finally {
        Locale.setDefault(saved)
    }
}
```

- [ ] **Step 2: Run them and see them fail**

Run: `ANDROID_HOME=~/Android/Sdk ~/.gradle/wrapper/dists/gradle-8.13-bin/*/gradle-8.13/bin/gradle -q :app:testDebugUnitTest --tests com.robnugen.tswa.temperature.ScaleTest`
Expected: FAIL (`5000.0°C` etc.); the existing `formatsToOneDecimalWithSymbol` cases still pass.

- [ ] **Step 3: Implement the rule in `Scale.format`**

Round the converted value to one decimal as now; if its absolute value is under 5,000, return it as now. Otherwise round to `MathContext(3, RoundingMode.HALF_UP)`; under 1e6 print it with `DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))`; else divide by the largest of 1e15/1e12/1e9/1e6 it reaches, print with `stripTrailingZeros().toPlainString()` (thousands grouped the same way for quadrillion), append the word, and join the symbol with a space. Update the comment above `format`.

- [ ] **Step 4: Run all unit tests**

Run: `ANDROID_HOME=~/Android/Sdk ~/.gradle/wrapper/dists/gradle-8.13-bin/*/gradle-8.13/bin/gradle -q :app:testDebugUnitTest`
Expected: exit 0, no output.

- [ ] **Step 5: Commit** (spec and plan go in their own commit first)

```bash
git add docs/superpowers
git commit -m "Plan big numbers at honest precision (mg #901)" -m "<why>" -m "Co-Authored-By: ..."
git add app/src
git commit -m "Show big temperatures at 3 figures (mg #901)" -m "<why>" -m "Co-Authored-By: ..."
```

- [ ] **Step 6: Check on Rob's phone**

Run: `tools/deploy-to-phone.sh`, expand Landmarks, screenshot in °C and °R; step the scale back to °C afterwards. Expected: `10 trillion °C`, `18 trillion °R`, `5,500°C`, `3422.0°C`.
