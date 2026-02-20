# Baynote Architecture Guide

## Project Structure

```
app/src/main/java/com/yuki/baynote/
├── MainActivity.kt                      # App entry point, hosts the NavGraph
├── data/
│   ├── BaynoteDatabase.kt              # Room database singleton
│   ├── dao/
│   │   ├── NoteDao.kt                  # Note database queries
│   │   ├── FolderDao.kt                # Folder database queries
│   │   └── TagDao.kt                   # Tag + note-tag link queries
│   └── model/
│       ├── Note.kt                     # Note entity + NoteWithTags relation
│       ├── Folder.kt                   # Folder entity + FolderWithChildren relation
│       ├── Tag.kt                      # Tag entity
│       └── NoteTagCrossRef.kt          # Many-to-many junction (note <-> tag)
├── ui/
│   ├── navigation/
│   │   └── BaynoteNavGraph.kt          # All screen routes and navigation wiring
│   ├── screen/
│   │   ├── notelist/
│   │   │   ├── NoteListViewModel.kt    # State + logic for note list / folder view
│   │   │   └── NoteListScreen.kt       # Home screen UI (list, drawer, search, FAB)
│   │   ├── noteedit/
│   │   │   ├── NoteEditViewModel.kt    # State + logic for creating/editing a note
│   │   │   ├── NoteEditScreen.kt       # Note editor UI (title + content fields)
│   │   │   ├── TableEditor.kt          # Editable table widget with formula support
│   │   │   ├── FormattingToolbar.kt    # Formatting toolbar + FormulaToolbar
│   │   │   ├── FormulaEvaluator.kt     # Formula parser/evaluator for table cells
│   │   │   ├── UndoRedoManager.kt      # Undo/redo state stack
│   │   │   └── markdown/               # Markdown parsing & visual transformation
│   │   └── components/
│   │       ├── NoteCard.kt             # Single note card (used in lists)
│   │       ├── FolderDrawerContent.kt  # Sidebar drawer with folder list
│   │       ├── CreateFolderDialog.kt   # "New Folder" popup dialog
│   │       └── MoveToFolderDialog.kt   # "Move to folder" picker dialog
│   └── theme/
│       ├── Color.kt                    # Color palette (light + dark)
│       ├── Theme.kt                    # Material 3 theme config + dynamic colors
│       └── Type.kt                     # Typography (font sizes, weights, spacing)
```

## What Each File Does

### Entry Point

| File | Purpose |
|------|---------|
| `MainActivity.kt` | Launches the app, applies the theme, creates the NavController, and renders `BaynoteNavGraph`. You rarely need to touch this. |

### Theme (change look and feel here)

| File | What to change |
|------|----------------|
| `theme/Color.kt` | Color values. `Purple80`/`PurpleGrey80`/`Pink80` = dark theme. `Purple40`/`PurpleGrey40`/`Pink40` = light theme. Rename or add your own colors here. |
| `theme/Theme.kt` | Which colors map to `primary`, `secondary`, `tertiary`, etc. Also controls whether dynamic colors (Android 12+ wallpaper-based) are used. Set `dynamicColor = false` to always use your custom palette. |
| `theme/Type.kt` | Font family, sizes, weights, line heights. Change `bodyLarge` to affect most text, `titleMedium` for card titles, `headlineSmall` for the note editor title field. |

### Navigation

| File | Purpose |
|------|---------|
| `navigation/BaynoteNavGraph.kt` | Defines all routes (`"notes"`, `"note/{noteId}"`, `"folder/{folderId}"`) and wires each route to its screen + ViewModel. **Add new screens here.** |

### Screens

