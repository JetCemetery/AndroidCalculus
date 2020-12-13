package com.jetcemetery.androidcalulus.calcOperation;

import android.util.Log;

import java.io.Serializable;

import static java.lang.Math.abs;

public class OperationValues implements Serializable {
    private static final String TAG = "OperationValues";
    public static String DATA_OBJ_NAME = "DataObj";
    private Long phoneNum;
    private final String rawPhoneNumber;
    private int integral_1_sta, integral_2_sta, integral_3_sta;
    private int integral_1_end, integral_2_end, integral_3_end;
    private boolean stopOnFirstSuccess;
    private cpu_use_options cpu_options;
    private int CPUs_on_device;
    private final int cpuToUse;
    private float expectedOperations;

    public enum cpu_use_options {
        CPU_SINGLE,
        CPU_HALF,
        CPU_ALL_MINUS_1,
        CPU_ALL,
    }

    public OperationValues(String phoneNum, int integral_1_sta, int integral_2_sta, int integral_3_sta, int integral_1_end, int integral_2_end, int integral_3_end, boolean stopOnFirstSuccess) {
        //this.phoneNum = phoneNum;
        this.rawPhoneNumber = phoneNum;
        this.integral_1_sta = integral_1_sta;
        this.integral_2_sta = integral_2_sta;
        this.integral_3_sta = integral_3_sta;
        this.integral_1_end = integral_1_end;
        this.integral_2_end = integral_2_end;
        this.integral_3_end = integral_3_end;
        this.stopOnFirstSuccess = stopOnFirstSuccess;
        Log.d(TAG, "OperationValues constructor stop on success == " + this.stopOnFirstSuccess);
        CPUs_on_device = 1;
        cpuToUse = 1;
        init();
    }

    public void setIntegralRanges(int start1, int start2, int start3, int end1, int end2, int end3) {
        this.integral_1_sta = start1;
        this.integral_2_sta = start2;
        this.integral_3_sta = start3;

        this.integral_1_end = end1;
        this.integral_2_end = end2;
        this.integral_3_end = end3;
    }

    public void setPhoneNumber(Long phoneNum){
        this.phoneNum = phoneNum;
    }

    public void setPhoneNumber(String srcPhoneNum){
        setPhoneNumber(Long.valueOf(srcPhoneNum));
    }

    public void setCpu_single() {
        cpu_options = cpu_use_options.CPU_SINGLE;
    }

    public void setCpu_half() {
        cpu_options = cpu_use_options.CPU_HALF;
    }

    public void setCpu_all_m_1() {
        cpu_options = cpu_use_options.CPU_ALL_MINUS_1;
    }

    public void setCpu_all() {
        cpu_options = cpu_use_options.CPU_ALL;
    }

    public void updateTotalExpectedOperations() {
        long tempVal = 1;
        int int1 = integral_1_end - integral_1_sta;
        int int2 = integral_2_end - integral_2_sta;
        int int3 = integral_3_end - integral_3_sta;
        int lastPart = 1000;
        tempVal = tempVal * int1;
        tempVal = tempVal * int2;
        tempVal = tempVal * int3;
        tempVal = tempVal * lastPart;
        expectedOperations = tempVal;
    }

    private void init() {
        stopOnFirstSuccess = false;
        cpu_options =  cpu_use_options.CPU_SINGLE;
        //expectedOperations = 1;
        //helper function that will figure out how many operation exists for the current settings
        //it's going to be the end of integral 1,2,3 - start of integral 1,2,3
        //as a permutation format
        updateTotalExpectedOperations();
    }

    public int alphaStart() {
        return this.integral_1_sta;
    }

    public Integer alphaEnd() {
        return this.integral_1_end;
    }

    public int getThreadNum() {
        return 2;
    }

    public long getNumber() {
        return this.phoneNum;
    }

    public boolean getMovingLowerLimit() {
        return true;
    }

    public int betaStart() {
        return this.integral_2_sta;
    }

    public int betaEnd() {
        return this.integral_2_end;
    }

    public int gammaStart() {
        return this.integral_3_sta;
    }

    public int gammaEnd() {
        return this.integral_3_end;
    }

    public int xStart() {
        return 1;
    }

    public int xEnd() {
        return 1000;
    }

    public void setPhoneCpuCnt(int totalCPU_cnt) {
        CPUs_on_device = totalCPU_cnt;
    }

