package com.jetcemetery.androidcalulus.helper;

import com.jetcemetery.androidcalulus.calcOperation.OperationValues;

import java.util.ArrayList;

public class OperationValues_default {
    public static int INTEGRAL_START_DEFAULT = 1;
    public static int INTEGRAL_END_DEFAULT = 20;

    //two constructors, one with a phone number provided, the other makes up the phone number
    public static OperationValues getDefaultValues(){
        String defaultPhoneNumber = "555-555-5555";
        return getDefaultValues(defaultPhoneNumber);
    }
    public static OperationValues getDefaultValues(String un_parsed_phone){
        int integral_1_sta, integral_2_sta, integral_3_sta;
        int integral_1_end, integral_2_end, integral_3_end;
        boolean stopOnFirstSuccess = false;
        integral_1_sta = INTEGRAL_START_DEFAULT;
        integral_2_sta = INTEGRAL_START_DEFAULT;
        integral_3_sta = INTEGRAL_START_DEFAULT;

        integral_1_end = INTEGRAL_END_DEFAULT;
        integral_2_end = INTEGRAL_END_DEFAULT;
        integral_3_end = INTEGRAL_END_DEFAULT;
        return new OperationValues(un_parsed_phone, integral_1_sta, integral_2_sta, integral_3_sta, integral_1_end, integral_2_end, integral_3_end, false);
    }

    public static long parsePhoneInput(String un_parsed_phone) {
        //helper method that will take care of parsing the phone number passed
        ArrayList<Character> buildingData = new ArrayList<>();
        //String un_parsed_phone = String.valueOf(phoneText.getText());
        for(char c : un_parsed_phone.toCharArray()){
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
}
