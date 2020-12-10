package com.jetcemetery.androidcalulus.calcOperation3.calcOperation2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jetcemetery.androidcalulus.OperationValues;
import com.jetcemetery.androidcalulus.activity.MainActivity_take2;
import com.jetcemetery.androidcalulus.calcOperation.PrintCalc;
import com.jetcemetery.androidcalulus.calcOperation.SingleMathOp;
import com.jetcemetery.androidcalulus.helper.getRandomInRange;

public class SecondaryForLoop3 {
    private static String TAG = "SecondaryForLoop3";
    private final OperationValues userInput;
    private final int movingValue;
    private final Handler handler;
    private final long targetNumber;
    private final int range;
    private final boolean operatingInBatchMode;
    private int currentBatchAmountRange;
    private int currentBatchAmount;


    public SecondaryForLoop3(OperationValues data, int movingValue, Handler handler) {
        this.userInput = data;
        this.movingValue = movingValue;
        this.handler = handler;
        this.targetNumber = data.getNumber();
        this.range = 1000;
        currentBatchAmount = 0;
        currentBatchAmountRange = getRandomInRange.getRandomNumberInRange(100, 1000);
        operatingInBatchMode = true;
    }

    public void startProcess() {
        //this function will need some kind of optimization
        //see https://howtodoinjava.com/java/collections/performance-comparison-of-different-for-loops-in-java/
        //according to that, we need to reduce the number of function calls, so I am, and here it is
        short movingIntegral_2 = (short) userInput.betaStart();
        short end_Integral_2 = (short)userInput.betaEnd();
        short movingIntegral_3 = (short)userInput.gammaStart();
        short end_Integral_3 = (short)userInput.gammaEnd();
        short rangeStart = (short) userInput.xStart();
        short rangeEnd = (short) userInput.xEnd();
        Log.d(TAG,"Starting secondary for loop now");
        for(short movingBeta = movingIntegral_2; movingBeta<end_Integral_2; movingBeta++){
            for(short movingGamma = movingIntegral_3; movingGamma<end_Integral_3; movingGamma++) {
                for (short movingLowerLimit = rangeStart; movingLowerLimit < rangeEnd - 1; movingLowerLimit++) {
                    SingleMathOp lowerLimitCalc = new SingleMathOp(movingLowerLimit, movingValue, movingBeta, movingGamma);
                    lowerLimitCalc.runMath();
                    long lowerLimitValue = lowerLimitCalc.getDervivate();
                    for (short movingUpperLimit = (short) (movingLowerLimit + 1); movingUpperLimit < rangeEnd; movingUpperLimit++) {
                        SingleMathOp mathOp = new SingleMathOp(movingUpperLimit, movingValue, movingBeta, movingGamma);
                        mathOp.runMath();
                        long upperLimitValue = mathOp.getDervivate();
                        long diff = upperLimitValue - lowerLimitValue;
                        if (Math.abs(targetNumber - diff) < range) {
                            //if here then number is within range!

                            long dividend = targetNumber - diff;
                            String phoneNumberTxt = PrintCalc.PrintCalcObjPass(movingLowerLimit, movingUpperLimit, movingValue, movingBeta, movingGamma, userInput.getNumber(), dividend);
                            Log.d(TAG,"****SUCCESS*********" + phoneNumberTxt);
                            postMessage(phoneNumberTxt + "\n");
                        }
                    }
                    postCompleteOperation();
                }
            }
        }
        if(operatingInBatchMode){
            //if we were operating in batch mode, we need to clear out what left, and post it into the UI filed
            //to do so we call the normal postCompleteOperationBatch operation, but pass true in the parameter field
            //yup that's all
            postCompleteOperationBatch(true);
        }
    }

    private void postCompleteOperation() {
        //we have two methods of posting an operation, either solo or batch
        //make the call the same, and figure which mode later, or right below
        if(operatingInBatchMode){
            postCompleteOperationBatch(false);
        }
        else{
            postCompleteOperationSingle();
        }
    }
    private void postCompleteOperationSingle() {
        //there was an issue with the main UI thread
        //calling this update UI function a few thousand times per second broke the UI thread
        //so now we must Make with call as Batch!
        Message msg = handler.obtainMessage();
        msg.what = MainActivity_take2.ONE_CALULATION_COMPLETED_BATCH;
        handler.sendMessage(msg);
    }

    private void postCompleteOperationBatch(boolean forceSend) {
        //this method was WAY to taxing on the main thread
        //need to do this stuff in a batch kind of mode, see new method
        //include a boolean called forced send
        //this means after the current integral for loop is completed, there probably is left over stuff
        //saved in this batch mode
        //go ahead and send the remainder to the UI thread to update and show true and accurate count
        //of the number of operation completed
        currentBatchAmount++;
        if(currentBatchAmount >= currentBatchAmountRange || forceSend){
            if(forceSend){
                //if here, then decrement the count by one, we didn't really finish another operation
                //we are here to clear out our queue.
                currentBatchAmount--;
            }
            Message msg = handler.obtainMessage();
            msg.what = MainActivity_take2.ONE_CALULATION_COMPLETED_BATCH;
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity_take2.MESSAGE_NAME_ID2, String.valueOf(currentBatchAmount));
            msg.setData(bundle);
            handler.sendMessage(msg);
            //to mimic authenticity, set the range amount to some random number
            //it's faking it I know, but that's okay, at least app does not crash anymore.
            currentBatchAmount = 0;
            currentBatchAmountRange = getRandomInRange.getRandomNumberInRange(100, 1000);
        }

    }

    private void postMessage(String msgStr) {
        //normal post message operation
        Message msg = handler.obtainMessage();
        msg.what = MainActivity_take2.POST_MESSAGE_IN_RESULTS2;
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity_take2.MESSAGE_NAME_ID2, msgStr);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

}
