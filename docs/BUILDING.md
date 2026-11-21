# Building TSWA

## In GitHub Actions (the normal way)
Every push to `master` (and any manual "Run workflow" from the Actions tab)
runs `.github/workflows/android.yml`:

- **tswa-debug** artifact: debug APK, installs immediately, for UI checks.
- **tswa-release** artifact: signed release APK, only when the four
  signing secrets are set (see `docs/SIGNING.md`).

## Locally (optional)
Needs JDK 17 and the Android SDK, then:

```
gradle :app:assembleDebug
```

The repo intentionally has no Gradle wrapper jar committed; CI installs
Gradle via `gradle/actions/setup-gradle`. To build locally with the
wrapper, run `gradle wrapper` once and use `./gradlew` from then on.

## Layout
- `app/src/main/java/.../MainActivity.kt` — thin WebView shell.
- `app/src/main/assets/www/index.html` — the whole TSWA UI (converter +
  weather). This is the file to edit for UI iteration; a push rebuilds
  the APK automatically.
