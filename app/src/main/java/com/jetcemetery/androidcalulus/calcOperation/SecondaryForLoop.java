package com.jetcemetery.androidcalulus.calcOperation;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.jetcemetery.androidcalulus.MainActivityDataObj;
import com.jetcemetery.androidcalulus.RenderValues;

public class SecondaryForLoop{

    private int movingAlpha;
    private Handler handler;
    private MainActivityDataObj allData;
    private long startTime;
    private long targetNumber;
    private int range;

    public SecondaryForLoop(MainActivityDataObj data, int srcAlpha, Handler handler) {
        allData = data;
        movingAlpha = srcAlpha;
        this.handler = handler;
        startTime = System.currentTimeMillis();
        targetNumber = data.getNumber();
        range = 1000;
        //range = data.getPrecision();
    }

    public void StartOperation() {
        //start operation has two variations
        //the first one where there lower limit is set to zero
        //the other is where we have a moving lower limit

        if(allData.getMovingLowerLimit()){
            //if here then we are having a moving lower limit
            movingLowerLimit();
        }
        else{
            //if here then we have a stationary lower limit of zero
            stationaryLowerLimit();
        }

    }

    private void movingLowerLimit() {
        //the new way that the operation will be done
        //VERY similar to lower limits as far as moving Alpha, Beta and Gamma goes
        //except that the moving x is split into lower and upper limit and the upper limit will be the moving target
        for(int movingBeta = allData.betaStart(); movingBeta<allData.betaEnd(); movingBeta++){
            for(int movingGamma = allData.gammaStart(); movingGamma<allData.gammaEnd(); movingGamma++){
                //Alpha, Beta and Gamma should remain the same through out the operations

                for(int movingLowerLimit = allData.xStart(); movingLowerLimit < allData.xEnd()-1; movingLowerLimit++){
                    SingleMathOp lowerLimitCalc = new SingleMathOp(movingLowerLimit, movingAlpha, movingBeta, movingGamma);
                    lowerLimitCalc.runMath();
                    long lowerLimitValue = lowerLimitCalc.getDervivate();

                    for(int movingUpperLimit = movingLowerLimit+1; movingUpperLimit<allData.xEnd(); movingUpperLimit++)	{
                        SingleMathOp mathOp = new SingleMathOp(movingUpperLimit, movingAlpha, movingBeta, movingGamma);
                        mathOp.runMath();
                        long upperLimitValue = mathOp.getDervivate();
                        long diff = upperLimitValue - lowerLimitValue;
                        if(Math.abs(targetNumber - diff) < range){
                            //if here then number is within range!
                            long dividend = targetNumber - diff;
                            postMessage(PrintCalc.PrintCalcObjPass(movingLowerLimit,movingUpperLimit, movingAlpha, movingBeta, movingGamma, allData.getNumber(),dividend));
                            //resultsArea.append();
                        }
                    }
                }
            }
        }
//		System.out.println("increment result!");
        //resultsArea.incrementProgressbar();
    }

    private void postMessage(String msgStr) {
        Message msg = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString(RenderValues.MESSAGE_NAME_ID, msgStr);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private void stationaryLowerLimit() {
        //classical way I did the calc problem with more parts moved out
        for(int movingBeta = allData.betaStart(); movingBeta<allData.betaEnd(); movingBeta++){
            for(int movingGamma = allData.gammaStart(); movingGamma<allData.gammaEnd(); movingGamma++){
                for(int movingX = allData.xStart(); movingX<allData.xEnd(); movingX++)	{
                    SingleMathOp mathOp = new SingleMathOp(movingX, movingAlpha, movingBeta, movingGamma);

                    mathOp.runMath();
                    if(Math.abs(targetNumber - mathOp.getDervivate()) < range){
                        //if here then number is within range!
                        postMessage(PrintCalc.PrintCalcOp(movingX, movingAlpha, movingBeta, movingGamma, allData.getNumber()));
                    }
                }
            }
        }
    }

    //public void showStartTime(int movingAlpha2, CalcOpInterface intObj) {
    //    new ShowOperationTime(movingAlpha2,intObj,startTime);
    //}

}