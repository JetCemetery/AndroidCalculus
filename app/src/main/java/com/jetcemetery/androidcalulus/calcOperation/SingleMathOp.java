package com.jetcemetery.androidcalulus.calcOperation;

public class SingleMathOp {
    private final int integrateOn;
    private final int alpha;
    private final int beta;
    private final int gamma;
    private long mathOpResults;

    public SingleMathOp(int xVal, int srcAlpha, int srcBeta, int srcGamma) {
        //constructor
        //sets up all the needed numbers
        integrateOn = xVal;
        alpha = srcAlpha;
        beta = srcBeta;
        gamma = srcGamma;
        mathOpResults = 1L;
    }


    public void runMath(){
        //the math operation will see if that value is within the accepted range
        long al = mathOp(alpha,4);
        long be = mathOp(beta,3);
        long ga = mathOp(gamma,2);
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