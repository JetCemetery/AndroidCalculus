package com.jetcemetery.calculusPhoneNumber.calcOperation;

public class SingleMathOp {
    private final int integrateOn;
    private final int integral_1;
    private final int integral_2;
    private final int integral_3;
    private long mathOpResults = 0;

    public SingleMathOp(int xVal, int srcAlpha, int srcBeta, int srcGamma) {
        //constructor
        //sets up all the needed numbers
        integrateOn = xVal;
        integral_1 = srcAlpha;
        integral_2 = srcBeta;
        integral_3 = srcGamma;
        runMath();
    }


    public void runMath(){
        //the math operation will see if that value is within the accepted range
        long al = mathOp(integral_1,4);
        long be = mathOp(integral_2,3);
        long ga = mathOp(integral_3,2);
        mathOpResults = (al+be+ga);
    }

    public long getDerivative(){
        return this.mathOpResults;
    }

    /*the operation described here will be
     * Coefficient * (xValue ^ exponent)
     */
    private long mathOp(int coEfficient, int expo) {
        return (long) (coEfficient*(Math.pow(integrateOn,expo)));
    }

}