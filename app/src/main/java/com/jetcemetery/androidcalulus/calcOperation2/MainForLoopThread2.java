package com.jetcemetery.androidcalulus.calcOperation2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.jetcemetery.androidcalulus.MainActivityDataObj;
import com.jetcemetery.androidcalulus.RenderValues;
import com.jetcemetery.androidcalulus.calcOperation.SecondaryForLoop;

import java.util.ArrayList;
import java.util.List;

public class MainForLoopThread2{
    private static String TAG = "MainForLoopThread2";
    private MainActivityDataObj data;
    private Handler mainLoopHandler;
    private int superCounter;
    private int[] pointsArray;
    private boolean pauseInEffect;

    public MainForLoopThread2(MainActivityDataObj data, Handler handler){
        //this is going to be the main loop caller
        //the soul purpose of this function is to take in the number of available CPUs
        //get the total number of
        this.data = data;
        this.mainLoopHandler = handler;
        superCounter = 0;
        pointsArray = createPointsArray(data);
        pauseInEffect = false;
        //TODO get time stamp?
        //showTimeOp = data.getBooleanDebugTime();

    }

    public void StartProcess(){
        Log.d(TAG, "StartProcess called, total loop should be " + pointsArray.length);
        for(int opCount=0; opCount < pointsArray.length-1; opCount++){
            //the goal plan for this is to create a new thread that passes the start/end values
            //run the START of that for loop, and make sure what executed in THAT loop is NOT run in thread mode
            superCounter++;
            createThread(pointsArray[opCount],pointsArray[opCount+1], opCount);
        }
    }

    private void createThread(int start, int end, final int opCount) {
        //the objective of this thread loop is to create as many small tasks as possible
        //however start them only when there is room available
        //hence if this operation needs to create 500 operations but there are only 8 threads
        //create 8 threads and when one is finished, create the next one
        Log.d(TAG,"Started new thread start val [" + start + "] end val [" + end + "]");

        Thread thread = new Thread() {
            private long startTime = System.currentTimeMillis();
            @Override
            public void run()
            {
                int startVal = start+0;
                int endVal = end-1;
                int operationNum = opCount+1;
                for(int movingValue = startVal; movingValue < endVal; movingValue++){
                    SecondaryForLoop2 secondObj = new SecondaryForLoop2(data, movingValue, mainLoopHandler);
                    secondObj.startProcess();
                    while(pauseInEffect){
                        SystemClock.sleep(1000);
                        Log.d(TAG,"pauseInEffect for 1 second");
                    }
                }
                superCounter--;
            }
        };
        thread.start();
        Log.d(TAG,"Stand alone thread has started");
    }

    private int[] createPointsArray(MainActivityDataObj data) {
        //this method shall break down the first integral by number of CPUs allocated
        //IE if there are 1000 operations and four cores, break the operation into
        //[1] 1 - 250
        //[2] 251 - 500
        //[3] 501 - 750
        //[4] 751 - 1000
        //in case the division isn't the best, let the last core have the most or least, it doesn't really matter

        int coresToUse = data.getThreadNum();
        int totalIterationRange = data.alphaEnd() - data.alphaStart();
        int incrementCount = totalIterationRange / coresToUse;
        //the array size should be cores to use + 1
        //the plus one is used to record last iteration point
        int [] returningArr = new int[coresToUse+1];
        int tempIntegralStartPoint = data.alphaStart();
        returningArr[0] = tempIntegralStartPoint;
        for(int i=1; i < coresToUse; i++){
            tempIntegralStartPoint += incrementCount;
            returningArr[i] = tempIntegralStartPoint;
        }
        returningArr[coresToUse] = data.alphaEnd();
        return returningArr;
    }

    private int[] createPointsArrayOld(MainActivityDataObj data) {
        //this method will create an array of int that will represent the start / end points for the for loops
        List<Integer> array = new ArrayList<Integer>();
        int startPt = data.alphaStart();
        //incrementValue == (x end - x start) / total number of threads
        int incrementValue = (int) Math.floor((data.alphaEnd() - data.alphaStart()) / data.getThreadNum());
        //Interesting issue here,
        //when x is from 0 to 1000 and you have 16 threads than means the increment value is going to be 62
        //that is going to be ALOT of
        Log.d(TAG, "Increment value == " + incrementValue);


        //the for loop below will create the breaking alpha end points
        for(int i=startPt; i < data.alphaEnd(); i = i + incrementValue){
            array.add(i);
        }

        //however we need to check if the end point was added!
        if(array.get(array.size()-1) != data.alphaEnd()){
            //if here, add the end point
            array.add(data.alphaEnd());
        }

        return convertIntArr(array);
    }

    private int[] convertIntArr(List<Integer> array){
        int[] retuningArray = new int[array.size()];
        int count = 0;
        for(int i : array)	{	retuningArray[count++] = i;	}
        return retuningArray;
    }

    public void postHandlerMessage(String answer) {
        Message msg = mainLoopHandler.obtainMessage();
        msg.what = RenderValues.POST_MESSAGE_IN_RESULTS;
        Bundle bundle = new Bundle();
        bundle.putString(RenderValues.MESSAGE_NAME_ID, answer);
        msg.setData(bundle);
        mainLoopHandler.sendMessage(msg);
    }

    public void pause() {
        this.pauseInEffect = true;
    }
}
