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

import com.jetcemetery.androidcalulus.R;
import com.jetcemetery.androidcalulus.calcOperation.OperationValues;
import com.jetcemetery.androidcalulus.helper.OperationValues_default;

public class AboutActivity extends AppCompatActivity {
    private static final String TAG = "AboutActivity";
    //the sending object, and ideally returning object
    private OperationValues dataObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        SetDataObjIntent();
    }

    private void SetDataObjIntent() {
        Intent intent = this.getIntent();
        if(intent != null){
            Bundle bundle = intent.getExtras();
            if(bundle != null){
                OperationValues TempObj = (OperationValues) bundle.getSerializable(OperationValues.DATA_OBJ_NAME);
                if(TempObj != null){
                    dataObj = TempObj;
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Calling onResume");
        SetDataObjIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Need this for the menu stuff
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        //if here, hide the home button, as we are home
        for (int i = 0; i < menu.size(); i++){
            int menuObjID = menu.getItem(i).getItemId();
            if(menuObjID == R.id.menu_about){
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
//        tempDebug();
        Intent intent;
        if(dataObj == null){
            dataObj = OperationValues_default.getDefaultValues();
        }
        Bundle bundle;
        switch (item.getItemId()){
            case R.id.menu_home:
                Log.d(TAG, "Menu button hit, going to menu_home");
                intent = new Intent(getApplicationContext(), MainActivity.class);
                bundle = new Bundle();
                if(dataObj != null){
                    bundle.putSerializable(OperationValues.DATA_OBJ_NAME, dataObj);
                    intent.putExtras(bundle);
                }
                startActivity(intent);
                break;
            case R.id.menu_settings:
                Log.d(TAG, "Menu button hit, going to menu_settings");
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                bundle = new Bundle();
                if(dataObj != null){
                    bundle.putSerializable(OperationValues.DATA_OBJ_NAME, dataObj);
                    intent.putExtras(bundle);
                }
                startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}