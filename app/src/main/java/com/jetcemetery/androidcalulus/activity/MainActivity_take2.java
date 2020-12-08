package com.jetcemetery.androidcalulus.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jetcemetery.androidcalulus.MainActivityDataObj;
import com.jetcemetery.androidcalulus.R;
import com.jetcemetery.androidcalulus.RenderValues;

public class MainActivity_take2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_take2);
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
        MainActivityDataObj dataObj = null;
        switch (item.getItemId()){
            case R.id.menu_help:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                dataObj = null;//getUserValues(ParsedNumber);
                intent.putExtra(MainActivityDataObj.DATAOBJ_NAME, dataObj);
                startActivity(intent);
                break;
            case R.id.menu_settings:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                dataObj = null;//getUserValues(ParsedNumber);
                intent.putExtra(MainActivityDataObj.DATAOBJ_NAME, dataObj);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}