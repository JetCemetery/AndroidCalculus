package com.jetcemetery.androidcalulus.helper;

import android.widget.EditText;

import com.jetcemetery.androidcalulus.OperationValues;

public class StartOperationHelper {
    private final OperationValues dataObj;
    private String misconfiguredIntegralRange_txt;
    public StartOperationHelper(OperationValues dataObj) {
        this.dataObj = dataObj;
        misconfiguredIntegralRange_txt= "";
    }

    public boolean misconstruedIntegralRange() {
        boolean returningVal = false;
        misconfiguredIntegralRange_txt= "";
        if(dataObj.alphaEnd() < dataObj.alphaStart()){
            misconfiguredIntegralRange_txt = "Integral 1 end value is less then initial!";
            returningVal = true;
        }
        else if(dataObj.betaEnd() < dataObj.betaStart()){
            misconfiguredIntegralRange_txt = "Integral 2 end value is less then initial!";
            returningVal = true;
        }
        else if(dataObj.gammaEnd() < dataObj.gammaStart()){
            misconfiguredIntegralRange_txt = "Integral 3 end value is less then initial!";
            returningVal = true;
        }
        return returningVal;
    }

    public String getMisconstruedIntegralRange_text() {
        return this.misconfiguredIntegralRange_txt;
    }

    public boolean numberInputValid(EditText phoneText) {
        //from https://stackoverflow.com/questions/123559/how-to-validate-phone-numbers-using-regex/9636657#9636657
        String regEx = "^\\s*(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)\\s*$";
        String un_parsed_phone = String.valueOf(phoneText.getText());
        return un_parsed_phone.matches(regEx);
    }
}
