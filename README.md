# TSWA — Two Skies Weather App

An Android weather app that shows the temperature in more than one
scale, made so Rob can tell family in the USA what the weather is in
Adelaide.

## The name
TSWA first stood for Tri-Scale Weather App, because Rob didn't know about
the Rankine scale when he named it. Once the app had four scales the
name no longer fit, so TSWA now stands for Two Skies Weather App. The
letters stay in `tswa.robnugen.com`, the package `com.robnugen.tswa`
and the code. The launcher label is just "Two Skies".

## What it does
- **Weather**: current conditions for a searched or saved place, from
  [Open-Meteo](https://open-meteo.com/) (forecast and geocoding APIs,
  no API key needed).
- **Converter**: converts between Celsius, Fahrenheit, Kelvin and
  Rankine, starting from the Weather page's temperature. Tap a
  temperature to cycle through the scales.
- **Settings**: choose which scales the tap cycle visits (Celsius and
  Fahrenheit are on by default), and save, sort and swipe away places.
- **Onboarding**: on first run the app asks about using your location.

## Status
TSWA is a native Kotlin app. Up to v0.5.1 it was a WebView shell
around one web page; the native rewrite (mg #876, November 2026) does
everything that page did, plus a smooth drag sort for saved places. The
web page lives on only in git history as the reference (see
[docs/BUILDING.md](docs/BUILDING.md)). The native version brought
saved places over from the WebView version's storage once; that import
was removed after it had done its job (mg #900).

## UI toolkit: Jetpack Compose
The native UI is written in Jetpack Compose with Material 3, chosen
for the rewrite on 2026-11-23 (mg #891):

- The reason for the rewrite is the saved-places drag sort, which kept
  getting stuck in the WebView. Compose has swipe-to-dismiss built in
  and a well-used reorderable-list library, so the sort and the swipe
  delete can be native gestures instead of fighting the browser.
- It is Android's current recommended toolkit, written in Kotlin
  only, with no XML layouts to keep in step with the code.
- It fits the existing build: the versions chosen work with the
  project's Kotlin, Android Gradle plugin and compileSdk, so nothing
  else had to be upgraded.

## Building
Every push to `master` makes GitHub Actions build a signed release APK
and publish it as a GitHub Release. Pushes are gated on the quality
tools (ktlint, detekt).

- [docs/BUILDING.md](docs/BUILDING.md): CI and local builds
- [docs/QUALITY.md](docs/QUALITY.md): the quality tools and release gate
- [docs/SIGNING.md](docs/SIGNING.md): release signing secrets
- [docs/VERSIONING.md](docs/VERSIONING.md): version numbers and release tags
- `branding/build_icon.py`: regenerates every launcher icon size from
  one SVG master (needs `cairosvg` and `fonttools`)

## License
GPL-3.0. See [LICENSE](LICENSE).
