package com.jetcemetery.androidcalulus.calcOperation;

public class PrintCalc {
//	private static int movingX;

    public static String PrintCalcOp(int movingX, int alpha, int beta, int gamma, long num){
        SingleMathOp mathOperation = new SingleMathOp(movingX, alpha, beta, gamma);
        mathOperation.runMath();

        String alphaStr = "" + String.valueOf(alpha*4) + "x" + "^3";
        String beta_str = " + " + String.valueOf(beta*3) + "x" + "^2";
        String gamma_str = " + " + String.valueOf(gamma*2) + "x" + "";
//		String end_prt = reducedFraction(mathOp2(mathOperation.getDervivate(),num),movingX) + ") dx";
        String end_prt = " + " + mathOp2(mathOperation.getDervivate(),num) + "/" + String.valueOf(movingX) + ") dx";
        String opening = "the integral of 0 to " + String.valueOf(movingX) + " of (";
        String combo_str = opening + alphaStr + beta_str + gamma_str + end_prt;
        return combo_str;
    }

    private static String reducedFraction(long mathOp2, int movingX) {
        //you are passed
        return null;
    }

    private static long mathOp2(long dervValue, long targetNum){
        //the math operation will see if that value is within the accepted range
        long diffOp = Math.abs(targetNum - dervValue);
        return diffOp;
    }

    private static String printResult(int lowerX, int upperX,int alpha, int beta, int gamma, long dividend, long divisor){
        StringBuilder builder = new StringBuilder();
        builder.append("the integral of ");
        builder.append(lowerX);
        builder.append(" to ");
        builder.append(upperX);
        builder.append(" of (");
        builder.append(alpha*4);
        builder.append("x^3 +");
        builder.append(beta*3);
        builder.append("x^2 +");
        builder.append(gamma*2);
        if(dividend >=0){
            builder.append("x +");
        }
        else{
            builder.append("x ");
        }
        builder.append(dividend);
        builder.append("/");
        builder.append(divisor);
        builder.append(") dx");

        return builder.toString();
    }

    public static String PrintCalcObjPass(int xlow, int xhigh, int alpha, int beta, int gamma, long number, long diff) {
        //figure out the various ways to see if you can get the last part
        int divBot = getDivisor(xlow,xhigh);
        return printResult(xlow,xhigh,alpha,beta,gamma,diff,divBot);
    }

    private static int getDivisor(int xlow,int xhigh) {
        //The divisor (bottom part) is simply the upper limit minus the lower limit
        return xhigh - xlow;
    }

}