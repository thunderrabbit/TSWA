# Versioning

How TSWA's version numbers work, written down at Rob's request on
2026-11-22 so the plan lives in the repo instead of in habit.

## Release tags

Every CI build publishes a GitHub Release tagged `v<versionName>.<run>`,
for example `v0.3.2.23`. The base `versionName` (the first three
segments) lives in `app/build.gradle.kts` as `tswaBaseVersionName`; the
workflow appends the GitHub Actions run number and passes the full
string to Gradle as the `tswaVersionName` property, so the four-part
string is the versionName inside the binary itself, not just a label.
The app shows it in its header (read from `BuildConfig.VERSION_NAME`)
and it is used as the release tag. One version, seen everywhere: Rob's rule is that the version
visible in the app and the version visible on the Play Store must be
the same version, and since 0.3.2 they are, because the Play Store
shows the binary's versionName.

The run number needs no decision: CI increments it on every build, so
builds that share a versionName are still distinct (the birth series
ran `v0.1.0.1` through `v0.1.0.17`).

## versionName: MAJOR.MINOR.PATCH

- **MAJOR** — landmark releases only, and only by Rob's decision.
  `1.0.0` is earmarked for the Google Play production launch unless
  Rob says otherwise. An agent never bumps MAJOR on its own.
- **MINOR** — a new thing the user can do: a new capability. Bumped in
  the BEGIN commit of the feature's git bubble, and only there.
  Examples: `0.2.0` Settings with per-scale toggles (mg #886);
  `0.3.0` the Rankine scale.
- **PATCH** — a shipped change that is not a new capability: bug
  fixes, polish, artwork and other asset changes. Bumped in the commit
  that makes the change. (Under this rule the launcher icon would have
  been a PATCH bump; it shipped as `v0.3.0.20` before the rule was
  written down.)

## History so far

| versionName | what it was |
| --- | --- |
| 0.1.0 | The birth series, runs 1–17: converter, weather page, verdicts |
| 0.2.0 | Settings page with per-scale toggles, Kelvin off by default |
| 0.3.0 | Rankine scale; the launcher icon rode along as v0.3.0.20 |
| 0.3.1 | This versioning plan, written down and corrected (see below) |
| 0.3.2 | One visible version: the four-part string becomes the binary's versionName, so app, tag and Play Store agree |

## versionCode

Android also carries an integer `versionCode`, separate from
`versionName`, and Google Play requires each upload to have a strictly
higher one than the upload before. Ours is already Play-shaped: the
workflow passes its run number to Gradle as the `tswaVersionCode`
property (`gradle :app:assembleRelease -PtswaVersionCode=<run>`), so a
build's `versionCode` always equals the fourth segment of its release
tag and strictly increases from build to build. The property's default
of 1 in `app/build.gradle.kts` only applies to local builds, which CI
never ships. (The first draft of this page, shipped in v0.3.0.21,
wrongly called this a gap; corrected in 0.3.1.)
