package com.jetcemetery.androidcalulus.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;

import com.jetcemetery.androidcalulus.calcOperation.OperationValues;
import com.jetcemetery.androidcalulus.R;
import com.jetcemetery.androidcalulus.helper.OperationValues_default;

public class SettingsActivity extends AppCompatActivity {
    public static String TAG = "SettingsActivity";
    private NumberPicker integral1_start, integral2_start, integral3_start;
    private NumberPicker integral1_end, integral2_end, integral3_end;
    private RadioButton rdgb_cpu_cnt_Single, rdgb_cpu_cnt_half, rdgb_cpu_cnt_all_minus1, rdgb_cpu_cnt_all;
    private RadioButton rdgb_stopOnSuccess_Yes_2, rdgb_stopOnSuccess_No_2;
    private OperationValues dataObj;
    private TextView txt_start, txt_end;

    private int numberPicker_setMinValue = 1;
    private int numberPicker_setMaxValue = 1000000;
    private int numberPicker_SetValueStart = 1;
    private int numberPicker_SetValueEnd = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Log.d(TAG, "Start of onCreate");
        integral1_start = findViewById(R.id.integral1_start_2);
        integral2_start = findViewById(R.id.integral2_start_2);
        integral3_start = findViewById(R.id.integral3_start_2);

        integral1_end = findViewById(R.id.integral1_end_2);
        integral2_end = findViewById(R.id.integral2_end_2);
        integral3_end = findViewById(R.id.integral3_end_2);
        //CPU Count
        rdgb_cpu_cnt_Single = findViewById(R.id.rbt_cpu_cnt_Single);
        rdgb_cpu_cnt_half = findViewById(R.id.rbt_cpu_cnt_half);
        rdgb_cpu_cnt_all_minus1 = findViewById(R.id.rbt_cpu_cnt_all_minus1);
        rdgb_cpu_cnt_all = findViewById(R.id.rbt_cpu_cnt_all);
        //Stop on first success
        rdgb_stopOnSuccess_Yes_2 = findViewById(R.id.rdgb_stopOnSuccess_Yes_2);
        rdgb_stopOnSuccess_No_2 = findViewById(R.id.rdgb_stopOnSuccess_No_2);

        //the label text start/end
        txt_start = findViewById(R.id.lbl_start2);
        txt_end = findViewById(R.id.lbl_end2);

