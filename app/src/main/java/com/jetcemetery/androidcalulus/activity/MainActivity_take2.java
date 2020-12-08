package com.jetcemetery.androidcalulus.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jetcemetery.androidcalulus.OperationValues;
import com.jetcemetery.androidcalulus.R;
import com.jetcemetery.androidcalulus.RenderValues;
import com.jetcemetery.androidcalulus.helper.OperationValues_default;
import com.jetcemetery.androidcalulus.helper.StartOperationHelper;

import java.util.ArrayList;

public class MainActivity_take2 extends AppCompatActivity {
    private EditText txtPhone;
    private TextView txtError, txtResults, txt_progressBar2;
    private Button btnStart;
    private ProgressBar prbBar_progressBar;
    private OperationValues dataObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_take2);
        txtPhone = findViewById(R.id.txt_phoneID2);
        txtError = findViewById(R.id.errorText2);
        btnStart = findViewById(R.id.btn_start2);
        prbBar_progressBar = findViewById(R.id.pgBar_Progress_bar2);
        txt_progressBar2 = findViewById(R.id.txt_progress2);
        txtResults = findViewById(R.id.txt_results2);
        String un_parsed_phone = String.valueOf(txtPhone.getText());
        dataObj = OperationValues_default.getDefaultValues(un_parsed_phone);
        initView();
    }

    private void initView() {
        txtError.setVisibility(TextView.GONE);
        updateProgressBar_and_Text();
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtError.setVisibility(TextView.GONE);
                StartOperationHelper helperObj = new StartOperationHelper(dataObj);
                if(helperObj.misconfiguredIntegralRange()){
                    //should never get here
                    //however if here, then the user set the integral start / end in wrong direction
                    txtError.setVisibility(TextView.VISIBLE);
                    txtError.setText(helperObj.getMisconfiguredIntegralRange_text());
                    Toast.makeText(getApplicationContext(), "Integral range is out of sequence", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(helperObj.numberInputValid(txtPhone)){
                    //if here, then input is valid
                    txtError.setVisibility(View.GONE);
                    String ParsedNumber = getParsedNumber();
                    dataObj.setPhoneNumer(ParsedNumber);
                    Toast.makeText(getApplicationContext(), "process [" + ParsedNumber + "]", Toast.LENGTH_SHORT).show();
                    //Intent intent = new Intent(getApplicationContext(), RenderValues.class);
                    //OperationValues dataObj = getUserValues(ParsedNumber);
                    //intent.putExtra(OperationValues.DATAOBJ_NAME, dataObj);
                    //startActivity(intent);
                }
                else{
                    //if here, then number is NOT valid
                    txtError.setVisibility(View.VISIBLE);
                    txtError.setText("ERROR!");
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private String getParsedNumber() {
        //this function needs to scrub the input of the phone number, and return only whole digits
        ArrayList<Character> buildingData = new ArrayList<>();
        String un_parsed_phone = String.valueOf(txtPhone.getText());
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

    private void updateProgressBar_and_Text() {
        prbBar_progressBar.setProgress(dataObj.getCurrentProgress_for_progressBar());
        txt_progressBar2.setText(dataObj.getInitialProgressText());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = null;
        //TODO fill in the data object with default if null....
        OperationValues dataObj = null;
        switch (item.getItemId()){
            case R.id.menu_help:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                intent.putExtra(OperationValues.DATAOBJ_NAME, dataObj);
                startActivity(intent);
                break;
            case R.id.menu_settings:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                intent.putExtra(OperationValues.DATAOBJ_NAME, dataObj);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}