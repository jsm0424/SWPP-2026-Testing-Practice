# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `@BindingAdapter("app:dueDateLabel") fun setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Show a task's D-day label in its list item's `TextView`: the view displays `dueDateLabel(task.dueDate, today, task.isCompleted)` when that label is not empty, and displays no label otherwise. Because list item views are reused and re-bound when a task is checked or unchecked, every call must fully replace whatever the view showed before. "No label" means the view shows no text: its `text` is `""` or its `visibility` is not `View.VISIBLE`.

**Pre-conditions**
- Called on the main thread with a non-null `TextView` (in the app, the item's `due_date_text` through `app:dueDateLabel="@{task}"`).
- `task` is `null` until data binding sets the item's task.
- The same `TextView` may be passed again with a different or updated `Task`, because RecyclerView reuses item views and the item re-binds when its task changes.
- "Today" is the device's current local date, `LocalDate.now()`; tests build `dueDate` relative to it.

**Post-conditions**
- Success cases
  - A `null` task shows no label. `[Integration]`
    - Tip: Given a new `TextView` / When `setDueDateLabel(textView, null)` is called / Then `textView.text` is `""` or `textView.visibility != View.VISIBLE`.
  - An active task with no due date shows no label. `[Integration]`
    - Tip: Given a new `TextView` and `Task("Title", "Desc", isCompleted = false, dueDate = null)` / When `setDueDateLabel` is called / Then `textView.text` is `""` or `textView.visibility != View.VISIBLE`.
  - An active task due in 3 days shows `D-3`. `[Integration]`
    - Tip: Given a new `TextView` and an active task with `dueDate = LocalDate.now().plusDays(3)` / When `setDueDateLabel` is called / Then `textView.text` is `"D-3"` and `textView.visibility == View.VISIBLE`.
  - An active task overdue by 3 days shows `D+3`. `[Integration]`
    - Tip: Given a new `TextView` and an active task with `dueDate = LocalDate.now().minusDays(3)` / When `setDueDateLabel` is called / Then `textView.text` is `"D+3"` and `textView.visibility == View.VISIBLE`.
  - A completed task with no due date shows `CLEAR`. `[Integration]`
    - Tip: Given a new `TextView` and `Task("Title", "Desc", isCompleted = true, dueDate = null)` / When `setDueDateLabel` is called / Then `textView.text` is `"CLEAR"` and `textView.visibility == View.VISIBLE`.
  - A completed task with a due date shows `CLEAR`. `[Integration]`
    - Tip: Given a new `TextView` and a completed task with `dueDate = LocalDate.now().plusDays(3)` / When `setDueDateLabel` is called / Then `textView.text` is `"CLEAR"` and `textView.visibility == View.VISIBLE`.
- Error cases
  - None. Every `Task`, and `null`, is a valid input. `[Integration]`
- Edge cases
  - At the `D-Day` boundary between `D+N` and `D-N`: an active task due today shows `D-Day`. `[Integration]`
    - Tip: Given a new `TextView` and an active task with `dueDate = LocalDate.now()` / When `setDueDateLabel` is called / Then `textView.text` is `"D-Day"` and `textView.visibility == View.VISIBLE`.
  - Right next to the `D-Day` boundary on the overdue side: an active task due yesterday shows `D+1`. `[Integration]`
    - Tip: Given a new `TextView` and an active task with `dueDate = LocalDate.now().minusDays(1)` / When `setDueDateLabel` is called / Then `textView.text` is `"D+1"` and `textView.visibility == View.VISIBLE`.
  - Right next to the `D-Day` boundary on the future side: an active task due tomorrow shows `D-1`. `[Integration]`
    - Tip: Given a new `TextView` and an active task with `dueDate = LocalDate.now().plusDays(1)` / When `setDueDateLabel` is called / Then `textView.text` is `"D-1"` and `textView.visibility == View.VISIBLE`.
  - A due date more than a month ahead shows the full calendar-day count (any 32-day span crosses a month end). `[Integration]`
    - Tip: Given a new `TextView` and an active task with `dueDate = LocalDate.now().plusDays(32)` / When `setDueDateLabel` is called / Then `textView.text` is `"D-32"` and `textView.visibility == View.VISIBLE`.
  - Unchecking a completed task with no due date removes `CLEAR` (label shown → no label). `[Integration]`
    - Tip: Given a `TextView` already bound to `Task("Title", "Desc", isCompleted = true, id = "id1", dueDate = null)` and showing `"CLEAR"` / When `setDueDateLabel` is called again with the same task with `isCompleted = false` / Then `textView.text` is `""` or `textView.visibility != View.VISIBLE`.
  - Checking an active task with no due date shows `CLEAR` (no label → label shown). `[Integration]`
    - Tip: Given a `TextView` already bound to `Task("Title", "Desc", isCompleted = false, id = "id1", dueDate = null)` and showing no label / When `setDueDateLabel` is called again with the same task with `isCompleted = true` / Then `textView.text` is `"CLEAR"` and `textView.visibility == View.VISIBLE`.
  - Unchecking a completed task with a due date replaces `CLEAR` with its D-day label. `[Integration]`
    - Tip: Given a `TextView` already bound to a completed task with `dueDate = LocalDate.now().plusDays(3)` and showing `"CLEAR"` / When `setDueDateLabel` is called again with the same task with `isCompleted = false` / Then `textView.text` is `"D-3"` and `textView.visibility == View.VISIBLE`.
  - Checking an active task with a due date replaces its D-day label with `CLEAR`. `[Integration]`
    - Tip: Given a `TextView` already bound to an active task with `dueDate = LocalDate.now().plusDays(3)` and showing `"D-3"` / When `setDueDateLabel` is called again with the same task with `isCompleted = true` / Then `textView.text` is `"CLEAR"` and `textView.visibility == View.VISIBLE`.
  - Clearing the due date of an active task removes its D-day label. `[Integration]`
    - Tip: Given a `TextView` already bound to an active task with `dueDate = LocalDate.now().plusDays(3)` and showing `"D-3"` / When `setDueDateLabel` is called again with the same task with `dueDate = null` / Then `textView.text` is `""` or `textView.visibility != View.VISIBLE`.
  - A reused view re-bound to `null` shows no label. `[Integration]`
    - Tip: Given a `TextView` already bound to an active task with `dueDate = LocalDate.now().plusDays(3)` and showing `"D-3"` / When `setDueDateLabel(textView, null)` is called / Then `textView.text` is `""` or `textView.visibility != View.VISIBLE`.
  - On the task list screen, checking an active task with no due date shows `CLEAR` in its row. `[Integration]`
    - Tip: Given the repository holds only `Task("TITLE1", "DESCRIPTION1", isCompleted = false, id = "id1", dueDate = null)` and `TasksFragment` is launched / When the `complete_checkbox` in the `TITLE1` row is clicked / Then that row's `due_date_text` has text `"CLEAR"` and is displayed.
  - On the task list screen, checking and then unchecking an active task with no due date removes `CLEAR` from its row. `[Integration]`
    - Tip: Given the repository holds only `Task("TITLE1", "DESCRIPTION1", isCompleted = false, id = "id1", dueDate = null)` and `TasksFragment` is launched / When the `complete_checkbox` in the `TITLE1` row is clicked twice / Then that row's `due_date_text` has text `""` or is not visible.

**Undefined behavior** (optional)
- A test that runs across local midnight can see two different values of "today"; the label for that run is not defined.
