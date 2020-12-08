package com.jetcemetery.androidcalulus.calcOperation;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.TextView;

public class testHandlerThreadOp extends HandlerThread {
    private static final String TAG = testHandlerThreadOp.class.getSimpleName();
    private TextView resultsArea;
    private Handler myHandle;

    public testHandlerThreadOp(TextView resultsArea){
        super(TAG);


    }

    public synchronized void startProcess(){
        myHandle = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                resultsArea.append("Do you see me START?\n");
            }
        };
    }

    public synchronized void createMessages(){
        for(int i=0; i < 2; i++){
            //myHandle.
        }
    }
}
