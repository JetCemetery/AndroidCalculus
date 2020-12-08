package com.jetcemetery.androidcalulus;

import java.io.Serializable;

public class OperationValues implements Serializable {
    public static String DATAOBJ_NAME = "DataObj";
    private Long phoneNum;
    private int integral_1_sta, integral_2_sta, integral_3_sta;
    private int integral_1_end, integral_2_end, integral_3_end;
    private boolean stopOnFirstSuccess;
    private boolean cpu_single, cpu_half, cpu_all_m_1, cpu_all;
    private int CPUs_on_device;
    private long expectedOperations;

    public OperationValues(Long phoneNum, int integral_1_sta, int integral_2_sta, int integral_3_sta, int integral_1_end, int integral_2_end, int integral_3_end, boolean stopOnFirstSuccess) {
        this.phoneNum = phoneNum;
        this.integral_1_sta = integral_1_sta;
        this.integral_2_sta = integral_2_sta;
        this.integral_3_sta = integral_3_sta;
        this.integral_1_end = integral_1_end;
        this.integral_2_end = integral_2_end;
        this.integral_3_end = integral_3_end;
        this.stopOnFirstSuccess = stopOnFirstSuccess;
        CPUs_on_device = 1;
        init();
    }

    public void setPhoneNumer(Long phoneNum){
        this.phoneNum = phoneNum;
    }
    public void setPhoneNumer(String srcPhoneNum){
        setPhoneNumer(String.valueOf(srcPhoneNum));
    }

    public void setCpu_single() {
        this.cpu_single = true;
        this.cpu_half = false;
        this.cpu_all_m_1 = false;
        this.cpu_all = false;
    }

    public void setCpu_half() {
        this.cpu_single = false;
        this.cpu_half = true;
        this.cpu_all_m_1 = false;
        this.cpu_all = false;
    }

    public void setCpu_all_m_1() {
        this.cpu_single = false;
        this.cpu_half = false;
        this.cpu_all_m_1 = true;
        this.cpu_all = false;
    }

    public void setCpu_all() {
        this.cpu_single = false;
        this.cpu_half = false;
        this.cpu_all_m_1 = false;
        this.cpu_all = true;
    }

    private void init() {
        stopOnFirstSuccess = false;
        cpu_single = false;
        cpu_half = false;
        cpu_all_m_1 = false;
        cpu_all = false;
        expectedOperations = 1;
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
        //helper function that will figure out how many operation exists for the current settings
        //it's going to be the end of integral 1,2,3 - start of integral 1,2,3
        //as a permutation format
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
        String returningStr ="0 / " + String.valueOf(tempVal);
        return returningStr;
    }

    public int getCurrentProgress_for_progressBar(){
        return operationForProgressBar(0);
    }
    public int operationForProgressBar(long completedOperations){
        double temp = (double) (completedOperations / expectedOperations);
        temp = temp * 100;
        return (int) temp;
    }

    public String getTotalOperationExpected() {
        return String.valueOf(expectedOperations);
    }
}