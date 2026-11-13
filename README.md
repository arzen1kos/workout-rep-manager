<div align="center">
  <img width="100" height="100" alt="workout-rep-manager-logo" src="https://github.com/user-attachments/assets/533567b9-dc44-4f3d-a980-f58faa22e6f1" />

# Reppin | Workout Rep Manager
![Java](https://img.shields.io/badge/Java-f65507?style=for-the-badge&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-00077e?style=for-the-badge&logo=gradle&logoColor=white)
![CSS](https://img.shields.io/badge/css-c159f9?style=for-the-badge&logo=css&logoColor=white)

~✦~

Offline-first workout manager, written in Java and compiled to the browser with [TeaVM](https://teavm.org). Build exercise cards, compose them into routines, run guided sessions with timers, rest blocks and cues, schedule weeks, and track streaks and insights. Everything stays on your device.

</div>

> ### Branch Note: `feat/core-domain-model`
> Slice 1 of the build: pure-Java domain and Library logic in `core`.
> No changes to `web`.
>
> Verify: `./gradlew :core:test`
>
> Known gaps:
>
> - Nothing is persisted yet (IndexedDB adapter is the next slice).
> - Tags cannot be deleted; link/unlink return the ids callers must remap.
> - Names are not Unicode-normalised yet (needs the TeaVM spike).
> - Not yet compiled for TeaVM; check once the web build is green.
>
> Next: IndexedDB storage for exercise cards, then the Library tab UI.

## What is in the box

| Path | What it is | Verified here? |
|---|---|---|
| `core/` | Pure-Java domain: unit conversions (`Units`), injected `Clock`, timestamp-based `Countdown` | Compiled with `javac --release 21 -Xlint:all` and run against a scratch check of the same logic. JUnit/jqwik tests are written but **not run** (no network for Gradle) |
| `web/` | TeaVM app shell: `Main.java`, `index.html`, manifest, service worker, CSS design tokens, icons | **Not compiled.** Written against the documented TeaVM JSO API; expect small signature fixes on first build |
| `.github/workflows/ci.yml` | Test core, build PWA, upload artifact | Not run |
| `docs/` | `SPEC.md`, `BUILD_PROMPT.md`, `NEXT_STEPS.md`, `DECISIONS.md` | n/a |
| Gradle wrapper | Gradle 8.14.3 | Generated; download happens on first run |

## Prerequisites

- JDK 21 (Temurin or similar)
- Network access for the first Gradle run (downloads Gradle, the TeaVM plugin and dependencies)
- Python 3 (only to serve the built PWA locally) or any static file server

## Run it

```bash
./gradlew :core:test                 # JUnit 5 + jqwik property tests
./gradlew :web:assemblePwa           # TeaVM -> JavaScript, assembled into web/build/pwa
python3 -m http.server 8080 -d web/build/pwa
# open http://localhost:8080  (service workers are allowed on localhost)
```

Release-style build (obfuscated, no source maps):

```bash
./gradlew :web:assemblePwa -Pteavm.js.obfuscated=true -Pteavm.js.sourceMap=false
```

TeaVM also has a hot-reload dev server (`./gradlew :web:javaScriptDevServer`, documented at teavm.org/docs/tooling/gradle.html). It serves the generated script, so you will need to point your `index.html` at it or use its static dirs option; try it after the first build works.

## Packages

| Package | Where | Version | Source of the version |
|---|---|---|---|
| TeaVM Gradle plugin `org.teavm` | `web` | 0.16.0 | teavm.org Gradle docs |
| `teavm.libs.jsoApis` (DOM, IndexedDB, etc. wrappers) | `web` | tied to plugin | same |
| JUnit Jupiter (via `junit-bom`) | `core` tests | 5.14.4 | jqwik 1.10.1 user guide |
| jqwik (property-based tests) | `core` tests | 1.10.1 | jqwik user guide |
| `junit-platform-launcher` | `core` tests runtime | from BOM | needed by newer Gradle |

Re-check all versions on Maven Central / the plugin portal before relying on them. TeaVM 0.15 raised its minimum Java to 17; this project targets 21.

## Layout

```text
core/   pure Java, zero runtime dependencies (keeps it TeaVM-safe)
web/    TeaVM entry point + static PWA shell (src/main/webapp)
docs/   spec, build prompt, next steps, decisions
```

## Known limitations

- Platform limits on background timers, wake lock and vibration (iOS especially) are real; see SPEC section 8.8. The first milestone is a feasibility spike for exactly this.
- Placeholder icons (a lime dot). Fonts are not bundled; see NEXT_STEPS step 4.
- Licence not chosen yet.

---

Created by Arsenie — 2026
