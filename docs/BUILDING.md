# Building TSWA

## In GitHub Actions (the normal way)
Every push to `master` (and any manual "Run workflow" from the Actions tab)
runs `.github/workflows/android.yml`:

- **tswa-release** artifact: the signed release APK (only when the four
  signing secrets are set, see `docs/SIGNING.md`), and a GitHub Release
  carrying that same APK. Per Rob (2026-11-21) there is deliberately no
  debug build: its throwaway per-run signing key caused "App Not
  Installed" clashes on his phone.

## Locally (optional)
Needs JDK 17 and the Android SDK, then:

```
gradle :app:assembleRelease
```

The repo intentionally has no Gradle wrapper jar committed; CI installs
Gradle via `gradle/actions/setup-gradle`. To build locally with the
wrapper, run `gradle wrapper` once and use `./gradlew` from then on.

## Layout
- `app/src/main/java/.../MainActivity.kt` — starts the Compose UI.
- `app/src/main/java/.../ui/` — the UI: `TswaApp.kt` (header, pages,
  tab bar, footer), `AppState.kt` (state shared by every page: the
  scale shown, the converter's value, the weather), the pages
  (`WeatherPage.kt` with `WeatherForecast.kt` and `WeatherDetails.kt`,
  `ConverterPage.kt`, `SettingsPage.kt`), `SearchRow.kt`, `ScaleControl.kt` (a
  temperature that steps the scale when tapped), `Hints.kt`, `Card.kt`,
  `Tab.kt`, `TswaColors.kt`, `TswaTheme.kt`.
- `app/src/main/res/drawable/wx_*.xml` — weather icons, drawn from the
  web UI's glyphs.
- `app/src/main/java/.../weather/` — the weather data: `OpenMeteo.kt`
  (the fetch; it sends its own User-Agent because Open-Meteo refuses
  Android's default one), `Forecast.kt` (the parsed response),
  `WeatherCache.kt` (last good forecast in `weather_cache.json`),
  `WeatherController.kt` (what the app shows and whether it is fresh),
  `WeatherView.kt` (what the Weather page shows, and the details grid's
  unit conversions), `Geocoding.kt` (place search), `Wmo.kt`,
  `Place.kt`. Tests use a saved response in
  `app/src/test/resources/`.
- `app/src/main/java/.../settings/TswaPrefs.kt` — everything kept
  between runs, in one SharedPreferences file (`tswa_settings`).
- `app/src/main/java/.../temperature/` — logic with no UI: `Scale.kt`
  (conversions, formatting, the tap's step order), `ScaleChoice.kt`
  (switching scales on and off) and `Verdicts.kt`
  (verdicts, landmarks, presets). Tested by `app/src/test/`.
- `app/src/main/res/drawable/ic_tab_*.xml` — tab icons, drawn from the
  web UI's glyphs.

## The old web UI
Up to v0.5.1 the whole app was one web page in a WebView. It is the
reference for the native rewrite (verdict texts, landmarks, conversion
maths) and is kept in git history, not in the build:

```
git show b4dcfdd:app/src/main/assets/www/index.html
```

Screenshots of it running are outside the repo, in
`~/work/rob/tswa/TSWA-rewrite-screenshots/pre/` on Rob's machine.
