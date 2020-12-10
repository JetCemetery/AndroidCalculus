package com.jetcemetery.androidcalulus.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.jetcemetery.androidcalulus.calcOperation3.calcOperation2.MainForLoopThread3;
import com.jetcemetery.androidcalulus.helper.OperationValues_default;
import com.jetcemetery.androidcalulus.helper.StartOperationHelper;

import java.util.ArrayList;

public class MainActivity_take2 extends AppCompatActivity {
    public static int POST_MESSAGE_IN_RESULTS2 = 555;
    public static int ONE_CALULATION_COMPLETED_BATCH = 556;
    public static int ONE_CALULATION_COMPLETED = 557;
    public final static String MESSAGE_NAME_ID2 ="My_data_msg2";
    private EditText txtPhone;
    private TextView txtError, txtResults, txt_progressBar2;
    private Button btnStart;
    private ProgressBar prbBar_progressBar;
    private OperationValues dataObj;
    private Handler updateUIHandler;
    private Runnable myRunnable;
    private MainForLoopThread3 myThread;
    private long currentOperationsCompleted;
    private String totalOperationExpected;

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
        createUpdateUiHandler();
    }

    private void initView() {
        //Section below shall initialize the code need when the start button is clicked
        //IF QC passes inside the code, call the MainForLoopThread2 on a new thread
        //passing the data object and the handler thread
        txtError.setVisibility(TextView.GONE);
        currentOperationsCompleted = 0;
        totalOperationExpected = dataObj.getTotalOperationExpected();
        updateProgressBar_and_Text();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtError.setVisibility(TextView.GONE);
                StartOperationHelper helperObj = new StartOperationHelper(dataObj);
                if(helperObj.misconstruedIntegralRange()){
                    //should never get here
                    //however if here, then the user set the integral start / end in wrong direction
                    txtError.setVisibility(TextView.VISIBLE);
                    txtError.setText(helperObj.getMisconstruedIntegralRange_text());
                    Toast.makeText(getApplicationContext(), "Integral range is out of sequence", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(helperObj.numberInputValid(txtPhone)){
                    //if here, then input is valid
                    txtError.setVisibility(View.GONE);
                    String ParsedNumber = getParsedNumber();
                    dataObj.setPhoneNumber(ParsedNumber);
                    Toast.makeText(getApplicationContext(), "process [" + ParsedNumber + "]", Toast.LENGTH_SHORT).show();
                    InitiateMainForLoopThread();
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

    private void InitiateMainForLoopThread() {
        //this function shall create a new thread, and start the MainForLoopThread2 operation
        txtResults.setText("");
        myRunnable = new Runnable() {
            private long startTime = System.currentTimeMillis();
            public void run() {
                //MainForLoopThread mainThreadedProcess = new MainForLoopThread(dataObj, updateUIHandler);
                //mainThreadedProcess.run();
                myThread = new MainForLoopThread3(dataObj,updateUIHandler);
                myThread.StartProcess();
            }
        };
        myRunnable.run();
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
        prbBar_progressBar.setProgress(dataObj.operationForProgressBar(currentOperationsCompleted));
        txt_progressBar2.setText(dataObj.getInitialProgressText());
        String progressTxt = String.valueOf(currentOperationsCompleted) + " / " + totalOperationExpected;
        txt_progressBar2.setText(progressTxt);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Need this for the menu stuff
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //need to finish this stuff to complete the menu actions
        Intent intent = null;
        //TODO fill in the data object with default if null....
        OperationValues dataObj = null;
        Bundle bundle = null;
        switch (item.getItemId()){
            case R.id.menu_help:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                bundle = new Bundle();
                bundle.putSerializable(OperationValues.DATAOBJ_NAME, dataObj);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.menu_settings:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
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

    /*
        This section of code shall initialize the threaded handler.
        It will contain the code that will process incoming messages.
        It will get the text from the message, and then append to the results area
    */
    private void createUpdateUiHandler()
    {
        if(updateUIHandler == null)
        {
            updateUIHandler = new Handler(Looper.myLooper())
            {
                @Override
                public void handleMessage(Message msg) {
                    // Means the message is sent from child thread.
                    //Log.d(TAG,"Got inside the handle section...");


                    if(msg != null){
                        int messageTypeID = msg.what;
                        if(messageTypeID == POST_MESSAGE_IN_RESULTS2){
                            //Log.d(TAG,"Got into the post message in results section");
                            //if here, then we are going to post a message text of some kind
                            //in the text view area
                            Bundle bundle = msg.getData();
                            String strMSg = bundle.getString(MESSAGE_NAME_ID2);
                            //Log.d(TAG,"strMSg == " + strMSg);
                            if(strMSg != null){
                                //Log.d(TAG,"strMSg != null");
                                if(!strMSg.isEmpty()){
                                    txtResults.append(strMSg);
                                }
                            }
                        }else if(messageTypeID == ONE_CALULATION_COMPLETED){
                            //Log.d(TAG,"Got into increment calculation");
                            //if here, then we are going to update the progress bar
                            //and update the text saying how many operation are completed
                            //vs how many more to go
                            currentOperationsCompleted++;
                            updateProgressBar_and_Text();

                        }else if(messageTypeID == ONE_CALULATION_COMPLETED_BATCH){
                            //if here, then we are doing a batch operation complete
                            //this was an issue when dealing with the UI thread being to slow
                            //so now we batch the stuff up and send it to the UI thread
                            //multi threading, isn't it fun????
                            Bundle bundle = msg.getData();
                            String strMSg = bundle.getString(MESSAGE_NAME_ID2);
                            //Log.d(TAG,"strMSg == " + strMSg);
                            if(strMSg != null){
                                //Log.d(TAG,"strMSg != null");
                                if(!strMSg.isEmpty()){
                                    currentOperationsCompleted += Integer.valueOf(strMSg);
                                    updateProgressBar_and_Text();
                                }
                            }
                        }

                    }
                }
            };
        }
    }
}