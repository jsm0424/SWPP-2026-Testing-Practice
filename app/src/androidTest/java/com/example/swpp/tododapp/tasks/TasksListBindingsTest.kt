package com.example.swpp.tododapp.tasks

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.swpp.tododapp.R
import com.example.swpp.tododapp.ServiceLocator
import com.example.swpp.tododapp.data.Task
import com.example.swpp.tododapp.data.source.FakeAndroidTestRepository
import com.example.swpp.tododapp.data.source.TasksRepository
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anyOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Tests for [setDueDateLabel], one per Tip in specs/TasksListBindings.spec.md.
 */
@MediumTest
@RunWith(AndroidJUnit4::class)
class TasksListBindingsTest {
    private lateinit var repository: TasksRepository

    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()

    @Before
    fun initRepository() {
        repository = FakeAndroidTestRepository()
        ServiceLocator.tasksRepository = repository
    }

    @After
    fun cleanupDb() = runTest {
        ServiceLocator.resetRepository()
    }

    // Success cases

    @Test
    fun setDueDateLabel_nullTask_showsNoLabel() {
        // GIVEN - A new TextView
        val textView = newTextView()

        // WHEN - Bind a null task
        bind(textView, null)

        // THEN - No label is shown
        assertNoLabel(textView)
    }

    @Test
    fun setDueDateLabel_activeWithNoDueDate_showsNoLabel() {
        // GIVEN - A new TextView and an active task with no due date
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = false, dueDate = null)

        // WHEN - Bind the task
        bind(textView, task)

