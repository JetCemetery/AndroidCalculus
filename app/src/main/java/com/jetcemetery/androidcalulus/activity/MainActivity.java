package com.jetcemetery.androidcalulus.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.jetcemetery.androidcalulus.calcOperation.OperationValues;
import com.jetcemetery.androidcalulus.R;
import com.jetcemetery.androidcalulus.calcOperation.Singleton_MainLoop;
import com.jetcemetery.androidcalulus.helper.OperationValues_default;
import com.jetcemetery.androidcalulus.helper.StartOperationHelper;

import static com.jetcemetery.androidcalulus.R.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static int POST_MESSAGE_IN_RESULTS = 555;
    public static int ONE_CALCULATION_COMPLETED_BATCH = 556;
    public static int ONE_CALCULATION_COMPLETED = 557;
    public static int SUCCESSFUL_OPERATION = 558;
    public final static String MESSAGE_NAME_ID ="My_data_msg";
    private EditText txtPhone;
    private TextView txtError, txtResults, txt_progressBar2;
    private Button btnStart;
//    private Button btnPause;
    private ProgressBar prbBar_progressBar;
    private Handler updateUIHandler;
    private boolean blockUpdates;

    private String totalOperationExpected;
    private long currentOperationsCompleted;
    private Singleton_MainLoop singleton_Thread;
    private OperationValues dataObj;
//    private OperationValues dataObj2;
//    private String myThread_STR = "THREAD_OBJECT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        Log.d(TAG, "Calling onCreate");
        txtPhone = findViewById(id.txt_phoneID);
        txtError = findViewById(id.errorText);
        btnStart = findViewById(id.btn_start);
