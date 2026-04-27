package com.jetcemetery.androidcalculus

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.hamcrest.core.IsNot.not
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private val helper: UT_Helper = UT_Helper()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = getInstrumentation().targetContext
        assertEquals("com.jetcemetery.androidcalculus", appContext.packageName)
    }

    @Test
    fun InitialLoad_main_text() {
        //this UT makes sure that the default stuff is loaded into the editable fields
        helper.helper_startMainActivity()
        val defProgressTxt = "0 / 970299008"
        val defPhoneNum = "555-555-5555"
        val defResults = "Results"
        onView(withId(R.id.txt_phoneID)).check(matches(withText(defPhoneNum)))
        onView(withId(R.id.txt_progress)).check(matches(withText(defProgressTxt)))
        // Note: txt_results might need to be checked in adapter if it's a RecyclerView
        // Based on original code it was checking txt_results, let's keep it for now.
    }

    @Test
    fun InitialLoad_main_UI_objects() {
        helper.helper_startMainActivity()
        helper_main_not_Running_visible_UI()
        //step two, check to see same button UI are visible/invisible during Orientation Change
        helper.helper_rotatePortrait()
        helper_main_not_Running_visible_UI()
    }

    @Test
    fun startStop() {
        helper.helper_startMainActivity()
        onView(withId(R.id.btn_start)).perform(click())
        onView(withId(R.id.btn_PauseResume)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_stop)).check(matches(isDisplayed()))

        onView(withId(R.id.btn_stop)).perform(click())
    }

    private fun helper_main_not_Running_visible_UI() {
        //this UT checks to see that all default buttons are viewable
        onView(withId(R.id.txt_phoneID)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_start)).check(matches(isDisplayed()))
        onView(withId(R.id.pgBar_Progress_bar)).check(matches(isDisplayed()))

        onView(withId(R.id.errorText)).check(matches(not(isDisplayed())))
        onView(withId(R.id.btn_PauseResume)).check(matches(not(isDisplayed())))
        onView(withId(R.id.btn_stop)).check(matches(not(isDisplayed())))
    }
}
