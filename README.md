<div align="center">
    <h1>twodo-android</h1>
    <h2>A shared to-do list for couples</h2>
</div>

<div align="center">
  <img alt="Stargazers" src="https://img.shields.io/github/stars/budchirp/twodo?style=for-the-badge&colorA=0b1221&colorB=ff8e8e" />
  <img alt="Last commit" src="https://img.shields.io/github/last-commit/budchirp/twodo?style=for-the-badge&colorA=0b1221&colorB=BDB0E4" />
  <img alt="Issues" src="https://img.shields.io/github/issues/budchirp/twodo?style=for-the-badge&colorA=0b1221&colorB=FBC19D" />
</div>

## Code structure

- `app`: Android entry point and dependency wiring.
- `presentation`: Compose UI, screen state, and ViewModels.
- `domain`: application models, repository contracts, and cross-repository operations.
- `data`: API requests, persistence, session handling, and repository caches.

Stateful features use `FeatureView` to collect ViewModel state and connect callbacks to private
rendering helpers. `AppNavigation` only declares destinations and navigation callbacks; it does not
load data or coordinate repositories.

Repositories own cached data. ViewModels own drafts, validation, and request state. Navigation after
an operation is represented as screen state and handled by the view while resumed. API-backed UI
state uses `UiData.Empty`, `Loading`, `Success`, and `Error`, handled directly with `when`
statements. Views render progress indicators where needed; `ErrorCard` owns its animation. Existing
data stays visible on refresh failure, and mutation errors stay with the form. Errors are not sent
through snackbars.

Prefer named arguments for Kotlin calls. Extract shared behavior rather than introducing
pass-through use cases or a generic action framework. Settings updates remain simple
`state.copy(...)` calls.

## Build

```sh
./gradlew :app:assembleDebug
```
