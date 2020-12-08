package com.jetcemetery.androidcalulus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jetcemetery.androidcalulus.helper.getCPU_Cnt;

import java.io.Serializable;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private NumberPicker integral1_start, integral2_start, integral3_start;
    private NumberPicker integral1_end, integral2_end, integral3_end;
    private int minValue = 1;
    private int maxValue = 1000;
    private EditText phoneText;
    private LinearLayout layout_PhoneError;
    private TextView errorText;
    private Button startAction;
    private RadioGroup rdgb_cpu_cnt, rdgb_stopOnSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        integral1_start = findViewById(R.id.integral1_start);
        integral2_start = findViewById(R.id.integral2_start);
        integral3_start = findViewById(R.id.integral3_start);

        integral1_end = findViewById(R.id.integral1_end);
        integral2_end = findViewById(R.id.integral2_end);
        integral3_end = findViewById(R.id.integral3_end);
        phoneText = findViewById(R.id.phoneID);
        layout_PhoneError = findViewById(R.id.layout_PhoneError);
        errorText = findViewById(R.id.errorText);
        startAction = findViewById(R.id.startButton);
        rdgb_cpu_cnt = findViewById(R.id.rdgb_cpu_cnt);
        rdgb_stopOnSuccess = findViewById(R.id.rdgb_stopOnSuccess);

        init();
    }

    private void init() {
        //there is some kinda of bug in android where the layout must be set to visible in the xml file
        //upon start up set it to gone
        //in the code/action you will then need to toggle it
        //moral of the story in the xml view, set layout error to visible
        //at launch set it to gone.....
        layout_PhoneError.setVisibility(View.GONE);
        setStartIntegral(integral1_start);
        setStartIntegral(integral2_start);
        setStartIntegral(integral3_start);

        setEndIntegral(integral1_end);
        setEndIntegral(integral2_end);
        setEndIntegral(integral3_end);

        startAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(misconfiguredIntegralRange()){
                    //before checking phone number stuff
                    //check to see that the user entered OK ranges
                    //if user decided to invert the start/end let them know that it's not going to work
                    Toast.makeText(getApplicationContext(), "Integral range is out of sequence", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(numberInputValid()){
                    //if here, then input is valid
                    layout_PhoneError.setVisibility(View.GONE);
                    String ParsedNumber = getParsedNumber();
                    Toast.makeText(getApplicationContext(), "process [" + ParsedNumber + "]", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), RenderValues.class);
                    MainActivityDataObj dataObj = getUserValues(ParsedNumber);
                    intent.putExtra(MainActivityDataObj.DATAOBJ_NAME, dataObj);
                    startActivity(intent);
                }
                else{
                    //if here, then number is NOT valid
                    layout_PhoneError.setVisibility(View.VISIBLE);
                    errorText.setText("ERROR!");
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean misconfiguredIntegralRange() {
        //QC function
        //this is intended to prevent the user to have a start integral value to be greater than the end value!
        //step one, presume everything is OK, and turn on the warning text
        layout_PhoneError.setVisibility(View.GONE);
        boolean returningVal = false;
        if(integral1_end.getValue() < integral1_start.getValue()){
            errorText.setText("Integral 1 end value is less then initial!");
            layout_PhoneError.setVisibility(View.VISIBLE);
            returningVal = true;
        }
        else if(integral2_end.getValue() < integral2_start.getValue()){
            errorText.setText("Integral 2 end value is less then initial!");
            layout_PhoneError.setVisibility(View.VISIBLE);
            returningVal = true;
        }
        else if(integral3_end.getValue() < integral3_start.getValue()){
            errorText.setText("Integral 2 end value is less then initial!");
            layout_PhoneError.setVisibility(View.VISIBLE);
            returningVal = true;
        }

        return returningVal;
    }

    private MainActivityDataObj getUserValues(String srcPhoneNumber) {
        //this function shall set up all the main activity data object
        //it will process the user inputs, save it inside a MainActivityDataObj, and then return the initialized object
        long phoneNumber = Long.valueOf(srcPhoneNumber);
        int integral_1_sta, integral_2_sta, integral_3_sta;
        int integral_1_end, integral_2_end, integral_3_end;
        boolean stopOnFirstSuccess = getStopOnFirstSuccessGroup();
        integral_1_sta = integral1_start.getValue();
        integral_2_sta = integral2_start.getValue();
        integral_3_sta = integral3_start.getValue();

        integral_1_end = integral1_end.getValue();
        integral_2_end = integral2_end.getValue();
        integral_3_end = integral3_end.getValue();


        MainActivityDataObj returningObj = new MainActivityDataObj(phoneNumber, integral_1_sta, integral_2_sta, integral_3_sta, integral_1_end, integral_2_end, integral_3_end, stopOnFirstSuccess);
        setCPU_Cnt(returningObj);
        return returningObj;
    }

    private void setCPU_Cnt(MainActivityDataObj returningObj) {

        int id = rdgb_stopOnSuccess.getCheckedRadioButtonId();
        getCPU_Cnt findCPU_cnt = new getCPU_Cnt();
        int totalCPU_Cnt = findCPU_cnt.getCount();
        returningObj.setPhoneCpuCnt(totalCPU_Cnt);
        Toast.makeText(getApplicationContext(), "Total CPUs so far == " + totalCPU_Cnt, Toast.LENGTH_SHORT).show();
        switch (id) {
            case R.id.rdgb_cpu_cnt_Single:
                returningObj.setCpu_single();
                break;
            case R.id.rdgb_cpu_cnt_half:
                returningObj.setCpu_half();
                break;
            case R.id.rdgb_cpu_cnt_all_minus1:
                returningObj.setCpu_all_m_1();
                break;
            case R.id.rdgb_cpu_cnt_all:
                returningObj.setCpu_all();
                break;
            default:
                returningObj.setCpu_single();
                break;
        }

    }

    private boolean getStopOnFirstSuccessGroup() {
        boolean returningBool = false;
        int id = rdgb_stopOnSuccess.getCheckedRadioButtonId();
        switch (id) {
            case R.id.rdgb_stopOnSuccess_Yes:
                returningBool = true;
                break;
            case R.id.rdgb_stopOnSuccess_No:
                break;
            default:
                break;
        }
        return returningBool;
    }

    private boolean numberInputValid() {
        //from https://stackoverflow.com/questions/123559/how-to-validate-phone-numbers-using-regex/9636657#9636657
        String regEx = "^\\s*(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)\\s*$";
        String un_parsed_phone = String.valueOf(phoneText.getText());
        return un_parsed_phone.matches(regEx);
    }

    private String getParsedNumber() {
        //this function needs to scrub the input of the phone number, and return only whole digits
        ArrayList<Character> buildingData = new ArrayList<>();
        String un_parsed_phone = String.valueOf(phoneText.getText());
        for(char c : un_parsed_phone.toCharArray()){
            if(Character.isDigit(c)){
                buildingData.add(c);
            }
        }
        String returningStr = "";
        for(char c : buildingData){
            returningStr += String.valueOf(c);
        }
        return returningStr;
    }

    private void setStartIntegral(NumberPicker srcStartIntegral) {
        srcStartIntegral.setMinValue(minValue);
        srcStartIntegral.setMaxValue(maxValue);
        srcStartIntegral.setValue(1);
    }
    private void setEndIntegral(NumberPicker srcStartIntegral) {
        srcStartIntegral.setMinValue(minValue);
        srcStartIntegral.setMaxValue(maxValue);
        srcStartIntegral.setValue(1000);
    }
}