# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `internal fun dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Build the D-day label shown next to a task in the task list. A completed task always shows `CLEAR`; an active task with no due date shows nothing (`""`); otherwise the label is `D-N` (due in N days), `D-Day` (due today) or `D+N` (overdue by N days), where N is the number of calendar days between `today` and `dueDate`.

**Pre-conditions**
- `today` is the caller's current local date; the function never reads the clock itself, so the result depends only on its three arguments.
- `dueDate` is `null` when the task has no due date.

**Post-conditions**
- Success cases
  - A completed task with a future due date returns `"CLEAR"`. `[Unit]`
    - Tip: Given `dueDate = 2026-10-25`, `today = 2026-10-19`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`.
  - A completed task with no due date returns `"CLEAR"`. `[Unit]`
    - Tip: Given `dueDate = null`, `today = 2026-10-19`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`.
  - A completed task that is past its due date returns `"CLEAR"`, not an overdue label. `[Unit]`
    - Tip: Given `dueDate = 2026-10-10`, `today = 2026-10-19`, `isCompleted = true` / When `dueDateLabel` is called / Then it returns `"CLEAR"`.
  - An active task with no due date returns `""`. `[Unit]`
    - Tip: Given `dueDate = null`, `today = 2026-10-19`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `""`.
  - An active task due in N days returns `"D-N"`. `[Unit]`
    - Tip: Given `dueDate = 2026-10-24`, `today = 2026-10-19`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-5"`.
  - An active task overdue by N days returns `"D+N"`. `[Unit]`
    - Tip: Given `dueDate = 2026-10-14`, `today = 2026-10-19`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+5"`.
- Error cases
  - None. Every combination of `dueDate`, `today` and `isCompleted` is valid.
- Edge cases
  - At the `D-Day` boundary between `D+N` and `D-N`: an active task due today returns `"D-Day"`. `[Unit]`
    - Tip: Given `dueDate = 2026-10-19`, `today = 2026-10-19`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-Day"`.
  - Right next to the `D-Day` boundary on the overdue side: an active task due yesterday returns `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = 2026-10-18`, `today = 2026-10-19`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`.
  - Right next to the `D-Day` boundary on the future side: an active task due tomorrow returns `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = 2026-10-20`, `today = 2026-10-19`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`.
  - Right before a month end, within the same month: due the last day of the month is `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = 2026-10-31`, `today = 2026-10-30`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`.
  - Across a month end, N counts calendar days: due the first day of next month is `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = 2026-11-01`, `today = 2026-10-31`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`.
  - Across a month end, overdue: due the last day of the previous month is `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = 2026-10-31`, `today = 2026-11-01`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`.
  - Across a year end: due New Year's Day from New Year's Eve is `"D-1"`. `[Unit]`
    - Tip: Given `dueDate = 2027-01-01`, `today = 2026-12-31`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`.
  - Across a year end, overdue: due New Year's Eve, checked on New Year's Day, is `"D+1"`. `[Unit]`
    - Tip: Given `dueDate = 2026-12-31`, `today = 2027-01-01`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+1"`.
  - Across February in a leap year, Feb 29 is counted. `[Unit]`
    - Tip: Given `dueDate = 2028-03-01`, `today = 2028-02-28`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-2"`.
  - Across February in a non-leap year, there is no Feb 29. `[Unit]`
    - Tip: Given `dueDate = 2027-03-01`, `today = 2027-02-28`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-1"`.
  - Exactly one calendar month ahead (31 days in October) counts every day, not the day-of-month difference. `[Unit]`
    - Tip: Given `dueDate = 2026-11-19`, `today = 2026-10-19`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-31"`.
  - One day short of one calendar month ahead. `[Unit]`
    - Tip: Given `dueDate = 2026-11-18`, `today = 2026-10-19`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-30"`.
  - One day past one calendar month ahead. `[Unit]`
    - Tip: Given `dueDate = 2026-11-20`, `today = 2026-10-19`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-32"`.
  - Overdue by more than one month counts every day. `[Unit]`
    - Tip: Given `dueDate = 2026-10-19`, `today = 2026-11-20`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D+32"`.
  - Exactly one year ahead counts every day of the year. `[Unit]`
    - Tip: Given `dueDate = 2027-10-19`, `today = 2026-10-19`, `isCompleted = false` / When `dueDateLabel` is called / Then it returns `"D-365"`.

**Undefined behavior** (optional)
- None.
