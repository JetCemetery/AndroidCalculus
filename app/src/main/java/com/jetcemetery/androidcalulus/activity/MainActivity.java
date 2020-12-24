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
    private EditText txtPhone4;
    private TextView txtError, txtResults, txt_progressBar2;
    private Button btnStart;
    private View btnPauseStopLayout;
    private Button btnPause_Resume, btnStop;
    private ProgressBar prbBar_progressBar;
    private Handler updateUIHandler;
    private boolean blockUpdates;

    private final String ResumeStr = "Resume";
    private final String PauseStr = "Pause";

    private String local_TotalExpected;
    private long local_currentOperationsCompleted;
    private Singleton_MainLoop singleton_Thread;
    private Singleton_OperationValues localDataObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        Log.d(TAG, "Calling onCreate");
        //TODO - find bug in expected values to run & why total operation is short
        //TODO - Add adapter to the result so you can copy the thing
        //TODO - make sure app doesn't crash from any pages
        //TODO - Add clipart stuff to the Start Pause/Resume and stop buttons

        txtPhone4 = findViewById(id.txt_phoneID);
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

        init_BothSingleTons();
        initView();
        createUpdateUiHandler();
        setUIData();
        safeResumeAllThreads();
    }

    private void init_BothSingleTons() {
        Log.d(TAG, "Inside init_BothSingleTons");
        Singleton_OperationValues.initInstance();
        localDataObj = Singleton_OperationValues.getInstance();
        singleton_Thread = Singleton_MainLoop.getInstance();

        txtPhone4.setText(localDataObj.getPhoneNumber());
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
            if(helperObj.numberInputValid(txtPhone4)){
                txtError.setVisibility(View.GONE);
                InitiateMainForLoopThread();
                LockStartButton();
            }
            else{
                //if here, then number is NOT valid
                txtError.setVisibility(View.VISIBLE);
                txtError.setText(string.error_msg);
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
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
            Log.d(TAG, "about to call stop all threads because btnStop.setOnClickListener");
            safeStopAllThreads();
            resetDataObj();
        });
    }

    private void LockStartButton() {
        //helper method that will do the following:
        //set the phone number.editable to false
        //grey out the phone number color
        txtPhone4.setEnabled(false);
        txtPhone4.setBackgroundColor(Color.GRAY);
    }

    private void UnlockStartButton() {
        //helper method that will do the following:
        //set the phone number.editable to false
        //grey out the phone number color
        txtPhone4.setEnabled(true);
        int myColor = (Color.parseColor("#B7B773"));
        txtPhone4.setBackgroundColor(myColor);
    }

    private void setUIData() {
        //this method shall have two parts to it,
        //Part 1, local counter, local totalExpected
        //  set the local counter to localDataObj counter, set total expected from localDataObj as well
        //Part 2, UI objects
        //  set progress bar, progress text, and result text
        //Part 3, set the localDataObj phone to whatever the current UI has

        //HOWEVER, we also need to take care if data was changed (IE we called this from another activity

        if(localDataObj.wasDataChanged()){
            //if here, then data was changed
            //a call to stop all threads must be made
            //then a clear change made latch
            //When function was data changed is called, it should update total expected operations
            //also reset the progress operations completed

            //issue was found that the safe stop all threads wont work here
            //we need to instantiate the singleton thread...
            Log.d(TAG, "about to call stop all threads because localDataObj.wasDataChanged()");
            safeStopAllThreads();
            localDataObj.clearChangesMadeLatch();
        }
        //part 1 - set local signals to date FROM localDataObj

        local_TotalExpected = localDataObj.getTotalOperationExpected();
        local_currentOperationsCompleted = localDataObj.getCurrentProgressCount();

        //part 2 set progress bar, progress text, and result text
        prbBar_progressBar.setProgress(localDataObj.operationForProgressBar());
        String progressText = local_currentOperationsCompleted + " / " + local_TotalExpected;
        txt_progressBar2.setText(progressText);
        txtResults.setText(localDataObj.getTextArea());

    }

    private void resetDataObj() {
        Log.d(TAG, "Calling resetUIData");
        //Reset function will have three parts to it
        //Part 1, localDataObj
        //  reset the counter, reset the text area, and update the phone number as well
        //Part 2, local counter, local totalExpected
        //  set the local counter to zero, reset total expected from localDataObj
        //Part 3, UI objects
        //  set progress bar, progress text, and result text


        //Part 1, dealing with the localDataObj
        localDataObj.setProgressCount(0);
        localDataObj.setTextArea("");

        //Part 2, local counter, local totalExpected
        local_TotalExpected = localDataObj.getTotalOperationExpected();
        local_currentOperationsCompleted = 0;

        //Part 3, dealing with the UI elements
        prbBar_progressBar.setProgress(0);
        String progressText = "0 / " + local_TotalExpected;
        txt_progressBar2.setText(progressText);
//        if(txtResults != null){
//            //random crash issue here...
//            txtResults.setText("");
//        }

    }


