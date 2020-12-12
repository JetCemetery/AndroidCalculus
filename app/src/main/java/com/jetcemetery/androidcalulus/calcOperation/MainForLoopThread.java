package com.jetcemetery.androidcalulus.calcOperation;

import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

public class MainForLoopThread {
    private static final String TAG = "MainForLoopThread3";
    private final OperationValues data;
    private final Handler mainLoopHandler;
    private final int[] pointsArray;
    private ArrayList<SecondaryForLoop> runnableList;

    public MainForLoopThread(OperationValues data, Handler handler){
        //this is going to be the main loop caller
        //the soul purpose of this function is to take in the number of available CPUs
        //get the total number of
        this.data = data;
        this.mainLoopHandler = handler;
        pointsArray = createPointsArray(data);

    }

    public void StartProcess(){
        Log.d(TAG, "StartProcess called, total loop should be " + (pointsArray.length-1));
        runnableList = new ArrayList<>();
        for(int opCount=0; opCount < pointsArray.length-1; opCount++){
            //the goal plan for this is to create a new thread that passes the start/end values
            //run the START of that for loop, and make sure what executed in THAT loop is NOT run in thread mode
            createThread(pointsArray[opCount],pointsArray[opCount+1]);
        }
    }

    private void createThread(int start, int end) {
        //the objective of this thread loop is to create as many small tasks as possible
        //however start them only when there is room available
        //hence if this operation needs to create 500 operations but there are only 8 threads
        //create 8 threads and when one is finished, create the next one
        Log.d(TAG,"Starting new thread start val [" + start + "] end val [" + end + "]");

        Thread myThread = new Thread() {
            @Override
            public void run()
            {
                int endVal = end-1;
                for(int movingValue = start; movingValue < endVal; movingValue++){
                    SecondaryForLoop secondObj = new SecondaryForLoop(data, movingValue, mainLoopHandler);
                    runnableList.add(secondObj);
                    secondObj.run();
                }
            }
        };
        myThread.start();
        Log.d(TAG,"Stand alone thread has started");
    }

    private int[] createPointsArray(OperationValues data) {
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

    /*
    //comment out this function, but do not delete
    //may use it later at some point
    public void postHandlerMessage(String answer) {
        Message msg = mainLoopHandler.obtainMessage();
        msg.what = RenderValues.POST_MESSAGE_IN_RESULTS;
        Bundle bundle = new Bundle();
        bundle.putString(RenderValues.MESSAGE_NAME_ID, answer);
        msg.setData(bundle);
        mainLoopHandler.sendMessage(msg);
    }
     */

    public void pauseAllThreads() {
        Log.d(TAG,"At start of pausing");
        for (SecondaryForLoop curTh : runnableList){
            if(curTh != null){
                if(curTh.getState().equals(SecondaryForLoop.STATE_RUNNING)){
                    curTh.pause();
                }
                else if(curTh.getState().equals(SecondaryForLoop.STATE_NEW)){
                    //should never get here, but just in case you know
                    curTh.pause();
                }
            }
        }
    }

    public void resumeAllThreads() {
        Log.d(TAG,"At start of resuming");
        for (SecondaryForLoop curTh : runnableList){
            if(curTh != null){
                if(curTh.getState().equals(SecondaryForLoop.STATE_PAUSED)){
                    curTh.resume();
                }
            }
        }
    }

    public void stopAllThreads() {
        Log.d(TAG,"At start of Killing all threads");
        if(runnableList != null){
            if(runnableList.size() > 1){
                for (SecondaryForLoop curTh : runnableList){
                    if(curTh != null){
                        //will call a function that will gracefully kill the thread
                        //not really kill more like let the run method goto exit
                        curTh.gracefullExit();
                    }
                }
            }
            //set the runnable list to null
            //this intern will call the garbage collector for all the those threads inside of said list
            runnableList = null;
        }
    }
}
