package com.jetcemetery.androidcalulus.calcOperation;

import android.os.Handler;

import java.util.ArrayList;

public class Singleton_MainLoop {
    private static final String TAG = "Singleton_MainLoop";
    public static String DATA_OBJ_NAME = "Singleton_MainLoop";
    private static Singleton_MainLoop instance;

//    private OperationValues data;
    private transient Handler mainLoopHandler;
    private int[] pointsArray;
    private ArrayList<SecondaryForLoop> runnableList;
    private static volatile boolean stopAllThreadsCalled = false;

    public static void initInstance()
    {
        if (instance == null)
        {
            // Create the instance
            instance = new Singleton_MainLoop();
            stopAllThreadsCalled = false;
        }
    }

    public void pauseAllThreads() {
//        Log.d(TAG,"At start of pausing");
        if(NoActiveThreads()){
            return;
        }
        for (SecondaryForLoop curTh : runnableList){
            if(curTh != null){
                if(curTh.getState().equals(SecondaryForLoop.STATE_RUNNING)){
//                    Log.d(TAG,"A thread was paused as it's state was running");
                    curTh.pause();
                }
                else if(curTh.getState().equals(SecondaryForLoop.STATE_NEW)){
                    //should never get here, but just in case you know
//                    Log.d(TAG,"A thread was paused as it's state was new");
                    curTh.pause();
                }
            }
        }
//        Log.d(TAG,"All threads pausing completed");
    }

    public void resumeAllThreads(Handler updateUIHandler) {
//        Log.d(TAG,"At start of resuming");
        this.mainLoopHandler = updateUIHandler;
        if(NoActiveThreads()){
            return;
        }
        for (SecondaryForLoop curTh : runnableList){
            if(curTh != null){
                if(curTh.getState().equals(SecondaryForLoop.STATE_PAUSED)){
                    curTh.resume(mainLoopHandler);
                }
            }
        }
    }

    private boolean NoActiveThreads() {
        boolean returningBool = false;
        if(runnableList == null){
            returningBool = true;
        }else if(runnableList.size() == 0){
            returningBool = true;
        }
        return returningBool;
    }

    public void stopAllThreads() {
//        Log.d(TAG,"At start of Killing all threads");
        stopAllThreadsCalled = true;
        if(runnableList != null){
            if(runnableList.size() > 1){
                for (SecondaryForLoop curTh : runnableList){
                    if(curTh != null){
                        //will call a function that will gracefully kill the thread
                        //not really kill more like let the run method goto exit
                        curTh.gracefulExit();
                    }
                }

            }
            //set the runnable list to null
            //this intern will call the garbage collector for all the those threads inside of said list
            runnableList = null;
        }
        instance = null;
    }

    public static Singleton_MainLoop getInstance()
    {
        return instance;
    }

    private Singleton_MainLoop()
    {
        // Constructor hidden because this is a singleton
    }

    public void MainForLoopThread(Singleton_OperationValues data, Handler handler){
        //this is going to be the main loop caller
        //the soul purpose of this function is to take in the number of available CPUs
        //get the total number of
//        this.data = data;
        this.mainLoopHandler = handler;
        pointsArray = createPointsArray(data);
    }

    public void StartProcess(Singleton_OperationValues data){
        // Log.d(TAG, "StartProcess called, total loop should be " + (pointsArray.length-1));
        runnableList = new ArrayList<>();
        for(int opCount=0; opCount < pointsArray.length-1; opCount++){
            //the goal plan for this is to create a new thread that passes the start/end values
            //run the START of that for loop, and make sure what executed in THAT loop is NOT run in thread mode
            createThread(pointsArray[opCount],pointsArray[opCount+1], data);
        }
    }

    private void createThread(int start, int end, Singleton_OperationValues data) {
        //the objective of this thread loop is to create as many small tasks as possible
        //however start them only when there is room available
        //hence if this operation needs to create 500 operations but there are only 8 threads
        //create 8 threads and when one is finished, create the next one
        //Log.d(TAG,"Starting new thread start val [" + start + "] end val [" + end + "]");

        Thread myThread = new Thread() {
            @Override
            public void run()
            {
                int endVal = end;
                for(int movingValue = start; movingValue < endVal; movingValue++){
                    SecondaryForLoop secondObj = new SecondaryForLoop(data, movingValue, mainLoopHandler);
                    if(runnableList == null){
                        //if here, then we PROBABLY set the stop on first success
                        //AND then we hit a section that posted a successful thing with a integral
                        break;
                    }
                    else if(!stopAllThreadsCalled){
                        runnableList.add(secondObj);
                        secondObj.run();
                    }

                }
            }
        };
        myThread.start();
        //Log.d(TAG,"Stand alone thread has started");
    }

    private int[] createPointsArray(Singleton_OperationValues data) {
        //this method shall break down the first integral by number of CPUs allocated
        //IE if there are 1000 operations and four cores, break the operation into
        //[1] 1 - 250
        //[2] 251 - 500
        //[3] 501 - 750
        //[4] 751 - 1000
        //in case the division isn't the best, let the last core have the most or least, it doesn't really matter

        int coresToUse = data.getThreadNum();
        int totalIterationRange = data.integral_1_End() - data.integral_1_Start();
        int incrementCount = totalIterationRange / coresToUse;
        //the array size should be cores to use + 1
        //the plus one is used to record last iteration point
        int [] returningArr = new int[coresToUse+1];
        int tempIntegralStartPoint = data.integral_1_Start();
        returningArr[0] = tempIntegralStartPoint;
        for(int i=1; i < coresToUse; i++){
            tempIntegralStartPoint += incrementCount;
            returningArr[i] = tempIntegralStartPoint;
        }
        returningArr[coresToUse] = data.integral_1_End();
        return returningArr;
    }

}
