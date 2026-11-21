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

Standard cards and grouped cards render the same `SettingRow`. Single-line rows use
`SettingRowSize.Compact`: 64 dp minimum height and 12 dp vertical padding. Radio and checkbox
selection groups and ordinary `SwitchSetting` rows explicitly use this compact size. Descriptions
and trailing action controls default to `SettingRowSize.Comfortable`: 80 dp minimum height and 16 dp
vertical padding. `LargeSwitchSetting` uses the comfortable size and primary-container color for
prominent toggles, including Material You’s main switch. Both sizes use 16 dp horizontal padding, a
16 dp gap between row contents, and 2 dp between title and supporting text. Leading icons and images
use their natural width without a reserved column. Leading content and trailing controls are not
height-clamped. Rows grow for wrapped text and larger fonts. Choose a shared row size instead of
adding per-row padding or height overrides. Custom card content uses the shared `AppCard`
frame and its default padding.

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

Home is the signed-in entry point. Home, Notes, Tasks, Calendar, and Settings appear in the
navigation island and rail. Home requests `GET /overview?date=YYYY-MM-DD` using today's local date
components. Home retains the welcome text/date, four latest task cards in a two-column grid, three
recently updated notes in a list, and an available period estimate. It reads `tasks.lastTasks` in
server order, including undated and completed tasks, without sorting by due date or filtering by
completion. Calendar previews, relationship information, count summaries, and quick access are not
displayed on Home. The relationship-start-date editor is a card in Couple settings. Home still uses
the overview's couple ID/member IDs to reconcile membership changes.

Home and `GET /periods?date=YYYY-MM-DD` share `PeriodSummary`. Availability is independent of
reliability: low-reliability dates remain visible as estimates. The current period model has one
`prediction` and its `nextPeriodWindow`, without legacy aliases or algorithm descriptions. Date-only
values remain `LocalDate` throughout requests and rendering.

Calendar requests `GET /calendar` with today's local `date` and the displayed month's `startDate`
and `endDate`. Its response is an object containing saved `entries`, `projections`, and
`prediction`. Saved entries keep their actual IDs and editing controls. Predicted period/ovulation
ranges are read-only, labelled estimates, and rendered separately in the month grid and selected-day
pane; they are never converted into editable records or assigned synthetic backend IDs. Calendar
caches include the reference date and requested range, clear with the session, and refresh after
mutations.

Home refreshes on resume, local-date changes, pull-to-refresh, and successful task, calendar, note,
or relationship mutations. It retains same-day content on refresh failures. Overview caches are
cleared on logout and account, couple, or member changes; results from an obsolete scope cannot
repopulate them. Greeting/date timers pause when Home is not resumed. Notes keep the same neutral
list rows on Home and the full Notes page. The Notes add button opens a blank editor through
`Route.CreateNote`. A new note stays a local draft until Create sends its title and Markdown content
together. Existing notes retain autosave and have an explicit Save button that flushes the draft
before closing. Creation/loading failures preserve drafts and prevent repeat submission. Notes
opened from Home start full-screen; those opened or created from Notes start split-view on larger
screens. The full-screen toggle remains available on larger screens from either origin; leaving
full-screen from Home adds the Notes list beneath the detail. Back from a standalone Home note
returns to Home. Creation and load failures retain the existing draft or cached content.

Tasks have their own destination showing all tasks, with no expansion control. Home reuses the same
task grid for its four newest tasks. Only task cards use stable, vivid colors and subtle rotations
derived from their IDs, with separate palettes for light and dark themes. A task morphs into the
full-screen `Route.Task` editor, retaining its card color while its rotation settles to zero. The
editor generates a local Material color scheme from its card background using the existing
MaterialKolor palette generator. The actions sheet starts with a responsibility and completion card
using the same rounded rows and small gap as the details group, followed by icon-led due-date,
creation-time, update-time, and delete rows. Both sheets show creation and update timestamps. Note
and task sheets share grouped-row styling and label/value spacing; delete rows use normal theme
colors. The description stays directly on the page. Notes and tasks share `EditorLayout`,
`EditorTextInput`, and `EditorBottomBar` for title, create/save controls, persistent loading, and
keyboard insets. The overflow button opens their respective actions sheets. Task actions include the
creation timestamp, completion, responsibility, due date, and confirmed deletion. Metadata edits
remain in the editor draft until Save. Notes and tasks share `ItemTransition`
and the same navigation transition scope. Navigation is a higher-z sibling outside the entire shared
transition layout. The island stays opaque while cards expand, then fades out, so animated content
cannot draw over the island or rail; notes retain their existing adaptive-pane behavior. The title
is editable in the app bar, with a plain description below. Forms support an optional due date,
shared or individual responsibility in a compact segmented control, and completion. Clearing a date
or assignee sends explicit JSON null; unchanged PATCH fields are omitted using `FieldChange`. Dates
remain
`LocalDate` values, including date-picker conversion and localized display. Character counters are
hidden while validation remains active. Saving remains in the bottom toolbar and deletion is a
normal sheet row using the shared confirmation sheet. Double-clicking a task completes or reopens
it; Space and an accessibility action provide the same behavior.

Notes remain neutral list rows on Home and in the full Notes destination. The three destinations
share refresh/loading layout and navigation padding; Home and Notes share list/error/empty-state
rendering. Relationship settings use `GET/PATCH /couples/me`; only an actual relationship start date
is used for days together, and future dates cannot be selected. Calendar forms and previews preserve
`isImportant`, including explicit false when clearing that flag.

Creation, navigation arrows, calendar controls, and note editor actions use the shared filled
`AppIconButton`, matching the Tasks create button. Surface rows own their padding once; task inputs
grow with their content instead of reserving empty description lines.

The task repository owns its own observable cache and serializes requests. Session invalidation
clears it immediately and advances a generation counter so in-flight requests from an old account or
couple cannot repopulate it. Successful mutations update cached tasks without clearing the list;
failures retain content and drafts, with operation-specific retries. Refresh manually to pick up a
partner’s changes; there is no polling or push synchronization. Task PATCH requests contain only
changed fields, and clearing a description sends an empty string. Backend field validation remains
attached to the editor fields. Requests use the current app language (`en` or `tr`) in
`Accept-Language`.

## Build

```sh
./gradlew :app:assembleDebug :app:lintDebug
```
