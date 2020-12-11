package com.jetcemetery.androidcalulus.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
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

import static com.jetcemetery.androidcalulus.R.*;

public class MainActivity_take2 extends AppCompatActivity {
    private static final String TAG = "MainActivity_take2";
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
//    private Runnable myRunnable;
    private MainForLoopThread3 myThread;
    private long currentOperationsCompleted;
    private String totalOperationExpected;

//    private Button btn_tempPause;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main_take2);
        txtPhone = findViewById(id.txt_phoneID2);
        txtError = findViewById(id.errorText2);
        btnStart = findViewById(id.btn_start2);
        prbBar_progressBar = findViewById(id.pgBar_Progress_bar2);
        txt_progressBar2 = findViewById(id.txt_progress2);
        txtResults = findViewById(id.txt_results2);
        String un_parsed_phone = String.valueOf(txtPhone.getText());
        dataObj = OperationValues_default.getDefaultValues(un_parsed_phone);
        initView();
        createUpdateUiHandler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume function!");
        //the resume activity
        //check to see dataObj that was passed is different from what is saved locally
        Intent intent = this.getIntent();
        verifyDataObject(intent);
        if(myThread != null){
            myThread.resumeAllThreads();
        }
    }

    private void verifyDataObject(Intent srcIntent) {
        //this function shall be called on every single activity change
        //the goal is simple
        //check to see if the intent that was invoked into here carries with it a data object
        //that data object holding all of the settings to run the app
        //if the intent does hold a new data object, then check to see if the specs match of the current data object
        //if they are different, then
        //you must kill and threads that are running in main loop -> secondary loop
        //update whatever
        //then save the data object that was passed into the local data object
        if(srcIntent != null && dataObj != null){
            //intent not null, good start, as well as dataObj not being null and all.
            Bundle bundle = srcIntent.getExtras();
            if(bundle != null){
                //bundle also not null, looking good
                OperationValues passedDataObj = (OperationValues) bundle.getSerializable(OperationValues.DATAOBJ_NAME);
                if(passedDataObj != null){
                    //data object was present inside of intent, we are almost there, one more step
                    if(passedDataObj.identicalDataObj(dataObj)){
                        //if here, then local data object, has the same settings
                        //so do nothing!
                        Log.d(TAG, "Function verifyDataObject showed that current dataobj and passed are the same");
                    }else{
                        //if here, then we need to do the following
                        //over-write the local data object, with the passed data object
                        //kill any threads that are running from previous setting
                        //reset the progress text
                        dataObj = passedDataObj;
                        currentOperationsCompleted = 0;
                        if(myThread != null){
                            myThread.stopAllThreads();
                        }
                    }
                }
            }
        }

        //at this point data object should have been set
        //and threads killed if applicable
        //if data object is not set, set to default
        //then go ahead and call upon the update GUI stuff
        if(dataObj == null){
            currentOperationsCompleted = 0;
            if(myThread != null){
                myThread.stopAllThreads();
            }
            dataObj = OperationValues_default.getDefaultValues();
        }
    }

    private void initView() {
        //Section below shall initialize the code need when the start button is clicked
        //IF QC passes inside the code, call the MainForLoopThread2 on a new thread
        //passing the data object and the handler thread
        txtError.setVisibility(TextView.GONE);
        currentOperationsCompleted = 0;
        totalOperationExpected = dataObj.getTotalOperationExpected();
        updateProgressBar_and_Text();

        btnStart.setOnClickListener(v -> {
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
                //Toast.makeText(getApplicationContext(), "process [" + ParsedNumber + "]", Toast.LENGTH_SHORT).show();
                InitiateMainForLoopThread();
            }
            else{
                //if here, then number is NOT valid
                txtError.setVisibility(View.VISIBLE);
                txtError.setText(string.error_msg);
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void InitiateMainForLoopThread() {
        //this function shall create a new thread, and start the MainForLoopThread2 operation
        txtResults.setText("");
        myThread = new MainForLoopThread3(dataObj,updateUIHandler);
        myThread.StartProcess();
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
        StringBuilder returningStr = new StringBuilder();
        for(char c : buildingData){
            returningStr.append(c);
        }
        return returningStr.toString();
    }

    private void updateProgressBar_and_Text() {
        prbBar_progressBar.setProgress(dataObj.operationForProgressBar(currentOperationsCompleted));
        txt_progressBar2.setText(dataObj.getInitialProgressText());
        String progressTxt = currentOperationsCompleted + " / " + totalOperationExpected;
        txt_progressBar2.setText(progressTxt);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Need this for the menu stuff
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        //if here, hide the home button, as we are home
        for (int i = 0; i < menu.size(); i++){
            int menuObjID = menu.getItem(i).getItemId();
            if(menuObjID == id.menu_home){
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
        Intent intent;
        if(dataObj == null){
            dataObj = OperationValues_default.getDefaultValues();
        }
        Bundle bundle;
        switch (item.getItemId()){
            case id.menu_help:
                intent = new Intent(getApplicationContext(), AboutActivity.class);
                bundle = new Bundle();
                bundle.putSerializable(OperationValues.DATAOBJ_NAME, dataObj);
                intent.putExtras(bundle);
                if(myThread != null){
                    myThread.pauseAllThreads();
                }

                startActivity(intent);
                break;
            case id.menu_settings:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                bundle = new Bundle();
                bundle.putSerializable(OperationValues.DATAOBJ_NAME, dataObj);
                intent.putExtras(bundle);
                if(myThread != null){
                    myThread.pauseAllThreads();
                }
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
                    if(msg != null){
                        int messageTypeID = msg.what;
                        if(messageTypeID == POST_MESSAGE_IN_RESULTS2){
                            //Log.d(TAG,"Got into the post message in results section");
                            //if here, then we are going to post a message text of some kind
                            //in the text view area
                            Bundle bundle = msg.getData();
                            String strMSg = bundle.getString(MESSAGE_NAME_ID2);
                            if(strMSg != null){
                                if(!strMSg.isEmpty()){
                                    txtResults.append(strMSg);
                                }
                            }
                        }else if(messageTypeID == ONE_CALULATION_COMPLETED){
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
                                if(!strMSg.isEmpty()){
                                    currentOperationsCompleted += Integer.parseInt(strMSg);
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