//        btnPause = findViewById(id.pauseBtn);
        prbBar_progressBar = findViewById(id.pgBar_Progress_bar);
        txt_progressBar2 = findViewById(id.txt_progress);
        txtResults = findViewById(id.txt_results);
        blockUpdates = false;

        if(defaultInitRequired()){
            Log.d(TAG, "The default Init IS required");
            String un_parsed_phone = String.valueOf(txtPhone.getText());
            dataObj = OperationValues_default.getDefaultValues(un_parsed_phone);
            resetUIData();
        }
        initView();
        createUpdateUiHandler();

    }

    //the onResume function seems to be called every time
    //if that's the case, lets init the singleton thread there
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Calling onResume");
        if(singleton_Thread == null){
            singleton_Thread = Singleton_MainLoop.getInstance();
            Log.d(TAG, "singleton_Thread was null, so I initalized it kinda");
        }

        //by default we must always check to see if an object was passed through an activity
        //if object was passed, then we need to update all of the local UI stuff
        if(DataObjectPassedThoughIntent()){
            Log.d(TAG, "DataObjectPassedThoughIntent has returned true!");
            //if here, object was passed through activity
            //let's over-write our local data object, and see if need to reset the UI

            //line below will over-write the local data object (that was easy)
            dataObj = getPassedDataObject();
            if(dataObj.wasDataChanged()){
                Log.d(TAG, "dataObj.wasDataChanged has returned true!");
                //if here, data was changed, we need to
                //kill threads if applicable
                //update the UI
                safeStopAllThreads();
                resetUIData();
            }
            else{
                //if here, then we need to resume the thread, if applicable
                Log.d(TAG, "dataObj.wasDataChanged has returned false, resuming threads!");
                safeResumeAllThreads();
            }

        }else{
            //if here, then no data object was passed through activity
            //keep the local one, and resume threads if applicable
            //unless local data object is null, in that case reset the whole kit and caboodle
            if(dataObj == null){
                //we should NEVER get here, data object is null
                //kill all threads, set default data object, update UI
                safeStopAllThreads();
                String un_parsed_phone = String.valueOf(txtPhone.getText());
                dataObj = OperationValues_default.getDefaultValues(un_parsed_phone);
                resetUIData();
            }
            else{
                //if here, then we can resume the threads, if applicable
                safeResumeAllThreads();
            }
        }
    }

    private void resetUIData() {
        //helper function
        //this function is called when we need to reset the data object counter, and progress bar
        //data object should have been initialized so we good on that
        dataObj.setCurrentOpCompleted(0);
        currentOperationsCompleted = dataObj.getTotalOpCompleted();
        totalOperationExpected = dataObj.getTotalOperationExpected();
        txt_progressBar2.setText(dataObj.getInitialProgressText());
        String progressTxt = currentOperationsCompleted + " / " + totalOperationExpected;
        txt_progressBar2.setText(progressTxt);
        txtResults.setText("");
    }

    private OperationValues getPassedDataObject(){
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        return (OperationValues) bundle.getSerializable(OperationValues.DATA_OBJ_NAME);
    }

    private boolean defaultInitRequired() {
        Log.d(TAG, "Calling defaultInitRequired");
        //this function shall have two features
        //  1 - Return true if no data object was passed, return false otherwise
        //  2 - if an data object was passed, then check if current data object is null
        //        if local data object is not null, compare to local
        //              if the two are different,
        //                assume the object passed, is going to be the new settings
        //                stop all previous threads if applicable
        //                redraw UI elements
        //

        Intent intent = this.getIntent();
        if(intent == null){
            return true;
        }
        Bundle bundle = intent.getExtras();
        if(bundle == null){
            return true;
        }
        OperationValues passedDataObj = (OperationValues) bundle.getSerializable(OperationValues.DATA_OBJ_NAME);
        if(passedDataObj == null){
            return true;
        }
//        Log.d(TAG, "In defaultInitRequired function, the default Init is NOT required");
        //if here, then when this activity was called, a data object was passed into it
        //lets go ahead and restore the needed data back into the view
        dataObj = passedDataObj;
        currentOperationsCompleted = dataObj.getTotalOpCompleted();
        totalOperationExpected = dataObj.getTotalOperationExpected();
        txtPhone.setText(dataObj.getPhoneNumber());
        txtResults.setText(dataObj.getTextArea());
//        safeResumeAllThreads();
        return false;
    }

    private boolean DataObjectPassedThoughIntent(){
        Intent intent = this.getIntent();
        if(intent == null){
            return false;
        }
        Bundle bundle = intent.getExtras();
        if(bundle == null){
            return false;
        }
        OperationValues passedDataObj = (OperationValues) bundle.getSerializable(OperationValues.DATA_OBJ_NAME);
        if(passedDataObj == null){
            return false;
        }
        return true;
    }

    private void initView() {
//        Log.d(TAG, "Calling initView");
        //Section below shall initialize the code need when the start button is clicked
        //IF QC passes inside the code, call the MainForLoopThread2 on a new thread
        //passing the data object and the handler thread
        txtError.setVisibility(TextView.GONE);
        updateProgressBar_and_Text();

        btnStart.setOnClickListener(v -> {
            txtError.setVisibility(TextView.GONE);
            StartOperationHelper helperObj = new StartOperationHelper(dataObj);
            if(singleton_Thread != null){
                Log.d(TAG, "Preventing action");
                Toast.makeText(getApplicationContext(), "Preventing action", Toast.LENGTH_SHORT).show();
                return;
            }
            if(helperObj.misconstruedIntegralRange()){
                //should never get here
                //however if here, then the user set the integral start / end in wrong direction
                txtError.setVisibility(TextView.VISIBLE);
                txtError.setText(helperObj.getMisconstruedIntegralRange_text());
                Toast.makeText(getApplicationContext(), "Integral range is out of sequence", Toast.LENGTH_SHORT).show();
                return;
            }
            if(helperObj.numberInputValid(txtPhone)){
                txtError.setVisibility(View.GONE);
                String rawPhone = String.valueOf(txtPhone.getText());
                dataObj.setPhoneNumber(rawPhone);
                InitiateMainForLoopThread();
            }
            else{
                //if here, then number is NOT valid
                txtError.setVisibility(View.VISIBLE);
                txtError.setText(string.error_msg);
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        });

        txtPhone.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                //if here, then text has changed
                //check to see if we have any threads running
                //  if threads are running, kill em
                //  if no threads are running, do nothing
                safeStopAllThreads();
                String rawPhone = String.valueOf(txtPhone.getText());
                dataObj.setPhoneNumber(rawPhone);
                resetUIData();

            }
        });
    }

    private void InitiateMainForLoopThread() {
        //this function shall create a new thread, and start the MainForLoopThread2 operation
        String toastMsg = "Cpu count == " + dataObj.getCPU_count_that_is_used();
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
        txtResults.setText("");
        if(singleton_Thread != null){
            //if here, well we have other threads a going
            //kill the threads, and set singleton to null
            singleton_Thread.stopAllThreads();
            singleton_Thread = null;
        }
        Singleton_MainLoop.initInstance();
        singleton_Thread = Singleton_MainLoop.getInstance();
        singleton_Thread.MainForLoopThread(dataObj,updateUIHandler);
        singleton_Thread.StartProcess();
//        lockUI();
    }