        //on create we need to get
//        Log.d(TAG, "Before starting getting intent");
        Intent intent = this.getIntent();
        if(intent != null){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                OperationValues tempObj = (OperationValues) bundle.getSerializable(OperationValues.DATA_OBJ_NAME);
                if(tempObj != null){
                    dataObj = tempObj;
                    Log.d(TAG, "Intent has sent a tempObj!");
                }

//                MainForLoopThread tempNextThread = (MainForLoopThread) bundle.getSerializable(MainForLoopThread.DATA_OBJ_NAME);
//                if(tempNextThread != null){
//                    myThread_local = tempNextThread;
//                    Log.d(TAG, "Intent has sent a MainForLoopOperation!");
//                }
            }
        }

        Log.d(TAG, "checking data object is not null");
        if(dataObj == null){
            //if here, then for some reason Data object was not initialised
            //this is bad, very bad, so for now set the values to default
            dataObj = OperationValues_default.getDefaultValues();
            Log.d(TAG, "data object set to default values");
        }
        init();
    }

    private void init() {
        Log.d(TAG, "Inside init");
        //this function will initialize all of the user buttons/function/action listeners
        //Number Pickers
        setStartIntegral(integral1_start);
        setStartIntegral(integral2_start);
        setStartIntegral(integral3_start);
        setEndIntegral(integral1_end);
        setEndIntegral(integral2_end);
        setEndIntegral(integral3_end);
        switch (dataObj.getCPU_OptionsEnum()){
            case CPU_HALF:
                rdgb_cpu_cnt_half.setChecked(true);
                break;
            case CPU_ALL_MINUS_1:
                rdgb_cpu_cnt_all_minus1.setChecked(true);
                break;
            case CPU_ALL:
                rdgb_cpu_cnt_all.setChecked(true);
                break;
            case CPU_SINGLE:
            default:
                rdgb_cpu_cnt_Single.setChecked(true);
                break;
        }

        if(dataObj.getStopOnFirstSuccess()){
            rdgb_stopOnSuccess_Yes_2.setChecked(true);
        }else{
            rdgb_stopOnSuccess_No_2.setChecked(true);
        }

        //CPU radio group
        //TODO finish coding the cpu choices and let user truly select cpu usage
        rdgb_cpu_cnt_Single.setOnClickListener(init_cpuSingle());
        rdgb_cpu_cnt_half.setOnClickListener(init_cpuHalf());
        rdgb_cpu_cnt_all_minus1.setOnClickListener(init_cpuMinus1());
        rdgb_cpu_cnt_all.setOnClickListener(init_cpuAll());

        //Stop on Success Group
        rdgb_stopOnSuccess_Yes_2.setOnClickListener(init_stopOnSuccess_Yes());
        rdgb_stopOnSuccess_No_2.setOnClickListener(init_stopOnSuccess_No());

        //set values from the local data object
        integral1_start.setValue(dataObj.alphaStart());
        integral2_start.setValue(dataObj.betaStart());
        integral3_start.setValue(dataObj.gammaStart());

        integral1_end.setValue(dataObj.alphaEnd());
        integral2_end.setValue(dataObj.betaEnd());
        integral3_end.setValue(dataObj.gammaEnd());
    }

    @Override
    protected void onResume() {
        super.onResume();
        //the resume activity
        //check to see dataObj that was passed is different from what is saved locally
    }

    private View.OnClickListener init_cpuSingle() {
        return v -> dataObj.setCpu_single();
    }

    private View.OnClickListener init_cpuHalf() {
        return v -> dataObj.setCpu_half();
    }

    private View.OnClickListener init_cpuMinus1() {
        return v -> dataObj.setCpu_all_m_1();
    }

    private View.OnClickListener init_cpuAll() {
        return v -> dataObj.setCpu_all();
    }

    private View.OnClickListener init_stopOnSuccess_Yes() {
        return v -> dataObj.setStopOnSuccess(true);
    }

    private View.OnClickListener init_stopOnSuccess_No() {
        return v -> dataObj.setStopOnSuccess(false);
    }

    private void setStartIntegral(NumberPicker srcIntegral) {
        srcIntegral.setMinValue(numberPicker_setMinValue);
        srcIntegral.setMaxValue(numberPicker_setMaxValue);
        srcIntegral.setValue(numberPicker_SetValueStart);
    }

    private void setEndIntegral(NumberPicker srcIntegral) {
        srcIntegral.setMinValue(numberPicker_setMinValue);
        srcIntegral.setMaxValue(numberPicker_setMaxValue);
        srcIntegral.setValue(numberPicker_SetValueEnd);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Need this for the menu stuff
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        //if here, hide the home button, as we are home
        for (int i = 0; i < menu.size(); i++){
            int menuObjID = menu.getItem(i).getItemId();
            if(menuObjID == R.id.menu_settings){
                menu.getItem(i).setVisible(false);
                break;
            }
        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //need to finish this stuff to complete the menu actions
        Log.d(TAG, "Start of onOptionsItemSelected");
        Intent intent;
        if(dataObj == null){
            Log.d(TAG, "dataObj was null, so I'm going to go ahead and set it to default...");
            dataObj = OperationValues_default.getDefaultValues();
        }

        Bundle bundle;
        switch (item.getItemId()){
            case R.id.menu_help:
//                intent = new Intent(getApplicationContext(), AboutActivity.class);
//                bundle = new Bundle();
//                bundle.putSerializable(OperationValues.DATA_OBJ_NAME, returnCurrentStateAsDataObj());
//                intent.putExtras(bundle);
//                startActivity(intent);
                break;
            case R.id.menu_home:
                Log.d(TAG, "Inside menu home");
                intent = new Intent(getApplicationContext(), MainActivity.class);
                bundle = new Bundle();
                bundle.putSerializable(OperationValues.DATA_OBJ_NAME, dataObj);
//                bundle.putSerializable(MainForLoopThread.DATA_OBJ_NAME, myThread_local);
                Log.d(TAG, "dataObj has been packed and sent to main activity");
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void returnCurrentStateAsDataObj() {
        //this function shall update the current OperationValues, and update any of the fields as needed
        int start1 = integral1_start.getValue();
        int start2 = integral2_start.getValue();
        int start3 = integral3_start.getValue();

        int end1 = integral1_end.getValue();
        int end2 = integral2_end.getValue();
        int end3 = integral3_end.getValue();

        dataObj.setIntegralRanges(start1, start2 , start3, end1, end2, end3);
//        dataObj.setStopOnSuccess(rdgb_stopOnSuccess_Yes_2.isSelected());
        dataObj.updateTotalExpectedOperations();

        Log.d(TAG, "inside returnCurrentStateAsDataObj, enum == " + dataObj.getCPU_OptionsEnum());
//        return dataObj;
    }
}