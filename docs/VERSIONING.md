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
  that makes the change. (The launcher icon should have been `0.3.1`
  under this rule; it shipped as `v0.3.0.20` before the rule was
  written down.)

## History so far

| versionName | what it was |
| --- | --- |
| 0.1.0 | The birth series, runs 1–17: converter, weather page, verdicts |
| 0.2.0 | Settings page with per-scale toggles, Kelvin off by default |
| 0.3.0 | Rankine scale; the launcher icon rode along as v0.3.0.20 |

## versionCode: a known gap

Android also carries an integer `versionCode`, separate from
`versionName`. Ours comes from the `tswaVersionCode` Gradle property
and defaults to 1; CI does not set it, so every build so far has had
versionCode 1. That is fine for sideloading from GitHub Releases, but
Google Play requires each upload to have a strictly higher
`versionCode` than the one before. Before the closed track opens, CI
must pass its run number as `tswaVersionCode`. Not yet implemented.
