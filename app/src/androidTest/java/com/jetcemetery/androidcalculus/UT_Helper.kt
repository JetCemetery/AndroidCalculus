package com.jetcemetery.androidcalculus

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.jetcemetery.calculusPhoneNumber.activity.MainActivity
import java.util.concurrent.CountDownLatch

class UT_Helper {

    fun helper_startMainActivity() {
        val appContext = getInstrumentation().targetContext
        val intent = Intent(appContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        appContext.startActivity(intent)
    }

    fun helper_rotatePortrait() {
        val myActivity = getActivityInstance()
        rotateScreen_Portrait(myActivity)
    }

    private fun rotateScreen_Portrait(activity: Activity) {
        val countDownLatch = CountDownLatch(1)
        val orientation = getInstrumentation().targetContext
            .resources
            .configuration
            .orientation
        val newOrientation = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        activity.requestedOrientation = newOrientation

        getInstrumentation().waitForIdle {
            countDownLatch.countDown()
        }

        try {
            countDownLatch.await()
        } catch (e: InterruptedException) {
            throw RuntimeException("Screen rotation failed", e)
        }
    }

    private fun getActivityInstance(): Activity {
        val currentActivity = arrayOfNulls<Activity>(1)

        getInstrumentation().runOnMainSync {
            val resumedActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
            val it = resumedActivity.iterator()
            currentActivity[0] = it.next()
        }

        return currentActivity[0]!!
    }
}
