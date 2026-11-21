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
- `app/src/main/java/.../MainActivity.kt` — thin WebView shell.
- `app/src/main/assets/www/index.html` — the whole TSWA UI (converter +
  weather). This is the file to edit for UI iteration; a push rebuilds
  the APK automatically.
