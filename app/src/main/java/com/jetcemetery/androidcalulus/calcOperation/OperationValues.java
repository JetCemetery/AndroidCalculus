package com.jetcemetery.androidcalulus.calcOperation;

import android.util.Log;

import com.jetcemetery.androidcalulus.helper.getCPU_Cnt;

import java.io.Serializable;

import static java.lang.Math.abs;

public class OperationValues implements Serializable {
    private static final String TAG = "OperationValues";
    public static String DATA_OBJ_NAME = "DataObj";
    private String rawPhoneNumber;
    private int integral_1_sta, integral_2_sta, integral_3_sta;
    private int integral_1_end, integral_2_end, integral_3_end;
    private boolean stopOnFirstSuccess;
    private cpu_use_options cpu_options;
    private int CPUs_on_device;
    private int cpuToUse;
    private float expectedOperations;
    private boolean changesMade;

//    private MainForLoopThread tranistionThread;
    private long transitionCurOperationsCompleted;
    private String transitionTotOperationExpected;
    private String transitionTextArea;

    public void tempDebugPrint() {
        //temp debugger
        Log.d(TAG, "inside temp debugger");
        Log.d(TAG, "stopOnFirstSuccess == " + stopOnFirstSuccess);
    }

//    public void setThread(MainForLoopThread myThread) {
//        if(myThread != null){
//            tranistionThread = myThread;
//        }
//    }

    public void setCurrentOpCompleted(long currentOperationsCompleted) {
        transitionCurOperationsCompleted = currentOperationsCompleted;
    }

    public void setTotalOpCompleted(String totalOperationExpected) {
        transitionTotOperationExpected = totalOperationExpected;
    }

    public void setTextArea(String TextArea) {
        transitionTextArea = TextArea;
    }
//
    public String getTextArea() {
        return transitionTextArea;
    }

    public long getTotalOpCompleted() {
        return transitionCurOperationsCompleted;
    }

    public String getCurrentOpCompleted() {
        return transitionTotOperationExpected;
    }

//    public MainForLoopThread getThread() {
//        return tranistionThread;
//    }

    public String getPhoneNumber() {
        return rawPhoneNumber;
    }

    public void movingToSettingsPage() {
        //if here, lets set the local variable changesMade to false
        //we are re-setting the latched signal
        changesMade = false;
    }

    public void changesMade() {
        //if here, then we are latching the signal changesMade to true
        Log.d(TAG, "changesMade tripped");
        changesMade = true;
    }

    public boolean wasDataChanged() {
        return changesMade;
    }

    public String getCPU_count_that_is_used() {
        return String.valueOf(cpuToUse);
    }

    public enum cpu_use_options {
        CPU_SINGLE,
        CPU_HALF,
        CPU_ALL_MINUS_1,
        CPU_ALL,
    }

