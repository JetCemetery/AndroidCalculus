package com.jetcemetery.calculusPhoneNumber.activity;

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
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import com.jetcemetery.androidcalulus.R;
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_OperationValues;

public class SettingsActivity extends AppCompatActivity {
    public static String TAG = "SettingsActivity";
    private NumberPicker integral1_start, integral2_start, integral3_start;
    private NumberPicker integral1_end, integral2_end, integral3_end;
    private RadioButton cpu_cnt_Single, cpu_cnt_half, cpu_cnt_all_minus1, cpu_cnt_all;
    private RadioButton stopOnSuccess_Yes, stopOnSuccess_No;
    private Singleton_OperationValues localDataObj;

    private final int numberPicker_setMinValue = 1;
    private final int numberPicker_setMaxValue = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Log.d(TAG, "Start of onCreate");
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

        initDataObjectSingleton();
        init();
    }

    private void initDataObjectSingleton() {
        //we should NOT need to init the singleton, but just in case....
        Singleton_OperationValues.initInstance();
        localDataObj = Singleton_OperationValues.getInstance();
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
        switch (localDataObj.getCPU_OptionsEnum()){
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

        if(localDataObj.getStopOnFirstSuccess()){
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
        integral1_start.setValue(localDataObj.integral_1_Start());
        integral2_start.setValue(localDataObj.integral_2_Start());
        integral3_start.setValue(localDataObj.integral_3_Start());

        integral1_end.setValue(localDataObj.integral_1_End());
        integral2_end.setValue(localDataObj.integral_2_End());
        integral3_end.setValue(localDataObj.integral_3_End());
    }

    private View.OnClickListener init_cpuSingle() {
        return v -> localDataObj.setCpu_single();
    }

    private View.OnClickListener init_cpuHalf() {
        return v -> localDataObj.setCpu_half();
    }

    private View.OnClickListener init_cpuMinus1() {
        return v -> localDataObj.setCpu_all_m_1();
    }

    private View.OnClickListener init_cpuAll() {
        return v -> localDataObj.setCpu_all();
    }

    private View.OnClickListener init_stopOnSuccess_Yes() {
        return v -> localDataObj.setStopOnSuccess(true);
    }

    private View.OnClickListener init_stopOnSuccess_No() {
        return v -> localDataObj.setStopOnSuccess(false);
    }

    private void setStartIntegral(NumberPicker srcIntegral) {
        srcIntegral.setMinValue(numberPicker_setMinValue);
        srcIntegral.setMaxValue(numberPicker_setMaxValue);
        srcIntegral.setOnValueChangedListener((picker, oldVal, newVal) -> localDataObj.changesMade());
    }

    private void setEndIntegral(NumberPicker srcIntegral) {
        srcIntegral.setMinValue(numberPicker_setMinValue);
        srcIntegral.setMaxValue(numberPicker_setMaxValue);
        srcIntegral.setOnValueChangedListener((picker, oldVal, newVal) -> localDataObj.changesMade());
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

        switch (item.getItemId()){
            case R.id.menu_about:
                Log.d(TAG, "Menu button hit, going to menu_about");
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                SaveDataObjState();
                startActivity(intent);
                break;
            case R.id.menu_home:
                Log.d(TAG, "Menu button hit, going to menu_home");
                intent = new Intent(getApplicationContext(), MainActivity.class);
                SaveDataObjState();
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void SaveDataObjState() {
        //this function shall update the current OperationValues, and update any of the fields as needed
        Log.d(TAG, "Inside SaveDataObjState");
        int start1 = integral1_start.getValue();
        int start2 = integral2_start.getValue();
        int start3 = integral3_start.getValue();

        int end1 = integral1_end.getValue();
        int end2 = integral2_end.getValue();
        int end3 = integral3_end.getValue();

        localDataObj.setIntegralRanges(start1, start2 , start3, end1, end2, end3);
        localDataObj.updateTotalExpectedOperations();
    }
}