| File | What it renders |
|------|-----------------|
| `notelist/NoteListScreen.kt` | The home screen: top bar, search bar, note list (LazyColumn), floating action button, and the navigation drawer. Also used for folder views (with a back arrow instead of the hamburger menu). |
| `notelist/NoteListViewModel.kt` | Provides `uiState` (notes, folders, search query) to the screen. Contains all actions: pin, delete, move, create folder, search. |
| `noteedit/NoteEditScreen.kt` | The note editor: borderless title + content text fields, pin button, delete button. Auto-saves when you press back. Manages `isFormulaMode` / `formulaInsertFn` state to swap the formatting toolbar for `FormulaToolbar` when a formula cell is focused. |
| `noteedit/NoteEditViewModel.kt` | Loads a note (or starts blank for new), tracks title/content changes, handles save and delete. |
| `noteedit/TableEditor.kt` | Renders and edits a table. Each cell is a `BasicTextField`. Supports formula cells (cells starting with `=`): shows a live result chip below the formula, accepts on space or focus loss, and intercepts taps on other cells to insert cell references instead of moving focus. Calls `onFormulaModeChange` to notify the parent screen when formula mode enters/exits. |
| `noteedit/FormattingToolbar.kt` | Contains two toolbars: `FormattingToolbar` (H1/H2/H3, Bold, Italic, Insert Table) and `FormulaToolbar` (+, -, ×, ÷, ✓). `NoteEditScreen` swaps between them based on `isFormulaMode`. |
| `noteedit/FormulaEvaluator.kt` | `object FormulaEvaluator` with `buildCellRef`, `parseCellRef`, `evaluate`, and `formatResult`. Private `ExpressionParser` does recursive-descent arithmetic (+, -, *, /) with correct precedence. Cell refs use A1 notation (col letter + 1-indexed row). Empty/non-numeric cells resolve to 0. |
| `noteedit/UndoRedoManager.kt` | Simple string-stack undo/redo manager used by `NoteEditScreen`. |

### Reusable Components

| File | What it is | Used by |
|------|------------|---------|
| `components/NoteCard.kt` | A Material card showing note title, content preview, date, and pin icon. Long-press opens a menu (pin, move, delete). | `NoteListScreen` |
| `components/FolderDrawerContent.kt` | The sidebar/drawer content: app title, "All Notes" link, folder list with delete buttons, "New Folder" button. | `NoteListScreen` |
| `components/CreateFolderDialog.kt` | A popup dialog with a text field to name a new folder. | `FolderDrawerContent` |
| `components/MoveToFolderDialog.kt` | A popup dialog listing all folders + "No folder" option to move a note. | `NoteListScreen` |

### Data Layer

| File | Purpose |
|------|---------|
| `data/BaynoteDatabase.kt` | Room database singleton. Call `BaynoteDatabase.getInstance(context)` to get DAOs. If you add a new entity, register it here and bump the `version`. |
| `data/model/Note.kt` | `Note` entity (title, content, folderId, isPinned, timestamps). Also contains `NoteWithTags` which loads a note with its tags attached. |
| `data/model/Folder.kt` | `Folder` entity (name, parentId for nesting). Also contains `FolderWithChildren`. |
| `data/model/Tag.kt` | `Tag` entity (name, usageCount). |
| `data/model/NoteTagCrossRef.kt` | Junction table linking notes to tags (many-to-many). |
| `data/dao/NoteDao.kt` | All note queries: insert, update, delete, get by ID, get all, get by folder, search. |
| `data/dao/FolderDao.kt` | All folder queries: insert, update, delete, get root folders, get children. |
| `data/dao/TagDao.kt` | Tag queries + note-tag linking: insert/delete cross refs, get tags for a note, increment/decrement usage. |

## How To: Common Tasks

### Change colors / theme
1. Edit `theme/Color.kt` to define your colors
2. Edit `theme/Theme.kt` to assign them to `lightColorScheme()`/`darkColorScheme()` slots (primary, secondary, background, surface, etc.)
3. Set `dynamicColor = false` in `Theme.kt` if you don't want Android 12 to override your colors