//    private void lockUI() {
//        //this method shall lock the phone number stuff
//        //also grey out the phone number
//        txtPhone.setEnabled(false);
//        txtPhone.setBackgroundColor(Color.GRAY);
//    }

    private void updateProgressBar_and_Text() {
        if(!blockUpdates){
            prbBar_progressBar.setProgress(dataObj.operationForProgressBar(currentOperationsCompleted));
        }
        txt_progressBar2.setText(dataObj.getInitialProgressText());
        String progressTxt = currentOperationsCompleted + " / " + totalOperationExpected;
        txt_progressBar2.setText(progressTxt);
    }

    private void safeStopAllThreads() {
        if(singleton_Thread != null){
            singleton_Thread.stopAllThreads();
            singleton_Thread = null;
        }
    }

    private void safeResumeAllThreads() {
        Log.d(TAG, "calling safeResumeAllThreads");
        if(singleton_Thread != null){
            Log.d(TAG, "singleton_Thread was NOT null");
            singleton_Thread.resumeAllThreads(updateUIHandler);
        }
        else{
            Log.d(TAG, "singleton_Thread was null");
        }
    }

    private void safePauseAllThreads() {
        Log.d(TAG, "calling safePauseAllThreads");
        if(singleton_Thread != null){
            singleton_Thread.pauseAllThreads();
        }
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
//        tempDebug();
        Intent intent;
        if(dataObj == null){
            dataObj = OperationValues_default.getDefaultValues();
        }
        Bundle bundle;
        switch (item.getItemId()){
            case id.menu_help:
//                intent = new Intent(getApplicationContext(), AboutActivity.class);
//                bundle = new Bundle();
//                SaveDataObjState();
//                bundle.putSerializable(OperationValues.DATA_OBJ_NAME, dataObj);
//                intent.putExtras(bundle);
//                safePauseAllThreads();
//                startActivity(intent);
                break;
            case id.menu_settings:
                Log.d(TAG, "Inside onOptionsItemSelected about to pack data object");
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                bundle = new Bundle();
                SaveDataObjState();
                dataObj.movingToSettingsPage();
                bundle.putSerializable(OperationValues.DATA_OBJ_NAME, dataObj);
                intent.putExtras(bundle);
//                Log.d(TAG, "Data object packed and ready to be sent to next activity");
//                tempDebug();
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void SaveDataObjState() {
        //this function shall update the current OperationValues, and update any of the fields as needed
        safePauseAllThreads();
        String rawPhone = String.valueOf(txtPhone.getText());
        dataObj.setPhoneNumber(rawPhone);
//        dataObj.setThread(myThread);
        dataObj.setCurrentOpCompleted(currentOperationsCompleted);
        dataObj.setTotalOpCompleted(totalOperationExpected);
        dataObj.setTextArea(txtResults.getText().toString());
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
                        if(messageTypeID == POST_MESSAGE_IN_RESULTS){
                            //Log.d(TAG,"Got into the post message in results section");
                            //if here, then we are going to post a message text of some kind
                            //in the text view area
                            Bundle bundle = msg.getData();
                            String strMSg = bundle.getString(MESSAGE_NAME_ID);
                            if(strMSg != null){
                                if(!strMSg.isEmpty()){
                                    txtResults.append(strMSg);
                                }
                            }
                        }else if(messageTypeID == ONE_CALCULATION_COMPLETED){
                            //if here, then we are going to update the progress bar
                            //and update the text saying how many operation are completed
                            //vs how many more to go
                            currentOperationsCompleted++;
                            updateProgressBar_and_Text();

                        }else if(messageTypeID == ONE_CALCULATION_COMPLETED_BATCH){
                            //if here, then we are doing a batch operation complete
                            //this was an issue when dealing with the UI thread being to slow
                            //so now we batch the stuff up and send it to the UI thread
                            //multi threading, isn't it fun????
                            Bundle bundle = msg.getData();
                            String strMSg = bundle.getString(MESSAGE_NAME_ID);
                            //Log.d(TAG,"strMSg == " + strMSg);
                            if(strMSg != null){
                                if(!strMSg.isEmpty()){
                                    currentOperationsCompleted += Long.valueOf(strMSg);
                                    updateProgressBar_and_Text();
                                }
                            }
                        }else if(messageTypeID == SUCCESSFUL_OPERATION){
                            Log.d(TAG, "messageTypeID == SUCCESSFUL_OPERATION");
                            //if here, then user wanted to stop on first successful operation
                            //kill all threads and set progress to 100;
                            safeStopAllThreads();
                            blockUpdates = true;
                            prbBar_progressBar.setProgress(100);
                            singleton_Thread = null;
                            Toast.makeText(getApplicationContext(), "All threads were stopped because of option selected", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            };
        }
    }
}