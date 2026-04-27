package com.jetcemetery.androidcalculus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.jetcemetery.calculusPhoneNumber.activity.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.*;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private UT_Helper helper;

    public ExampleInstrumentedTest(){
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
    }

    @Test
    public void InitialLoad_main_UI_objects(){

        helper.helper_startMainActivity();
        helper_main_not_Running_visible_UI();
        //step two, check to see same button UI are visible/invisible during Orientation Change
        helper.helper_rotatePortrait();
        helper_main_not_Running_visible_UI();

    }

    @Test
    public void startStop(){
        helper.helper_startMainActivity();
        onView(withId(R.id.btn_start)).perform(click());
        onView(withId(R.id.btn_PauseResume)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_stop)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_stop)).perform(click());
    }

    private void helper_main_not_Running_visible_UI() {
        //this UT checks to see that all default buttons are viewable
        //things viewable:
        //  txt_phoneID
        //  btn_start
        //  pgBar_Progress_bar
        //  txt_progress
        //  txt_results
        //
        //things NOT viewable
        //  errorText
        //  btn_PauseResume
        //  btn_stop
        onView(withId(R.id.txt_phoneID)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_start)).check(matches(isDisplayed()));
        onView(withId(R.id.pgBar_Progress_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_results)).check(matches(isDisplayed()));

        onView(withId(R.id.errorText)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_PauseResume)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_stop)).check(matches(not(isDisplayed())));
    }
}