### Change fonts / typography
1. Edit `theme/Type.kt`
2. Override any Material 3 text style: `displayLarge`, `headlineMedium`, `titleSmall`, `bodyLarge`, `labelSmall`, etc.
3. Screens reference these via `MaterialTheme.typography.titleMedium`, etc.

### Change the note card appearance
Edit `components/NoteCard.kt`. The layout is:
- `Card` > `Column` > Row (title + pin icon), content preview text, date text
- Change padding, colors, card shape, elevation here

### Change the note editor appearance
Edit `noteedit/NoteEditScreen.kt`. The layout is:
- `Scaffold` > TopAppBar (back, pin, delete) + Column (title TextField, content TextField)
- The `transparentTextFieldColors()` function at the bottom controls the borderless look

### Change the home screen layout
Edit `notelist/NoteListScreen.kt`. Key areas:
- `SearchTopBar` composable = the search bar
- `NoteListContent` composable = the LazyColumn with pinned/others sections
- The `Scaffold` section = top bar, FAB placement

### Add a new screen
1. Create `ui/screen/yournewscreen/YourScreen.kt` (the composable)
2. Create `ui/screen/yournewscreen/YourViewModel.kt` (if it needs state/data)
3. Add a route in `navigation/BaynoteNavGraph.kt`:
   ```kotlin
   // In BaynoteRoutes object:
   const val YOUR_SCREEN = "yourscreen"

   // In the NavHost block:
   composable(BaynoteRoutes.YOUR_SCREEN) {
       val viewModel: YourViewModel = viewModel(
           factory = YourViewModel.factory(application)
       )
       YourScreen(viewModel = viewModel)
   }
   ```
4. Navigate to it from another screen: `navController.navigate(BaynoteRoutes.YOUR_SCREEN)`

### Add a new reusable component
1. Create a file in `ui/screen/components/YourComponent.kt`
2. Write a `@Composable fun YourComponent(...)` with the parameters it needs
3. Use it from any screen by calling `YourComponent(...)`

### Add a new database table
1. Create a data class in `data/model/` annotated with `@Entity`
2. Create a DAO interface in `data/dao/` annotated with `@Dao`
3. Register both in `data/BaynoteDatabase.kt`: add the entity to the `@Database(entities = [...])` list and add an `abstract fun yourDao(): YourDao`
4. **Increment the database `version`** (or uninstall the app to reset the DB during development)

## Architecture Pattern

```
Screen (Composable) ──observes──> ViewModel ──calls──> DAO ──queries──> Room DB
       │                              │
       │ user actions                 │ exposes StateFlow<UiState>
       └──────calls methods on────────┘
```

- **Screens** are pure UI: they render state and forward user actions to the ViewModel
- **ViewModels** hold state (`StateFlow`) and business logic, call DAOs in coroutines
- **DAOs** return `Flow<List<...>>` for reactive queries (UI auto-updates when data changes)
- No repository layer — ViewModels access DAOs directly via `BaynoteDatabase.getInstance()`

## Table Formula Support

Cells in a table that start with `=` enter formula mode.

### Cell reference notation
- Columns: A, B, C … (0-indexed → letter, max 26 columns)
- Rows: 1, 2, 3 … (0-indexed + 1)
- Example: `rows[1][2]` → `C2`

### Accept mechanisms
| Trigger | Behaviour |
|---------|-----------|
| Type space at end of valid formula | Cell replaced with computed result |
| Tap ✓ in FormulaToolbar | Sends a space → same path as above |
| Focus leaves the formula cell | Auto-accepted if result is valid; stays as formula text if invalid |

### Extending formulas
- Add new operators in `ExpressionParser` inside `FormulaEvaluator.kt`
- Multi-letter column support: update `buildCellRef` and `parseCellRef` (currently single A–Z only)
- Add function support (e.g. `SUM(A1:A3)`) by extending `parsePrimary` in `ExpressionParser`