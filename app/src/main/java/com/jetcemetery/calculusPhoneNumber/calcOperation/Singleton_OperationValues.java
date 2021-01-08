package com.jetcemetery.calculusPhoneNumber.calcOperation;

import android.util.Log;

import com.jetcemetery.calculusPhoneNumber.helper.GettingUserPhoneNumber;
import com.jetcemetery.calculusPhoneNumber.helper.getCPU_Cnt;

import java.util.ArrayList;

public class Singleton_OperationValues {
    private static final String TAG = "OperationValues";
    private static Singleton_OperationValues instance;

    private String rawPhoneNumber;
    private int integral_1_sta, integral_2_sta, integral_3_sta;
    private int integral_1_end, integral_2_end, integral_3_end;
    private boolean stopOnFirstSuccess;
    private Singleton_OperationValues.cpu_use_options cpu_options;
    private int CPUs_on_device;
    private int cpuToUse;
    private float expectedOperations;
    private boolean changesMade = false;
    private long local_ProgressOperationsCompleted;
    private String ResultsTextArea;


    public static void initInstance() {
        if (instance == null) {
            // Create the instance
            int INTEGRAL_START_DEFAULT = 1;
            int INTEGRAL_END_DEFAULT = 100;
            String PhoneNumber;
            String defNumber = "555-555-5555";
            try {
                GettingUserPhoneNumber phoneHelper = new GettingUserPhoneNumber();
                PhoneNumber = phoneHelper.getUserNumber();
                if(PhoneNumber == null){
                    PhoneNumber = defNumber;
                }else if(PhoneNumber.length() <= 1){
                    PhoneNumber = defNumber;
                }
            } catch (Exception e) {
                PhoneNumber = defNumber;
            }

            int integral_1_sta, integral_2_sta, integral_3_sta;
            int integral_1_end, integral_2_end, integral_3_end;
            integral_1_sta = INTEGRAL_START_DEFAULT;
            integral_2_sta = INTEGRAL_START_DEFAULT;
            integral_3_sta = INTEGRAL_START_DEFAULT;

            integral_1_end = INTEGRAL_END_DEFAULT;
            integral_2_end = INTEGRAL_END_DEFAULT;
            integral_3_end = INTEGRAL_END_DEFAULT;

            instance = new Singleton_OperationValues(PhoneNumber,
                    integral_1_sta,
                    integral_2_sta,
                    integral_3_sta,
                    integral_1_end,
                    integral_2_end,
                    integral_3_end,
                    false);
        }
    }

    public static Singleton_OperationValues getInstance() {
        return instance;
    }

    public void setTextArea(String TextArea) {
        Log.d(TAG, "calling setTextArea, passed [" + TextArea + "]");
        ResultsTextArea = TextArea;
    }
    public String getTextArea() {
        return ResultsTextArea;
    }

    public String getPhoneNumber() {
        return rawPhoneNumber;
    }

    public void clearChangesMadeLatch() {
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
        //two part function
        //if changes were made, AND this function was called
        //we need to go ahead and reprocess total expected operation
        //AND we need to set local counter to zero
        //AND finally cleat the results text area
        if(changesMade){
            updateTotalExpectedOperations();
            local_ProgressOperationsCompleted = 0;
            setTextArea("");
        }

        return changesMade;
    }

    public long getParsedPhoneNumber() {
        //helper method that will take care of parsing the raw phone number into type long
        ArrayList<Character> buildingData = new ArrayList<>();
        //String un_parsed_phone = String.valueOf(phoneText.getText());
        for(char c : rawPhoneNumber.toCharArray()){
            if(Character.isDigit(c)){
                buildingData.add(c);
            }
        }
        StringBuilder returningStr = new StringBuilder();
        for(char c : buildingData){
            returningStr.append(c);
        }
        return Long.parseLong(returningStr.toString());
    }

    public void setProgressCount(long currentOperationsCompleted) {
        this.local_ProgressOperationsCompleted = currentOperationsCompleted;
    }

    public long getCurrentProgressCount(){
        return this.local_ProgressOperationsCompleted;
    }

    public enum cpu_use_options {
        CPU_SINGLE,
        CPU_HALF,
        CPU_ALL_MINUS_1,
        CPU_ALL,
    }

    public Singleton_OperationValues(String phoneNum, int integral_1_sta, int integral_2_sta, int integral_3_sta, int integral_1_end, int integral_2_end, int integral_3_end, boolean stopOnFirstSuccess) {
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

    public synchronized void setPhoneNumber(String srcPhoneNum){
        rawPhoneNumber = srcPhoneNum;
    }

    public void setCpu_single() {

        cpu_options = Singleton_OperationValues.cpu_use_options.CPU_SINGLE;
        CPUs_to_use_populate();
    }

    public void setCpu_half() {
        cpu_options = Singleton_OperationValues.cpu_use_options.CPU_HALF;
        CPUs_to_use_populate();
    }

    public void setCpu_all_m_1() {
        cpu_options = Singleton_OperationValues.cpu_use_options.CPU_ALL_MINUS_1;
        CPUs_to_use_populate();
    }

    public void setCpu_all() {
        cpu_options = Singleton_OperationValues.cpu_use_options.CPU_ALL;
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
        cpu_options =  cpu_use_options.CPU_ALL;
        getCPU_Cnt findCPU_cnt = new getCPU_Cnt();
        CPUs_on_device = findCPU_cnt.getCount();
        CPUs_to_use_populate();
        local_ProgressOperationsCompleted = 0;
        changesMade = false;
        updateTotalExpectedOperations();
        setTextArea("Results");
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

    public int getThreadNum() {
        return cpuToUse;
    }

    public int integral_1_Start() {
        return this.integral_1_sta;
    }

    public Integer integral_1_End() {
        return this.integral_1_end;
    }

    public int integral_2_Start() {
        return this.integral_2_sta;
    }

    public int integral_2_End() {
        return this.integral_2_end;
    }

    public int integral_3_Start() {
        return this.integral_3_sta;
    }

    public int integral_3_End() {
        return this.integral_3_end;
    }

    public int xStart() {
        return 1;
    }

    public int xEnd() {
        return 1000;
    }

    public int operationForProgressBar(){
        return operationForProgressBar(local_ProgressOperationsCompleted);
    }

    public int operationForProgressBar(float completedOperations){
        float temp = completedOperations / expectedOperations;
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

    public Singleton_OperationValues.cpu_use_options getCPU_OptionsEnum() {
        return cpu_options;
    }

}
