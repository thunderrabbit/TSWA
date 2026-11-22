# Code quality

The tools that check TSWA's code, set up on 2026-11-22 at Rob's
request. The standard is zero findings: the codebase passed clean the
day the tools arrived, so there is no baseline file and no
grandfathered warnings. A new finding fails the Quality workflow.

## The tools

| Tool | Checks | Version |
| --- | --- | --- |
| ktlint | Kotlin style and formatting (official code style, pinned in `.editorconfig`) | 1.8.0 |
| detekt | Kotlin static analysis: complexity, bug patterns, smells. Config is `detekt.yml` in the repo root, the tool's own defaults written out in full | 1.23.8 |
| ESLint | The JavaScript inside `app/src/main/assets/www/index.html`, extracted by eslint-plugin-html. Rules are ESLint's recommended set with browser globals; config is `eslint.config.js` | 9 (see package.json) |

Android Lint is deliberately not here: it needs the full Android SDK
and a Gradle build, so if it is ever adopted it belongs in the release
workflow, not this one.

## Run them locally

Kotlin tools (Muse's VM keeps a JRE, ktlint and detekt under
`~/workspace/tools/`; anywhere else, install a JDK 17 and the two
pinned releases):

```
export JAVA_HOME=~/workspace/tools/jdk-17.0.21.1+2-jre
~/workspace/tools/ktlint/ktlint app/src/main/java   # add -F to auto-fix style
$JAVA_HOME/bin/java -jar ~/workspace/tools/detekt/detekt-cli.jar \
  --config detekt.yml --input app/src/main/java
```

JavaScript (Node required):

```
npm ci
npm run lint:js
```

## In CI

`.github/workflows/quality.yml` runs all three on every push to
master and every pull request. The release workflow also calls it as
its first job, and the release build `needs` that job: a red Quality
run means no release is built. That dependency was added on
2026-11-22 at Rob's direction, the day the tools landed.

## One convention worth naming

A `catch` in the web UI is never silently empty. Storage access in a
WebView can throw, and the app must not die for it, so the catch stays
— but it carries a comment saying what was given up (persisting,
caching, restoring). ESLint's `no-empty` and `no-unused-vars` rules
are what enforce this.