    public String getInitialProgressText() {
        //there's an issue of non whole numbers for the expected text...
        return "0 / " + String.valueOf(Long.valueOf((long) expectedOperations));
    }

//    public int getCurrentProgress_for_progressBar(){
//        return operationForProgressBar(0);
//    }

    public int operationForProgressBar(float completedOperations){
        String tempStr = "Comp [" + completedOperations + "] exp [" + expectedOperations + "]\n";
        float temp = (float) (completedOperations / expectedOperations);
        temp = temp * 100;
//        tempStr+= "temp == [" + temp + "]";
//        Log.d(TAG, tempStr);
        return (int) temp;
    }

    public String getTotalOperationExpected() {
        //type float leaves a .0 at the end
        //need to cast into type long, and then into type string
        return String.valueOf(Long.valueOf((long) expectedOperations));
    }

    public void setStopOnSuccess(boolean value) {
        stopOnFirstSuccess = value;
        Log.d(TAG, "OperationValues getStopOnFirstSuccess stop on success == " + this.stopOnFirstSuccess);
    }

    public boolean getStopOnFirstSuccess(){
        Log.d(TAG, "OperationValues getStopOnFirstSuccess stop on success == " + this.stopOnFirstSuccess);
        return stopOnFirstSuccess;
    }

    public boolean identicalDataObj(OperationValues srcDataObj) {
        //helper function that will compare this data object with the one that was passed
        //you are only looking at the following params
        //integrals start/end
        //cpu count
        //stop on first success
        boolean valuesSame = true;

        valuesSame &= withinRange(srcDataObj.alphaStart(), this.alphaStart(),"alpha Start");
        valuesSame &= withinRange(srcDataObj.alphaEnd(), this.alphaEnd(),"alphaEnd");
        valuesSame &= withinRange(srcDataObj.betaStart(), this.betaStart(),"betaStart");
        valuesSame &= withinRange(srcDataObj.betaEnd(), this.betaEnd(),"betaEnd");
        valuesSame &= withinRange(srcDataObj.gammaStart(), this.gammaStart(),"gammaStart");
        valuesSame &= withinRange(srcDataObj.gammaEnd(), this.gammaEnd(),"gammaEnd");


        if(!srcDataObj.getCPU_OptionsEnum().equals(this.getCPU_OptionsEnum())){
            valuesSame = false;
            Log.d(TAG,"valuesSame tripped to false on getCPU_OptionsEnum");
        }

        if(srcDataObj.getStopOnFirstSuccess() != this.getStopOnFirstSuccess()){
            valuesSame = false;
            Log.d(TAG,"valuesSame tripped to false on step on success");
        }

        if(!valuesSame){
            String buildingStr = "identicalDataObj this one is off somewhere\n";
            buildingStr+=srcDataObj.alphaStart()+ " == " + this.alphaStart() + "\n";
            buildingStr+=srcDataObj.alphaEnd()+ " == " + this.alphaEnd()+ "\n";
            buildingStr+=srcDataObj.betaStart()+ " == " + this.betaStart()+ "\n";
            buildingStr+=srcDataObj.betaEnd()+ " == " + this.betaEnd()+ "\n";
            buildingStr+=srcDataObj.gammaStart()+ " == " + this.gammaStart()+ "\n";
            buildingStr+=srcDataObj.gammaEnd()+ " == " + this.gammaEnd()+ "\n";
            buildingStr+=srcDataObj.getCPU_OptionsEnum()+ " == " + this.getCPU_OptionsEnum()+ "\n";
            buildingStr+=srcDataObj.getStopOnFirstSuccess()+ " == " + this.getStopOnFirstSuccess()+ "\n";
            Log.d(TAG,buildingStr);
        }
        Log.d(TAG,"Returning valuesSame = [" + valuesSame + "]");
        return valuesSame;
    }

    private boolean withinRange(int srcValue, int srcThisValue, String msg) {
        //temp debug helper
        Log.d(TAG,"data passed == \t[" + srcValue + "]\t["+srcThisValue+"]");
        if(abs(srcValue - srcThisValue) > 1){
            Log.d(TAG,"Returning false for here [" + msg + "]");
            return false;
        }
        return true;
    }

    public cpu_use_options getCPU_OptionsEnum() {
        return cpu_options;
    }
}