//
//    protected TextWatcher setTextWatcher() {
//        return new TextWatcher() {
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                //do nothing
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                //do nothing
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                //if here, then text has changed
//                //check to see if we have any threads running
//                //  if threads are running, kill em
//                //  if no threads are running, do nothing
//                Log.d(TAG, "about to call stop all threads because afterTextChanged");
//                safeStopAllThreads();
//                resetDataObj();
//            }
//        };
//    }

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
        String progressTxt = local_currentOperationsCompleted + " / " + local_TotalExpected;
        txt_progressBar2.setText(progressTxt);
    }

    private void safeStopAllThreads() {
        Log.d(TAG, "Calling safeStopAllThreads");
        if(singleton_Thread != null){
            singleton_Thread.stopAllThreads();
            singleton_Thread = null;
            //if we killed the thread, lets set the button text...
            btnPause_Resume.setText(PauseStr);
            ShowStart_HidePauseStop(true);

            //last step, reset data object

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
        UnlockStartButton();
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
            LockStartButton();
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
//        removeTextAreaListener();
        Log.d(TAG, "\tonSaveInstanceState - calling safe pause");
        safePauseAllThreads();
        SaveDataObjState();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "calling onRestoreInstanceState");
        String temptxt = localDataObj.getTextArea();
        Log.d(TAG, "calling BEFORE super");
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "calling POST onRestoreInstanceState");
        //if here, then there was a chance that the text was removed from the result area
        //cause, change from landscape to portrait or vice versa...
//        String temptxt = localDataObj.getTextArea();
        if(temptxt != null && !temptxt.isEmpty()){
            txtResults.setText(temptxt);
        }
        updateProgressBar_and_Text();
        safeResumeAllThreads();
//        addTextAreaListener();
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
        Log.d(TAG, "calling SaveDataObjState");
        //This function is called just before moving into a new activity
        //There will be two parts, part 1
        //update localDataObj
        //  update the current counter, and update the textarea only
        //Part 2, remove text watcher
        //There is an issue on the resume function, as the text gets update from the localDataObj
        //the reset function is tripped....

        //part 1, save the data
        localDataObj.setProgressCount(local_currentOperationsCompleted);
        localDataObj.setTextArea(txtResults.getText().toString());

        //part 2, remove text change listener
//        removeTextAreaListener();

        String rawPhone = String.valueOf(txtPhone4.getText());
        localDataObj.setPhoneNumber(rawPhone);
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
                            Log.d(TAG, "about to call stop all threads because of messageTypeID == SUCCESSFUL_OPERATION");
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

//    private void addTextAreaListener() {
////        txtPhone4.addTextChangedListener(setTextWatcher());
//    }
//
//    private void removeTextAreaListener() {
//        txtPhone4.removeTextChangedListener(setTextWatcher());
//    }
}