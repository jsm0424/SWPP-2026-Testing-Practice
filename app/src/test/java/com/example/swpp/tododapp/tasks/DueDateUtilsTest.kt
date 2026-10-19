package com.example.swpp.tododapp.tasks

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Tests for [dueDateLabel], one per Tip in specs/DueDateUtils.spec.md.
 */
class DueDateUtilsTest {

    // Success cases

    @Test
    fun dueDateLabel_completedWithFutureDueDate_returnsClear() {
        // GIVEN - A completed task due 2026-10-25, today is 2026-10-19
        val dueDate = LocalDate.of(2026, 10, 25)
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = true)

        // THEN - Completed tasks always show CLEAR
        assertEquals("CLEAR", label)
    }

    @Test
    fun dueDateLabel_completedWithNoDueDate_returnsClear() {
        // GIVEN - A completed task with no due date, today is 2026-10-19
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(null, today, isCompleted = true)

        // THEN - Completed tasks always show CLEAR
        assertEquals("CLEAR", label)
    }

    @Test
    fun dueDateLabel_completedPastDueDate_returnsClear() {
        // GIVEN - A completed task due 2026-10-10, today is 2026-10-19
        val dueDate = LocalDate.of(2026, 10, 10)
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = true)

        // THEN - CLEAR, not an overdue label
        assertEquals("CLEAR", label)
    }

    @Test
    fun dueDateLabel_activeWithNoDueDate_returnsEmpty() {
        // GIVEN - An active task with no due date, today is 2026-10-19
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(null, today, isCompleted = false)

        // THEN - No label
        assertEquals("", label)
    }

    @Test
    fun dueDateLabel_activeDueInFiveDays_returnsDMinus5() {
        // GIVEN - An active task due 2026-10-24, today is 2026-10-19
        val dueDate = LocalDate.of(2026, 10, 24)
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Due in 5 days
        assertEquals("D-5", label)
    }

    @Test
    fun dueDateLabel_activeOverdueByFiveDays_returnsDPlus5() {
        // GIVEN - An active task due 2026-10-14, today is 2026-10-19
        val dueDate = LocalDate.of(2026, 10, 14)
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Overdue by 5 days
        assertEquals("D+5", label)
    }

    // Edge cases: the D-Day boundary

    @Test
    fun dueDateLabel_activeDueToday_returnsDDay() {
        // GIVEN - An active task due 2026-10-19, today is 2026-10-19
        val dueDate = LocalDate.of(2026, 10, 19)
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Due today
        assertEquals("D-Day", label)
    }

    @Test
    fun dueDateLabel_activeDueYesterday_returnsDPlus1() {
        // GIVEN - An active task due 2026-10-18, today is 2026-10-19
        val dueDate = LocalDate.of(2026, 10, 18)
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Overdue by 1 day
        assertEquals("D+1", label)
    }

    @Test
    fun dueDateLabel_activeDueTomorrow_returnsDMinus1() {
        // GIVEN - An active task due 2026-10-20, today is 2026-10-19
        val dueDate = LocalDate.of(2026, 10, 20)
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Due in 1 day
        assertEquals("D-1", label)
    }

    // Edge cases: calendar boundaries

    @Test
    fun dueDateLabel_dueLastDayOfMonthFromDayBefore_returnsDMinus1() {
        // GIVEN - An active task due 2026-10-31, today is 2026-10-30
        val dueDate = LocalDate.of(2026, 10, 31)
        val today = LocalDate.of(2026, 10, 30)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Due in 1 day
        assertEquals("D-1", label)
    }

    @Test
    fun dueDateLabel_dueFirstDayOfNextMonth_returnsDMinus1() {
        // GIVEN - An active task due 2026-11-01, today is 2026-10-31
        val dueDate = LocalDate.of(2026, 11, 1)
        val today = LocalDate.of(2026, 10, 31)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Due in 1 day across the month end
        assertEquals("D-1", label)
    }

    @Test
    fun dueDateLabel_dueLastDayOfPreviousMonth_returnsDPlus1() {
        // GIVEN - An active task due 2026-10-31, today is 2026-11-01
        val dueDate = LocalDate.of(2026, 10, 31)
        val today = LocalDate.of(2026, 11, 1)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Overdue by 1 day across the month end
        assertEquals("D+1", label)
    }

    @Test
    fun dueDateLabel_dueNewYearsDayFromNewYearsEve_returnsDMinus1() {
        // GIVEN - An active task due 2027-01-01, today is 2026-12-31
        val dueDate = LocalDate.of(2027, 1, 1)
        val today = LocalDate.of(2026, 12, 31)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Due in 1 day across the year end
        assertEquals("D-1", label)
    }

    @Test
    fun dueDateLabel_dueNewYearsEveFromNewYearsDay_returnsDPlus1() {
        // GIVEN - An active task due 2026-12-31, today is 2027-01-01
        val dueDate = LocalDate.of(2026, 12, 31)
        val today = LocalDate.of(2027, 1, 1)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Overdue by 1 day across the year end
        assertEquals("D+1", label)
    }

    @Test
    fun dueDateLabel_acrossLeapFebruary_returnsDMinus2() {
        // GIVEN - An active task due 2028-03-01, today is 2028-02-28 (2028 has Feb 29)
        val dueDate = LocalDate.of(2028, 3, 1)
        val today = LocalDate.of(2028, 2, 28)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Feb 29 is counted
        assertEquals("D-2", label)
    }

    @Test
    fun dueDateLabel_acrossNonLeapFebruary_returnsDMinus1() {
        // GIVEN - An active task due 2027-03-01, today is 2027-02-28 (2027 has no Feb 29)
        val dueDate = LocalDate.of(2027, 3, 1)
        val today = LocalDate.of(2027, 2, 28)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Due in 1 day
        assertEquals("D-1", label)
    }

    @Test
    fun dueDateLabel_exactlyOneMonthAhead_returnsDMinus31() {
        // GIVEN - An active task due 2026-11-19, today is 2026-10-19 (October has 31 days)
        val dueDate = LocalDate.of(2026, 11, 19)
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Every calendar day is counted
        assertEquals("D-31", label)
    }

    @Test
    fun dueDateLabel_oneDayShortOfOneMonthAhead_returnsDMinus30() {
        // GIVEN - An active task due 2026-11-18, today is 2026-10-19
        val dueDate = LocalDate.of(2026, 11, 18)
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Due in 30 days
        assertEquals("D-30", label)
    }

    @Test
    fun dueDateLabel_oneDayPastOneMonthAhead_returnsDMinus32() {
        // GIVEN - An active task due 2026-11-20, today is 2026-10-19
        val dueDate = LocalDate.of(2026, 11, 20)
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Due in 32 days
        assertEquals("D-32", label)
    }

    @Test
    fun dueDateLabel_overdueByMoreThanOneMonth_returnsDPlus32() {
        // GIVEN - An active task due 2026-10-19, today is 2026-11-20
        val dueDate = LocalDate.of(2026, 10, 19)
        val today = LocalDate.of(2026, 11, 20)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Overdue by 32 days
        assertEquals("D+32", label)
    }

    @Test
    fun dueDateLabel_exactlyOneYearAhead_returnsDMinus365() {
        // GIVEN - An active task due 2027-10-19, today is 2026-10-19
        val dueDate = LocalDate.of(2027, 10, 19)
        val today = LocalDate.of(2026, 10, 19)

        // WHEN - Build the label
        val label = dueDateLabel(dueDate, today, isCompleted = false)

        // THEN - Every day of the year is counted
        assertEquals("D-365", label)
    }
}
