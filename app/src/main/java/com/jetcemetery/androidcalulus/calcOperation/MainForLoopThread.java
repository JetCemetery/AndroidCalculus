package com.jetcemetery.androidcalulus.calcOperation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.jetcemetery.androidcalulus.MainActivityDataObj;
import com.jetcemetery.androidcalulus.RenderValues;

import java.util.ArrayList;
import java.util.List;

public class MainForLoopThread extends Thread {
    private static final String TAG = MainForLoopThread.class.getSimpleName();
    //private TextView resultsArea;
    private boolean showTimeOp;
    private Handler handler;
    int superCounter;

    public MainForLoopThread(final MainActivityDataObj data, Handler handler){
        //this will be the main thread separator
        //you need to create multiple loops based on start / end values of alpha
        //resultsArea = intObj;
        this.handler = handler;
        superCounter = 0;
        int[] pointsArray = createPointsArray(data);
        //TODO get time stamp?
        //showTimeOp = data.getBooleanDebugTime();

        for(int opCount=0; opCount < pointsArray.length-1; opCount++){
            //the goal plan for this is to create a new thread that passes the start/end values
            //run the START of that for loop, and make sure what executed in THAT loop is NOT run in thread mode
            superCounter++;
            createThread(pointsArray[opCount],pointsArray[opCount+1],data, opCount);
        }
    }

    @Override
    public void run(){
        while(superCounter !=0){
            try {
                Thread.sleep(5000);	//let it sleep for 5 seconds
            } catch (InterruptedException e) {
                String msgStr = "thread sleep error occurred, msg == " + e.getMessage();
                Message msg = handler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putString(RenderValues.MESSAGE_NAME_ID, msgStr);
                //resultsArea.append("my int val == " + i + "\n")
                msg.setData(bundle);
                handler.sendMessage(msg);
                e.printStackTrace();
            }
        }

        //TODO add the complete command
        //intObj.completed();
    }

    private void createThread(final int start, final int end, final MainActivityDataObj data, final int opCount) {
        //the objective of this thread loop is to create as many small tasks as possible
        //however start them only when there is room available
        //hence if this operation needs to create 500 operations but there are only 8 threads
        //create 8 threads and when one is finished, create the next one

        Runnable thisRunnable = new Runnable() {
            private long startTime = System.currentTimeMillis();
            public void run()
            {
                int startVal = start+0;
                int endVal = end-1;
                int operationNum = opCount+1;
                for(int movingValue = startVal; movingValue < endVal; movingValue++){
                    SecondaryForLoop seconObj = new SecondaryForLoop(data,movingValue,handler);
                    //seconObj.StartOperation();

                }
                String message = "***Completed thread number " + operationNum;
                //resultsArea.append(message);
                superCounter--;
            }
        };
        handler.post(thisRunnable);

        /*
        new Thread(new Runnable()
        {
            public void run()
            {
                int startVal = start+0;
                int endVal = end-1;
                int operationNum = opCount+1;
                for(int movingValue = startVal; movingValue < endVal; movingValue++){
                    SecondaryForLoop seconObj = new SecondaryForLoop(data,movingValue,handler);
                    seconObj.StartOperation();
                    if(showTimeOp){
                        //seconObj.showStartTime(movingValue, resultsArea);
                    }

                }
                String message = "***Completed thread number " + operationNum;
                //resultsArea.append(message);
                superCounter--;
            }
        }).start();
        */
        //TODO check if I need to post this message or not..
        /*8handler.post(new Runnable() {
            public void run() {
                resultsArea.append("Success initiating main thread!");
            }
        });*/
    }


    private int[] createPointsArray(MainActivityDataObj data) {
        //this method will create an array of int that will represent the start / end points for the for loops
        List<Integer> array = new ArrayList<Integer>();
        int startPt = data.alphaStart();
        //incrementValue == (x end - x start) / total number of threads
        int incrementValue = (int) Math.floor((data.alphaEnd() - data.alphaStart()) / data.getThreadNum());
        //Interesting issue here,
        //when x is from 0 to 1000 and you have 16 threads than means the increment value is going to be 62
        //that is going to be ALOT of



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

    public void add(String answer) {
        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString(RenderValues.MESSAGE_NAME_ID, answer);
        //resultsArea.append("my int val == " + i + "\n")
        msg.setData(bundle);
        handler.sendMessage(msg);
        //resultsArea.append(answer);
    }

}
