package com.jetcemetery.androidcalulus.calcOperation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.jetcemetery.androidcalulus.RenderValues;

public class simplePost extends Thread {
    private Handler resultsArea;
    public simplePost(Handler resultsArea) {
        this.resultsArea = resultsArea;
    }

    public void run() {
        for(int i=0; i<2; i++){
            Message msg = resultsArea.obtainMessage();
            String msgStr = "my Simple Post int val == "+ i +"\n";
            Bundle bundle = new Bundle();
            bundle.putString(RenderValues.MESSAGE_NAME_ID, msgStr);
            //resultsArea.append("my int val == " + i + "\n")
            msg.setData(bundle);
            resultsArea.sendMessage(msg);

        }
    }
}