    public OperationValues(String phoneNum, int integral_1_sta, int integral_2_sta, int integral_3_sta, int integral_1_end, int integral_2_end, int integral_3_end, boolean stopOnFirstSuccess) {
        this.rawPhoneNumber = phoneNum;
        this.integral_1_sta = integral_1_sta;
        this.integral_2_sta = integral_2_sta;
        this.integral_3_sta = integral_3_sta;
        this.integral_1_end = integral_1_end;
        this.integral_2_end = integral_2_end;
        this.integral_3_end = integral_3_end;
        this.stopOnFirstSuccess = stopOnFirstSuccess;
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

    public void setPhoneNumber(String srcPhoneNum){
        rawPhoneNumber = srcPhoneNum;
    }

    public void setCpu_single() {

        cpu_options = cpu_use_options.CPU_SINGLE;
        CPUs_to_use_populate();
    }

    public void setCpu_half() {
        cpu_options = cpu_use_options.CPU_HALF;
        CPUs_to_use_populate();
    }

    public void setCpu_all_m_1() {
        cpu_options = cpu_use_options.CPU_ALL_MINUS_1;
        CPUs_to_use_populate();
    }

    public void setCpu_all() {
        cpu_options = cpu_use_options.CPU_ALL;
        CPUs_to_use_populate();
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
        cpu_options =  cpu_use_options.CPU_HALF;
        getCPU_Cnt findCPU_cnt = new getCPU_Cnt();
        CPUs_on_device = findCPU_cnt.getCount();
        CPUs_to_use_populate();
        transitionCurOperationsCompleted = 0;
        //expectedOperations = 1;
        //helper function that will figure out how many operation exists for the current settings
        //it's going to be the end of integral 1,2,3 - start of integral 1,2,3
        //as a permutation format
        updateTotalExpectedOperations();
    }

    private void CPUs_to_use_populate() {
        //self setting function
        //depending on how many CPUs this phone has (based on getCPU_Cnt function), and what the
        //state of cpu_options enum is set, set the count for the variable cpuToUse

        //at same time, if this is called, then we need to indicate a change was made in the settings page
        changesMade();
        switch(cpu_options){
            case CPU_HALF:
                cpuToUse = CPUs_on_device / 2;
                break;
            case CPU_ALL_MINUS_1:
                cpuToUse = CPUs_on_device -1;
                break;
            case CPU_ALL:
                cpuToUse = CPUs_on_device;
                break;
            case CPU_SINGLE:
            default:
                cpuToUse = 1;
                break;
        }
    }

    public int alphaStart() {
        return this.integral_1_sta;
    }

    public Integer alphaEnd() {
        return this.integral_1_end;
    }

    public int getThreadNum() {
        return cpuToUse;
    }

    public String getNumber() {
        return this.rawPhoneNumber;
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

    public String getInitialProgressText() {
        //there's an issue of non whole numbers for the expected text...
        return "0 / " + String.valueOf(Long.valueOf((long) expectedOperations));
    }

    public int operationForProgressBar(float completedOperations){
        float temp = (float) (completedOperations / expectedOperations);
        temp = temp * 100;
        return (int) temp;
    }

    public String getTotalOperationExpected() {
        //type float leaves a .0 at the end
        //need to cast into type long, and then into type string
        return String.valueOf(Long.valueOf((long) expectedOperations));
    }

    public void setStopOnSuccess(boolean value) {
        //at same time, if this is called, then we need to indicate a change was made in the settings page
        changesMade();
        stopOnFirstSuccess = value;
    }

    public boolean getStopOnFirstSuccess(){
        return stopOnFirstSuccess;
    }

//    public boolean identicalDataObj(OperationValues srcDataObj) {
//        //helper function that will compare this data object with the one that was passed
//        //you are only looking at the following params
//        //integrals start/end
//        //cpu count
//        //stop on first success
//        boolean valuesSame = true;
//
//        valuesSame &= withinRange(srcDataObj.alphaStart(), this.alphaStart(),"alpha Start");
//        valuesSame &= withinRange(srcDataObj.alphaEnd(), this.alphaEnd(),"alphaEnd");
//        valuesSame &= withinRange(srcDataObj.betaStart(), this.betaStart(),"betaStart");
//        valuesSame &= withinRange(srcDataObj.betaEnd(), this.betaEnd(),"betaEnd");
//        valuesSame &= withinRange(srcDataObj.gammaStart(), this.gammaStart(),"gammaStart");
//        valuesSame &= withinRange(srcDataObj.gammaEnd(), this.gammaEnd(),"gammaEnd");
//
////        Log.d(TAG, "src  getCPU_OptionsEnum == " + srcDataObj.getCPU_OptionsEnum());
////        Log.d(TAG, "this getCPU_OptionsEnum == " + this.getCPU_OptionsEnum());
//        if(!srcDataObj.getCPU_OptionsEnum().equals(this.getCPU_OptionsEnum())){
//            valuesSame = false;
//        }
//
//        if(srcDataObj.getStopOnFirstSuccess() != this.getStopOnFirstSuccess()){
//            valuesSame = false;
//        }
//
//        if(!valuesSame){
//            String buildingStr = "identicalDataObj this one is off somewhere\n";
//            buildingStr+=srcDataObj.alphaStart()+ " == " + this.alphaStart() + "\n";
//            buildingStr+=srcDataObj.alphaEnd()+ " == " + this.alphaEnd()+ "\n";
//            buildingStr+=srcDataObj.betaStart()+ " == " + this.betaStart()+ "\n";
//            buildingStr+=srcDataObj.betaEnd()+ " == " + this.betaEnd()+ "\n";
//            buildingStr+=srcDataObj.gammaStart()+ " == " + this.gammaStart()+ "\n";
//            buildingStr+=srcDataObj.gammaEnd()+ " == " + this.gammaEnd()+ "\n";
//            buildingStr+=srcDataObj.getCPU_OptionsEnum()+ " == " + this.getCPU_OptionsEnum()+ "\n";
//            buildingStr+=srcDataObj.getStopOnFirstSuccess()+ " == " + this.getStopOnFirstSuccess()+ "\n";
//            Log.d(TAG,buildingStr);
//        }
//        return valuesSame;
//    }
//
//    private boolean withinRange(int srcValue, int srcThisValue, String msg) {
//        if(abs(srcValue - srcThisValue) > 1){
//            return false;
//        }
//        return true;
//    }

    public cpu_use_options getCPU_OptionsEnum() {
        return cpu_options;
    }
}