package com.jetcemetery.androidcalculus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.jetcemetery.calculusPhoneNumber.activity.MainActivity;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import static androidx.test.InstrumentationRegistry.getTargetContext;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

public class UT_Helper {

    public UT_Helper(){
        //do nothing constructor
    }

    public void helper_startMainActivity() {
        Context appContext = getInstrumentation().getTargetContext();
        Intent intent = new Intent(appContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(intent);
    }

    public void helper_rotatePortrait() {
        Activity myActivity = getActivityInstance();
        rotateScreen_Portrait(myActivity);
    }


    private void rotateScreen_Portrait(Activity activity) {
        //talen from https://stackoverflow.com/questions/37362200/how-to-rotate-activity-i-mean-screen-orientation-change-using-espresso
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final int orientation = getTargetContext()
                .getResources()
                .getConfiguration()
                .orientation;
        final int newOrientation = (orientation == Configuration.ORIENTATION_PORTRAIT) ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE :
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        activity.setRequestedOrientation(newOrientation);

        getInstrumentation().waitForIdle(new Runnable() {
            @Override
            public void run() {
                countDownLatch.countDown();
            }
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException("Screen rotation failed", e);
        }
    }

    private Activity getActivityInstance(){
        //copied from https://stackoverflow.com/questions/24517291/get-current-activity-in-espresso-android
        final Activity[] currentActivity = {null};

        getInstrumentation().runOnMainSync(new Runnable(){
            public void run(){
                Collection<Activity> resumedActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                Iterator<Activity> it = resumedActivity.iterator();
                currentActivity[0] = it.next();
            }
        });

        return currentActivity[0];
    }
}
