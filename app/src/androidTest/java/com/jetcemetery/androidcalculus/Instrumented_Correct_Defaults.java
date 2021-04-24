package com.jetcemetery.androidcalculus;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class Instrumented_Correct_Defaults {
    //This UT will cover all of the default information that should be filled in for all of the views.
    //This will test both portrait and landscape views
    private UT_Helper helper;

    public Instrumented_Correct_Defaults(){
        helper = new UT_Helper();
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = getInstrumentation().getTargetContext();
        assertEquals("com.jetcemetery.androidcalculus", appContext.getPackageName());
    }

    @Test
    public void InitialLoad_main_text(){
        //this UT makes sure that the default stuff is loaded into the editable fields
        helper.helper_startMainActivity();
        String def_progressTxt = "0 / 970299008";
        String def_phoneNum = "555-555-5555";
        String def_results = "Results";

        onView(withId(R.id.txt_phoneID)).check(matches(withText(def_phoneNum)));
        onView(withId(R.id.txt_progress)).check(matches(withText(def_progressTxt)));
        onView(withId(R.id.txt_results)).check(matches(withText(def_results)));

        onView(withId(R.id.txt_phoneID)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_start)).check(matches(isDisplayed()));
        onView(withId(R.id.pgBar_Progress_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_results)).check(matches(isDisplayed()));

        onView(withId(R.id.errorText)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_PauseResume)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_stop)).check(matches(not(isDisplayed())));

    }

}
