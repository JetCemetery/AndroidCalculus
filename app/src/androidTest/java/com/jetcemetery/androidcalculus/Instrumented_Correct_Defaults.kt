package com.jetcemetery.androidcalculus

import androidx.test.espresso.Espresso.onView
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
class Instrumented_Correct_Defaults {
    //This UT will cover all of the default information that should be filled in for all of the views.
    //This will test both portrait and landscape views
    private val helper: UT_Helper = UT_Helper()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = getInstrumentation().targetContext
        assertEquals("com.jetcemetery.androidcalculus", appContext.getPackageName())
    }

    @Test
    fun InitialLoad_main_text() {
        //this UT makes sure that the default stuff is loaded into the editable fields
        helper.helper_startMainActivity()
        val defProgressTxt = "0 / 970299008"
        val defPhoneNum = "555-555-5555"

        onView(withId(R.id.txt_phoneID)).check(matches(withText(defPhoneNum)))
        onView(withId(R.id.txt_progress)).check(matches(withText(defProgressTxt)))

        onView(withId(R.id.txt_phoneID)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_start)).check(matches(isDisplayed()))
        onView(withId(R.id.pgBar_Progress_bar)).check(matches(isDisplayed()))

        onView(withId(R.id.errorText)).check(matches(not(isDisplayed())))
        onView(withId(R.id.btn_PauseResume)).check(matches(not(isDisplayed())))
        onView(withId(R.id.btn_stop)).check(matches(not(isDisplayed())))
    }
}