        // THEN - No label is shown
        assertNoLabel(textView)
    }

    @Test
    fun setDueDateLabel_activeDueInThreeDays_showsDMinus3() {
        // GIVEN - A new TextView and an active task due in 3 days
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = false, dueDate = LocalDate.now().plusDays(3))

        // WHEN - Bind the task
        bind(textView, task)

        // THEN - D-3 is shown
        assertLabel("D-3", textView)
    }

    @Test
    fun setDueDateLabel_activeOverdueByThreeDays_showsDPlus3() {
        // GIVEN - A new TextView and an active task overdue by 3 days
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = false, dueDate = LocalDate.now().minusDays(3))

        // WHEN - Bind the task
        bind(textView, task)

        // THEN - D+3 is shown
        assertLabel("D+3", textView)
    }

    @Test
    fun setDueDateLabel_completedWithNoDueDate_showsClear() {
        // GIVEN - A new TextView and a completed task with no due date
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = true, dueDate = null)

        // WHEN - Bind the task
        bind(textView, task)

        // THEN - CLEAR is shown
        assertLabel("CLEAR", textView)
    }

    @Test
    fun setDueDateLabel_completedWithDueDate_showsClear() {
        // GIVEN - A new TextView and a completed task due in 3 days
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = true, dueDate = LocalDate.now().plusDays(3))

        // WHEN - Bind the task
        bind(textView, task)

        // THEN - CLEAR is shown
        assertLabel("CLEAR", textView)
    }

    // Edge cases: the D-Day boundary

    @Test
    fun setDueDateLabel_activeDueToday_showsDDay() {
        // GIVEN - A new TextView and an active task due today
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = false, dueDate = LocalDate.now())

        // WHEN - Bind the task
        bind(textView, task)

        // THEN - D-Day is shown
        assertLabel("D-Day", textView)
    }

    @Test
    fun setDueDateLabel_activeDueYesterday_showsDPlus1() {
        // GIVEN - A new TextView and an active task due yesterday
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = false, dueDate = LocalDate.now().minusDays(1))

        // WHEN - Bind the task
        bind(textView, task)

        // THEN - D+1 is shown
        assertLabel("D+1", textView)
    }

    @Test
    fun setDueDateLabel_activeDueTomorrow_showsDMinus1() {
        // GIVEN - A new TextView and an active task due tomorrow
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = false, dueDate = LocalDate.now().plusDays(1))

        // WHEN - Bind the task
        bind(textView, task)

        // THEN - D-1 is shown
        assertLabel("D-1", textView)
    }

    @Test
    fun setDueDateLabel_activeDueIn32Days_showsDMinus32() {
        // GIVEN - A new TextView and an active task due in 32 days (always crosses a month end)
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = false, dueDate = LocalDate.now().plusDays(32))

        // WHEN - Bind the task
        bind(textView, task)

        // THEN - The full calendar-day count is shown
        assertLabel("D-32", textView)
    }

    // Edge cases: re-binding a reused view

    @Test
    fun setDueDateLabel_uncheckCompletedWithNoDueDate_removesClear() {
        // GIVEN - A TextView bound to a completed task with no due date, showing CLEAR
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = true, id = "id1", dueDate = null)
        bind(textView, task)
        assertLabel("CLEAR", textView)

        // WHEN - Re-bind the same task, now unchecked
        bind(textView, task.copy(isCompleted = false))

        // THEN - No label is shown
        assertNoLabel(textView)
    }

    @Test
    fun setDueDateLabel_checkActiveWithNoDueDate_showsClear() {
        // GIVEN - A TextView bound to an active task with no due date, showing no label
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = false, id = "id1", dueDate = null)
        bind(textView, task)
        assertNoLabel(textView)

        // WHEN - Re-bind the same task, now checked
        bind(textView, task.copy(isCompleted = true))

        // THEN - CLEAR is shown
        assertLabel("CLEAR", textView)
    }

    @Test
    fun setDueDateLabel_uncheckCompletedWithDueDate_showsDMinus3() {
        // GIVEN - A TextView bound to a completed task due in 3 days, showing CLEAR
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = true, id = "id1", dueDate = LocalDate.now().plusDays(3))
        bind(textView, task)
        assertLabel("CLEAR", textView)

        // WHEN - Re-bind the same task, now unchecked
        bind(textView, task.copy(isCompleted = false))

        // THEN - CLEAR is replaced with D-3
        assertLabel("D-3", textView)
    }

    @Test
    fun setDueDateLabel_checkActiveWithDueDate_showsClear() {
        // GIVEN - A TextView bound to an active task due in 3 days, showing D-3
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = false, id = "id1", dueDate = LocalDate.now().plusDays(3))
        bind(textView, task)
        assertLabel("D-3", textView)

        // WHEN - Re-bind the same task, now checked
        bind(textView, task.copy(isCompleted = true))

        // THEN - D-3 is replaced with CLEAR
        assertLabel("CLEAR", textView)
    }

    @Test
    fun setDueDateLabel_clearDueDateOfActiveTask_removesLabel() {
        // GIVEN - A TextView bound to an active task due in 3 days, showing D-3
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = false, id = "id1", dueDate = LocalDate.now().plusDays(3))
        bind(textView, task)
        assertLabel("D-3", textView)

        // WHEN - Re-bind the same task with its due date cleared
        bind(textView, task.copy(dueDate = null))

        // THEN - No label is shown
        assertNoLabel(textView)
    }

    @Test
    fun setDueDateLabel_rebindToNull_removesLabel() {
        // GIVEN - A TextView bound to an active task due in 3 days, showing D-3
        val textView = newTextView()
        val task = Task("Title", "Desc", isCompleted = false, id = "id1", dueDate = LocalDate.now().plusDays(3))
        bind(textView, task)
        assertLabel("D-3", textView)

        // WHEN - Re-bind the view to null
        bind(textView, null)

        // THEN - No label is shown
        assertNoLabel(textView)
    }

    // Edge cases: the task list screen

    @Test
    fun tasksScreen_checkActiveWithNoDueDate_showsClearInRow() = runTest {
        // GIVEN - The task list shows one active task with no due date
        repository.saveTask(Task("TITLE1", "DESCRIPTION1", isCompleted = false, id = "id1", dueDate = null))
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)

        // WHEN - Check the task
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("TITLE1")))).perform(click())

        // THEN - Its row shows CLEAR
        onView(allOf(withId(R.id.due_date_text), hasSibling(withText("TITLE1"))))
            .check(matches(allOf(withText("CLEAR"), isDisplayed())))
    }

    @Test
    fun tasksScreen_checkThenUncheckActiveWithNoDueDate_removesClearFromRow() = runTest {
        // GIVEN - The task list shows one active task with no due date
        repository.saveTask(Task("TITLE1", "DESCRIPTION1", isCompleted = false, id = "id1", dueDate = null))
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)

        // WHEN - Check the task, then uncheck it
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("TITLE1")))).perform(click())
        onView(allOf(withId(R.id.complete_checkbox), hasSibling(withText("TITLE1")))).perform(click())

        // THEN - Its row shows no label
        onView(allOf(withId(R.id.due_date_text), hasSibling(withText("TITLE1"))))
            .check(matches(anyOf(withText(""), not(isDisplayed()))))
    }

    private fun newTextView(): TextView {
        lateinit var textView: TextView
        instrumentation.runOnMainSync { textView = TextView(instrumentation.targetContext) }
        return textView
    }

    private fun bind(textView: TextView, task: Task?) {
        instrumentation.runOnMainSync { setDueDateLabel(textView, task) }
    }

    private fun assertLabel(expected: String, textView: TextView) {
        assertEquals(expected, textView.text.toString())
        assertEquals(View.VISIBLE, textView.visibility)
    }

    private fun assertNoLabel(textView: TextView) {
        assertTrue(
            "Expected no label, but text was \"${textView.text}\" with visibility ${textView.visibility}",
            textView.text.isEmpty() || textView.visibility != View.VISIBLE
        )
    }
}
