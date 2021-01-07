package com.jetcemetery.androidcalculus;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.jetcemetery.calculusPhoneNumber.activity.MainActivity;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.jetcemetery.androidcalculus", appContext.getPackageName());
    }

    @Test
    public void InitialLoad(){
        //this UT makes sure that the default stuff is loaded
//        EditText ut_txt_phoneID = findViewById(R.id.txt_phoneID);

//        assertEquals(ut_txt_phoneID.toString(), "555-555-5555");

//        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        Intent intent = new Intent(appContext, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        appContext.startActivity(intent);
//        //onView(withId(R.id.btn_start)).check(assertEquals();)
//        //onView().check()
//        //txt_phoneID
    }

    @Test
    public void startStop(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(intent);
        onView(withId(R.id.btn_start)).perform(click());
        onView(withId(R.id.btn_PauseResume)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_stop)).check(matches(isDisplayed()));

    }
}