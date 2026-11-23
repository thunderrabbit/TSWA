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

## In CI

`.github/workflows/quality.yml` runs both on every push to
master and every pull request. The release workflow also calls it as
its first job, and the release build `needs` that job: a red Quality
run means no release is built. That dependency was added on
2026-11-22 at Rob's direction, the day the tools landed.

## Compose naming

Jetpack Compose functions are PascalCase by convention (`Header`,
`TabBar`): they read as pieces of UI, not actions. Both tools are told
to skip their lowercase function-name rule for functions annotated
`@Composable` (`.editorconfig` for ktlint, `FunctionNaming` in
`detekt.yml`); every other function keeps the rule.

## One convention worth naming

A `catch` is never silently empty. Storage access can throw, and the
app must not die for it, so the catch stays — but it says what was
given up (persisting, caching, restoring). detekt's `EmptyCatchBlock`
and `SwallowedException` rules enforce this.

## History

Until mg #891 (2026-11-23) the UI was a web page inside a WebView, and
ESLint checked its JavaScript. The page and ESLint went when the
native rewrite began; the page survives in git history (see
`docs/BUILDING.md`).
