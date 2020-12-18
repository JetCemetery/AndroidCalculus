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

import com.jetcemetery.androidcalulus.calcOperation.OperationValues;
import com.jetcemetery.androidcalulus.R;
import com.jetcemetery.androidcalulus.helper.OperationValues_default;

public class SettingsActivity extends AppCompatActivity {
    public static String TAG = "SettingsActivity";
    private NumberPicker integral1_start, integral2_start, integral3_start;
    private NumberPicker integral1_end, integral2_end, integral3_end;
    private RadioButton cpu_cnt_Single, cpu_cnt_half, cpu_cnt_all_minus1, cpu_cnt_all;
    private RadioButton stopOnSuccess_Yes, stopOnSuccess_No;
    private OperationValues dataObj;

    private final int numberPicker_setMinValue = 1;
    private final int numberPicker_setMaxValue = 1000000;
//    private final int numberPicker_SetValueStart = 1;
//    private final int numberPicker_SetValueEnd = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
//        Log.d(TAG, "Start of onCreate");
        integral1_start = findViewById(R.id.integral1_start);
        integral2_start = findViewById(R.id.integral2_start);
        integral3_start = findViewById(R.id.integral3_start);

        integral1_end = findViewById(R.id.integral1_end);
        integral2_end = findViewById(R.id.integral2_end);
        integral3_end = findViewById(R.id.integral3_end);
        //CPU Count
        cpu_cnt_Single = findViewById(R.id.cpu_cnt_Single);
        cpu_cnt_half = findViewById(R.id.cpu_cnt_half);
        cpu_cnt_all_minus1 = findViewById(R.id.cpu_cnt_all_minus1);
        cpu_cnt_all = findViewById(R.id.cpu_cnt_all);
        //Stop on first success
        stopOnSuccess_Yes = findViewById(R.id.stopOnSuccess_Yes);
        stopOnSuccess_No = findViewById(R.id.stopOnSuccess_No);

        Intent intent = this.getIntent();
        if(intent != null){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                OperationValues tempObj = (OperationValues) bundle.getSerializable(OperationValues.DATA_OBJ_NAME);
                if(tempObj != null){
                    dataObj = tempObj;
                }
            }
        }

        if(dataObj == null){
            //if here, then for some reason Data object was not initialised
            //this is bad, very bad, so for now set the values to default
            dataObj = OperationValues_default.getDefaultValues();
            Log.d(TAG, "data object set to default values");
        }
        init();
    }

    private void init() {
//        Log.d(TAG, "Inside init");
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
                cpu_cnt_half.setChecked(true);
                break;
            case CPU_ALL_MINUS_1:
                cpu_cnt_all_minus1.setChecked(true);
                break;
            case CPU_ALL:
                cpu_cnt_all.setChecked(true);
                break;
            case CPU_SINGLE:
            default:
                cpu_cnt_Single.setChecked(true);
                break;
        }

        if(dataObj.getStopOnFirstSuccess()){
            stopOnSuccess_Yes.setChecked(true);
        }else{
            stopOnSuccess_No.setChecked(true);
        }

        cpu_cnt_Single.setOnClickListener(init_cpuSingle());
        cpu_cnt_half.setOnClickListener(init_cpuHalf());
        cpu_cnt_all_minus1.setOnClickListener(init_cpuMinus1());
        cpu_cnt_all.setOnClickListener(init_cpuAll());

        //Stop on Success Group
        stopOnSuccess_Yes.setOnClickListener(init_stopOnSuccess_Yes());
        stopOnSuccess_No.setOnClickListener(init_stopOnSuccess_No());

        //set values from the local data object
        integral1_start.setValue(dataObj.alphaStart());
        integral2_start.setValue(dataObj.betaStart());
        integral3_start.setValue(dataObj.gammaStart());

        integral1_end.setValue(dataObj.alphaEnd());
        integral2_end.setValue(dataObj.betaEnd());
        integral3_end.setValue(dataObj.gammaEnd());
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
//        srcIntegral.setValue(numberPicker_SetValueStart);
        srcIntegral.setOnValueChangedListener((picker, oldVal, newVal) -> {
            //if here, then value has changed, so we need to trip the data object indicating so
//            Log.d(TAG, "A value change has been detected in the number picker!");
            dataObj.changesMade();
        });
    }

    private void setEndIntegral(NumberPicker srcIntegral) {
        srcIntegral.setMinValue(numberPicker_setMinValue);
        srcIntegral.setMaxValue(numberPicker_setMaxValue);
//        srcIntegral.setValue(numberPicker_SetValueEnd);
        srcIntegral.setOnValueChangedListener((picker, oldVal, newVal) -> dataObj.changesMade());
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
//        Log.d(TAG, "Start of onOptionsItemSelected");
        Intent intent;
        if(dataObj == null){
//            Log.d(TAG, "dataObj was null, so I'm going to go ahead and set it to default...");
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
//                Log.d(TAG, "Inside menu home");
                intent = new Intent(getApplicationContext(), MainActivity.class);
                bundle = new Bundle();
                SaveDataObjState();
                bundle.putSerializable(OperationValues.DATA_OBJ_NAME, dataObj);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void SaveDataObjState() {
        //this function shall update the current OperationValues, and update any of the fields as needed
        int start1 = integral1_start.getValue();
        int start2 = integral2_start.getValue();
        int start3 = integral3_start.getValue();

        int end1 = integral1_end.getValue();
        int end2 = integral2_end.getValue();
        int end3 = integral3_end.getValue();

        dataObj.setIntegralRanges(start1, start2 , start3, end1, end2, end3);
        dataObj.updateTotalExpectedOperations();
    }
}