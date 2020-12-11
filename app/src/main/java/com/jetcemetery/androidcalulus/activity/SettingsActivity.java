package com.jetcemetery.androidcalulus.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.jetcemetery.androidcalulus.OperationValues;
import com.jetcemetery.androidcalulus.R;
import com.jetcemetery.androidcalulus.helper.OperationValues_default;

public class SettingsActivity extends AppCompatActivity {
    public static String TAG = "SettingsActivity";
    private NumberPicker integral1_start, integral2_start, integral3_start;
    private NumberPicker integral1_end, integral2_end, integral3_end;
    private RadioButton rdgb_cpu_cnt_Single_2, rdgb_cpu_cnt_half_2, rdgb_cpu_cnt_all_minus1_2, rdgb_cpu_cnt_all_2;
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
        integral1_start = findViewById(R.id.integral1_start_2);
        integral2_start = findViewById(R.id.integral2_start_2);
        integral3_start = findViewById(R.id.integral3_start_2);

        integral1_end = findViewById(R.id.integral1_end_2);
        integral2_end = findViewById(R.id.integral2_end_2);
        integral3_end = findViewById(R.id.integral3_end_2);
        //CPU Count
        rdgb_cpu_cnt_Single_2 = findViewById(R.id.rdgb_cpu_cnt_Single_2);
        rdgb_cpu_cnt_half_2 = findViewById(R.id.rdgb_cpu_cnt_half_2);
        rdgb_cpu_cnt_all_minus1_2 = findViewById(R.id.rdgb_cpu_cnt_all_minus1_2);
        rdgb_cpu_cnt_all_2 = findViewById(R.id.rdgb_cpu_cnt_all_2);
        //Stop on first success
        rdgb_stopOnSuccess_Yes_2 = findViewById(R.id.rdgb_stopOnSuccess_Yes_2);
        rdgb_stopOnSuccess_No_2 = findViewById(R.id.rdgb_stopOnSuccess_No_2);

        //the label text start/end
        txt_start = findViewById(R.id.lbl_start2);
        txt_end = findViewById(R.id.lbl_end2);

        //on create we need to get
        Intent intent = this.getIntent();
        if(intent != null){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                OperationValues tempObj = (OperationValues) bundle.getSerializable(OperationValues.DATAOBJ_NAME);
                if(tempObj != null){
                    dataObj = tempObj;
                }
            }
        }


        if(dataObj == null){
            //if here, then for some reason Data object was not initialised
            //this is bad, very bad, so for now set the values to default
            dataObj = OperationValues_default.getDefaultValues();
        }
        init();
    }

    private void init() {
        //this function will initialize all of the user buttons/function/action listeners
        //Number Pickers
        setStartIntegral(integral1_start);
        setStartIntegral(integral2_start);
        setStartIntegral(integral3_start);
        setEndIntegral(integral1_end);
        setEndIntegral(integral2_end);
        setEndIntegral(integral3_end);

        //CPU radio group
        rdgb_cpu_cnt_Single_2.setOnClickListener(init_cpuSignle());
        rdgb_cpu_cnt_half_2.setOnClickListener(init_cpuHalf());
        rdgb_cpu_cnt_all_minus1_2.setOnClickListener(init_cpuMinus1());
        rdgb_cpu_cnt_all_2.setOnClickListener(init_cpuAll());

        //Stop on Success Group
        rdgb_stopOnSuccess_Yes_2.setOnClickListener(init_stopOnSuccess_Yes());
        rdgb_stopOnSuccess_No_2.setOnClickListener(init_stopOnSuccess_No());

    }

    @Override
    protected void onResume() {
        super.onResume();
        //the resume activity
        //check to see dataObj that was passed is different from what is saved locally
    }

    private View.OnClickListener init_cpuSignle() {
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //need to finish this stuff to complete the menu actions
        Log.d(TAG, "Start of onOptionsItemSelected");
        Intent intent = null;
        if(dataObj == null){
            Log.d(TAG, "dataObj was null, so I'm going to go ahead and set it to default...");
            dataObj = OperationValues_default.getDefaultValues();
        }

        Bundle bundle = null;
        switch (item.getItemId()){
            case R.id.menu_help:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                bundle = new Bundle();
                bundle.putSerializable(OperationValues.DATAOBJ_NAME, dataObj);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.menu_home:
                Log.d(TAG, "Inside menu home");
                intent = new Intent(getApplicationContext(), MainActivity_take2.class);
                bundle = new Bundle();
                bundle.putSerializable(OperationValues.DATAOBJ_NAME, dataObj);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}