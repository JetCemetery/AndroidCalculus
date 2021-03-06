package com.jetcemetery.calculusPhoneNumber.calcOperation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jetcemetery.calculusPhoneNumber.activity.MainActivity;
import com.jetcemetery.calculusPhoneNumber.helper.getRandomInRange;

public class SecondaryForLoop implements Runnable {
    //for enabling pause resume section
    //--------------
    private volatile boolean pauseWork = false;
    private volatile String state = STATE_NEW;
    //--------------
    private static final String TAG = "SecondaryForLoop";
    public static String STATE_NEW = "New";
    public static String STATE_PAUSED = "Paused";
    public static String STATE_RUNNING = "Running";
    public static String STATE_FINISHED = "Finished";
    private final Singleton_OperationValues userInput;
    private final int first_integral_value;
    private Handler handler;
    private final long targetNumber;
    private final int range;
    private final boolean operatingInBatchMode;
    private int currentBatchAmountRange;
    private int currentBatchAmount;
    private volatile boolean stopProcess;// = false;

    public SecondaryForLoop(Singleton_OperationValues data, int first_integral_value, Handler handler) {
        this.userInput = data;
        this.first_integral_value = first_integral_value;
        this.handler = handler;
        this.targetNumber = data.getParsedPhoneNumber();
        this.range = 1000;
        currentBatchAmount = 0;
        currentBatchAmountRange = getRandomInRange.getRandomNumberInRange(100, 1000);
        operatingInBatchMode = true;
        stopProcess = false;
    }

    @Override
    public void run() {
        //this function will need some kind of optimization
        //see https://howtodoinjava.com/java/collections/performance-comparison-of-different-for-loops-in-java/
        //according to that, we need to reduce the number of function calls, so I am, and here it is
        int movingIntegral_2 = (short) userInput.integral_2_Start();
        int end_Integral_2 = (short)userInput.integral_2_End();
        int movingIntegral_3 = (short)userInput.integral_3_Start();
        int end_Integral_3 = (short)userInput.integral_3_End();
        int rangeStart = (short) userInput.xStart();
        int rangeEnd = (short) userInput.xEnd();

        for(int second_integral_value = movingIntegral_2; second_integral_value<end_Integral_2; second_integral_value++){
            //use to kill thread process gracefully
//            Log.d(TAG,"At outer moving for loop beta == " + second_integral_value);
            for(int third_integral_value = movingIntegral_3; third_integral_value<end_Integral_3; third_integral_value++) {
                //use to kill thread process gracefully
                for (int movingLowerLimit = rangeStart; movingLowerLimit < rangeEnd; movingLowerLimit++) {
                    //for enabling pause resume section
                    //--------------
                    while(pauseWork)
                    {
                        setState(STATE_PAUSED);
                        try
                        {
                            //noinspection BusyWait
                            Thread.sleep(1000); //stop for 1000ms increments
                            //use to kill thread process gracefully
                            if(stopProcess){
                                gracefulExit_kill_thread();
                                return;
                            }
                        } catch (InterruptedException ie)
                        {
                            Log.e(TAG,"Interrupt on secondary for loop!");
                            Log.e(TAG,ie.getMessage());
                            //report or ignore
                        }
                    }
                    setState(STATE_RUNNING);
                    //--------------

                    SingleMathOp lowerLimitCalc = new SingleMathOp(movingLowerLimit, first_integral_value, second_integral_value, third_integral_value);
                    long lowerLimitValue = lowerLimitCalc.getDerivative();
                    for (int movingUpperLimit = (movingLowerLimit + 1); movingUpperLimit <= rangeEnd; movingUpperLimit++) {
                        SingleMathOp mathOp = new SingleMathOp(movingUpperLimit, first_integral_value, second_integral_value, third_integral_value);
                        long upperLimitValue = mathOp.getDerivative();
                        long diff = upperLimitValue - lowerLimitValue;
                        if (Math.abs(targetNumber - diff) < range) {
                            //if here then number is within range!
                            long dividend = targetNumber - diff;
                            String phoneNumberTxt = PrintCalc.PrintCalcObjPass(movingLowerLimit, movingUpperLimit, first_integral_value, second_integral_value, third_integral_value, dividend);
//                            Log.d(TAG,"****SUCCESS*********" + phoneNumberTxt);

                            postMessage(phoneNumberTxt + "\n");
                            if(userInput.getStopOnFirstSuccess()){
                                SignalStop();
                            }
                        }
                        //use to kill thread process gracefully
                        if(stopProcess){
                            gracefulExit_kill_thread();
                            return;
                        }

                    }
                    postCompleteOperation();
                }
                //need to increment current batch here
                //cause, the loop math logic, so leave it be.
                currentBatchAmount++;
            }

        }


        if(operatingInBatchMode){
            //if we were operating in batch mode, we need to clear out what left, and post it into the UI filed
            //to do so we call the normal postCompleteOperationBatch operation, but pass true in the parameter field
            //yup that's all
            postCompleteOperationBatch(true);
        }
        setState(STATE_FINISHED);
    }

    private void SignalStop() {
        //if here, then user wanted to stop on first success
        //use post massage to stop all threads
        stopProcess = true;
        Message msg = handler.obtainMessage();
        msg.what = MainActivity.SUCCESSFUL_OPERATION;
        handler.sendMessage(msg);
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
        if(stopProcess){
            return;
        }
        //there was an issue with the main UI thread
        //calling this update UI function a few thousand times per second broke the UI thread
        //so now we must Make with call as Batch!
        Message msg = handler.obtainMessage();
        msg.what = MainActivity.ONE_CALCULATION_COMPLETED_BATCH;
        handler.sendMessage(msg);
    }

    private void postCompleteOperationBatch(boolean forceSend) {
        if(stopProcess){
            return;
        }
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
            msg.what = MainActivity.ONE_CALCULATION_COMPLETED_BATCH;
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.MESSAGE_NAME_ID, String.valueOf(currentBatchAmount));
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
        if(stopProcess){
            return;
        }
        Message msg = handler.obtainMessage();
        msg.what = MainActivity.POST_MESSAGE_IN_RESULTS;
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.MESSAGE_NAME_ID, msgStr);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }


    //for enabling pause resume section
    //--------------
    //taken from
    //https://coderanch.com/t/436108/java/Pause-Resume-Thread
    public void pause()
    {
        this.pauseWork = true;
    }

    public void resume(Handler mainLoopHandler)
    {
        this.handler = mainLoopHandler;
        this.pauseWork = false;
    }

    private void setState(String state)
    {
        this.state = state;
    }

    public String getState()
    {
        return this.state;
    }

    public void gracefulExit(){
        stopProcess = true;
    }

    private void gracefulExit_kill_thread(){
        this.handler = null;
    }

}
