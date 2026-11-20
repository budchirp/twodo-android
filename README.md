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
- `domain`: application models, repository contracts, and use cases for actions and state
  observation.
- `data`: API requests, persistence, session handling, and repository caches.

Stateful features use `FeatureView` to collect ViewModel state and connect callbacks to private
rendering helpers. `AppNavigation` only declares destinations and navigation callbacks; it does not
load data or coordinate repositories.

Repositories own cached data. ViewModels own drafts, validation, and request state. Each feature
keeps nullable data separate from its nullable `ApiResult<Unit>` load status. Mutation results keep
returned data only when needed for navigation. Cache observations stay active during requests;
refreshes preserve existing content and drafts, and refresh failures show an inline error above that
content. Session and repository invalidation clear obsolete data. Errors stay with the relevant
page, form, or confirmation sheet and are not sent through snackbars.

HTTP failures retain their status code and backend message. Unreadable error bodies fall back to the
HTTP status description. An authenticated `401` atomically invalidates only the token used by that
request; session observation cancels the old session, clears cached data, and returns to sign-in. A
late rejection must never clear credentials from a newer login. Other HTTP failures stay with the
relevant page or sheet and do not sign the user out.

Pages use 16 dp horizontal insets, list top/bottom padding, section gaps, and card padding. Related
controls use 8 dp gaps; profile names and usernames use 2 dp. Apply each inset once. Full-width
dividers have no horizontal inset, and clickable rows own their internal padding so their hover and
ripple cover the row. Feature geometry such as calendar cells, color swatches, and editor controls
keeps its own dimensions.

Use one persistent linear progress indicator per page or sheet operation, fading its opacity over
200 ms without removing its layout space. Pull-to-refresh supplies the gesture, not another loader.
Sheets own mutation loaders and errors; parent pages do not mirror them. The circular indicator is
reserved for application bootstrap. Render errors conditionally in the relevant scrolling content,
after any header, without empty error items. `ErrorCard` is stateless. Retry the failed operation
with its existing inputs and guard duplicate submissions.

Keep each file ordered as public View, private Content, then meaningful feature helpers. Views
collect state and connect callbacks; content renders the scaffold, loader, optional fixed header,
scrolling content, and finally sheets. Use `if` for single conditions and `when` for multiple
meaningful branches. Prefer named arguments for Kotlin calls. Reuse the existing layout and UI
components without introducing a generic screen or action framework.

Presentation consumes constructor-injected domain use cases for actions and state observation. Use
cases delegate to repositories, which retain session and cache ownership. Navigation after an
operation is represented as screen state and handled by the view while resumed. Settings updates
remain simple `state.copy(...)` calls.

## Build

```sh
./gradlew :app:assembleDebug
```
