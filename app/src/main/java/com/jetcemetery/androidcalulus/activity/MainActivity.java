package com.jetcemetery.androidcalulus.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.jetcemetery.androidcalulus.calcOperation.Singleton_OperationValues;
//import com.jetcemetery.androidcalulus.helper.OperationValues_default;
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
    private View btnPauseStopLayout;
    private Button btnPause_Resume, btnStop;
    private ProgressBar prbBar_progressBar;
    private Handler updateUIHandler;
    private boolean blockUpdates;

    private final String ResumeStr = "Resume";
    private final String PauseStr = "Pause";

//    private String totalOperationExpected;
    private long local_currentOperationsCompleted;
    private Singleton_MainLoop singleton_Thread;
    private Singleton_OperationValues localDataObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        Log.d(TAG, "Calling onCreate");
        //TODO - fix the issue about changing from landscpae into other mode
        //TODO - find bug in expected values to run & why total operation is short

        txtPhone = findViewById(id.txt_phoneID);
        txtError = findViewById(id.errorText);
        btnStart = findViewById(id.btn_start);

        btnPause_Resume = findViewById(id.btn_pause2);
        btnPause_Resume.setText(PauseStr);
        btnStop = findViewById(id.btn_stop2);
        btnPauseStopLayout = findViewById(id.layout_pause_stop);

        prbBar_progressBar = findViewById(id.pgBar_Progress_bar);
        txt_progressBar2 = findViewById(id.txt_progress);
        txtResults = findViewById(id.txt_results);
        blockUpdates = false;
        initDataObjectSingleton();
        initView();
        createUpdateUiHandler();
        setUIData();
    }

    private void setUIData() {
        //this method shall update the needed UI elements
        local_currentOperationsCompleted = localDataObj.getCurrentProgressCount();
        txt_progressBar2.setText(localDataObj.getInitialProgressText());
        prbBar_progressBar.setProgress(localDataObj.operationForProgressBar(local_currentOperationsCompleted));
    }

    private void initDataObjectSingleton() {
        Log.d(TAG, "Inside initDataObjectSingleton");
        Singleton_OperationValues.initInstance();
        localDataObj = Singleton_OperationValues.getInstance();

        if(localDataObj.wasDataChanged()){
            safeStopAllThreads();
            resetDataObj();
            localDataObj.clearChangesMadeLatch();
        }
    }

    private void initSingleton_Thread() {
        singleton_Thread = Singleton_MainLoop.getInstance();

        if(singleton_Thread != null){
            safeResumeAllThreads();
        }
    }

    //the onResume function seems to be called every time
    //if that's the case, lets init the singleton thread there
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Calling onResume");
        initDataObjectSingleton();
        initSingleton_Thread();
    }


    private void resetDataObj() {
        Log.d(TAG, "Calling resetUIData");
        //helper function
        //this function is called when we need to reset the data object counter, and progress bar
        //data object should have been initialized so we good on that
        String rawPhone = String.valueOf(txtPhone.getText());
        localDataObj.setPhoneNumber(rawPhone);

        localDataObj.setProgressCount(0);
        local_currentOperationsCompleted = localDataObj.getCurrentProgressCount();
        txt_progressBar2.setText(localDataObj.getInitialProgressText());
        String progressTxt = "0 / " + localDataObj.getCurrentProgressCount();
        txt_progressBar2.setText(progressTxt);
        txtResults.setText("");
        prbBar_progressBar.setProgress(localDataObj.operationForProgressBar(local_currentOperationsCompleted));
    }

    private void initView() {
        Log.d(TAG, "Calling initView");
        //Section below shall initialize the code need when the start button is clicked
        //IF QC passes inside the code, call the MainForLoopThread2 on a new thread
        //passing the data object and the handler thread
        txtError.setVisibility(TextView.GONE);
        ShowStart_HidePauseStop(true);

        btnStart.setOnClickListener(v -> {
            txtError.setVisibility(TextView.GONE);
            StartOperationHelper helperObj = new StartOperationHelper(localDataObj);
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
                localDataObj.setPhoneNumber(rawPhone);
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
                resetDataObj();
            }
        });

        btnPause_Resume.setOnClickListener(v -> {
            String currentTxt = btnPause_Resume.getText().toString();
            if(currentTxt.equalsIgnoreCase(PauseStr)){
                btnPause_Resume.setText(ResumeStr);
                safePauseAllThreads();
            }else{
                btnPause_Resume.setText(ResumeStr);
                safeResumeAllThreads();
            }

        });

        btnStop.setOnClickListener(v -> {
            safeStopAllThreads();
            resetDataObj();
        });
    }

    private void InitiateMainForLoopThread() {
        Log.d(TAG, "Calling InitiateMainForLoopThread");
        //this function shall create a new thread, and start the MainForLoopThread2 operation
        txtResults.setText("");
        if(singleton_Thread != null){
            //if here, well we have other threads a going
            //kill the threads, and set singleton to null
            singleton_Thread.stopAllThreads();
            singleton_Thread = null;
        }
        Singleton_MainLoop.initInstance();
        singleton_Thread = Singleton_MainLoop.getInstance();
        singleton_Thread.MainForLoopThread(localDataObj, updateUIHandler);
        singleton_Thread.StartProcess(localDataObj);
        ShowStart_HidePauseStop(false);
    }

    private void updateProgressBar_and_Text() {
//        Log.d(TAG, "Calling updateProgressBar_and_Text");
        if(!blockUpdates){
            prbBar_progressBar.setProgress(localDataObj.operationForProgressBar(local_currentOperationsCompleted));
        }
        txt_progressBar2.setText(localDataObj.getInitialProgressText());
        String progressTxt = local_currentOperationsCompleted + " / " + localDataObj.getTotalOperationExpected();
        txt_progressBar2.setText(progressTxt);
    }

    private void safeStopAllThreads() {
        Log.d(TAG, "Calling safeStopAllThreads");
        if(singleton_Thread != null){
            singleton_Thread.stopAllThreads();
            singleton_Thread = null;
            //if we killed the thread, lets set the button text...
            btnPause_Resume.setText(PauseStr);
        }
        ShowStart_HidePauseStop(true);

        //I need a 50 m sec delay
        //This multi thread is a killer, I tell the stuff to stop processing, but it keeps going...
        //resume / pause isn't much of a problem. Or any problem, I don't care if that process
        //takes 500 milliseconds to propagate. Only the stop cause an issue...
        Thread Fifty_MS_delay = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(50);
                    resetDataObj();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Fifty_MS_delay.start();
    }

    private void ShowStart_HidePauseStop(boolean showStart) {
        Log.d(TAG, "Calling ShowStart_HidePauseStop");
        //helper function that will take care of setting things visible / invisible
        //if passing true then
        //  show the start button, but hide the pause and stop button
        //if passing false then
        //  hide the start button, but show the pause and stop button
        if(showStart){
            btnPauseStopLayout.setVisibility(View.GONE);
            btnStart.setVisibility(View.VISIBLE);
        }else {
            btnPauseStopLayout.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.GONE);
        }

    }

    private void safeResumeAllThreads() {
        Log.d(TAG, "calling safeResumeAllThreads");
        if(singleton_Thread != null){
            singleton_Thread.resumeAllThreads(updateUIHandler);
            ShowStart_HidePauseStop(false);
        }
        btnPause_Resume.setText(PauseStr);
    }

    private void safePauseAllThreads() {
        Log.d(TAG, "calling safePauseAllThreads");
        if(singleton_Thread != null){
            singleton_Thread.pauseAllThreads();

        }
        btnPause_Resume.setText(ResumeStr);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "calling onSaveInstanceState");
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "\tonSaveInstanceState - calling safe pause");
        safePauseAllThreads();
        SaveDataObjState();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "calling onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        updateProgressBar_and_Text();
        safeResumeAllThreads();
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
        switch (item.getItemId()){
            case id.menu_settings:
                Log.d(TAG, "Menu button hit, going to menu_settings");
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                SaveDataObjState();
                startActivity(intent);
                break;
            case id.menu_about:
                Log.d(TAG, "Menu button hit, going to menu_about");
                intent = new Intent(getApplicationContext(), AboutActivity.class);
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
        String rawPhone = String.valueOf(txtPhone.getText());
        localDataObj.setPhoneNumber(rawPhone);
        localDataObj.setProgressCount(local_currentOperationsCompleted);
        localDataObj.setTextArea(txtResults.getText().toString());
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
                            local_currentOperationsCompleted++;
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
                                    local_currentOperationsCompleted += Long.parseLong(strMSg);
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
                        }
                    }
                }
            };
        }
    }
}