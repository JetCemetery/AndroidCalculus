package com.jetcemetery.androidcalulus.helper;

import android.widget.EditText;

import com.jetcemetery.androidcalulus.calcOperation.OperationValues;
import com.jetcemetery.androidcalulus.calcOperation.Singleton_OperationValues;

public class StartOperationHelper {
    private final Singleton_OperationValues dataObj;
    private String mis_configuredIntegralRange_txt;
    public StartOperationHelper(Singleton_OperationValues dataObj) {
        this.dataObj = dataObj;
        mis_configuredIntegralRange_txt = "";
    }

    public boolean misconstruedIntegralRange() {
        boolean returningVal = false;
        mis_configuredIntegralRange_txt = "";
        if(dataObj.alphaEnd() < dataObj.alphaStart()){
            mis_configuredIntegralRange_txt = "Integral 1 end value is less then initial!";
            returningVal = true;
        }
        else if(dataObj.betaEnd() < dataObj.betaStart()){
            mis_configuredIntegralRange_txt = "Integral 2 end value is less then initial!";
            returningVal = true;
        }
        else if(dataObj.gammaEnd() < dataObj.gammaStart()){
            mis_configuredIntegralRange_txt = "Integral 3 end value is less then initial!";
            returningVal = true;
        }
        return returningVal;
    }

    public String getMisconstruedIntegralRange_text() {
        return this.mis_configuredIntegralRange_txt;
    }

    public boolean numberInputValid(EditText phoneText) {
        //from https://stackoverflow.com/questions/123559/how-to-validate-phone-numbers-using-regex/9636657#9636657
        //such a dam strange error here with the dash
        //we need to do a substitute in text.....
        //per https://qaz.wtf/u/show.cgi
        String goodVersionDash = "-";   //45     002D     -     HYPHEN-MINUS
        String badVersionDash = "–";    //8211     2013     –     EN DASH
        String un_parsed_phone = String.valueOf(phoneText.getText()).replace(badVersionDash, goodVersionDash);
        String regEx = "^\\s*(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)\\s*$";

        return un_parsed_phone.matches(regEx);
    }
}
