# Versioning

How TSWA's version numbers work, written down at Rob's request on
2026-11-22 so the plan lives in the repo instead of in habit.

## Release tags

Every CI build publishes a GitHub Release tagged `v<versionName>.<run>`,
for example `v0.3.0.20`. The `versionName` lives in
`app/build.gradle.kts`; the workflow appends the GitHub Actions run
number and stamps the full string into the app page footer, so the
version shown inside the app always names the exact build.

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
