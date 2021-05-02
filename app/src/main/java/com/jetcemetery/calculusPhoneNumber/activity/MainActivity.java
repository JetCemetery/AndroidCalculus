package com.jetcemetery.calculusPhoneNumber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.jetcemetery.androidcalculus.R;
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_MainLoop;
import com.jetcemetery.calculusPhoneNumber.calcOperation.Singleton_OperationValues;
import com.jetcemetery.calculusPhoneNumber.helper.StartOperationHelper;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CalcRecyclerViewAdapter.ItemClickListener {

    CalcRecyclerViewAdapter adapter;

    private static final String TAG = "MainActivity";
    public static int POST_MESSAGE_IN_RESULTS = 555;
    public static int ONE_CALCULATION_COMPLETED_BATCH = 556;
    public static int ONE_CALCULATION_COMPLETED = 557;
    public static int SUCCESSFUL_OPERATION = 558;
    public final static String MESSAGE_NAME_ID ="My_data_msg";
    private EditText txtPhone;
    private TextView txtError, txt_progressBar;
    private Button btnStart;
    private View btnPauseStopLayout;
    private MaterialButton btnPause_Resume;
    private Button btnStop;
    private ProgressBar prbBar_progressBar;
    private Handler updateUIHandler;
    private boolean blockUpdates;

    private String local_TotalExpected;
    private long local_currentOperationsCompleted;
    private Singleton_MainLoop singleton_Thread;
    private Singleton_OperationValues localDataObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Calling onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Calling onCreate");
        //fixed the keyboard issue popping up each time...
        //https://stackoverflow.com/questions/2496901/android-on-screen-keyboard-auto-popping-up
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        txtPhone = findViewById(R.id.txt_phoneID);
        txtError = findViewById(R.id.errorText);
        btnStart = findViewById(R.id.btn_start);

        btnPause_Resume = findViewById(R.id.btn_PauseResume);
        btnPause_Resume.setText(R.string.strPause);
        btnStop = findViewById(R.id.btn_stop);
        btnPauseStopLayout = findViewById(R.id.layout_pause_stop);

        prbBar_progressBar = findViewById(R.id.pgBar_Progress_bar);
        txt_progressBar = findViewById(R.id.txt_progress);

        //TODO - Add adapter to the result so you can copy the thing
        //txtResults = findViewById(R.id.txt_results);
        ArrayList<String> tempDataFolder = new ArrayList<>();
        tempDataFolder.add("Results");

        RecyclerView recyclerView = findViewById(R.id.resV_result);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CalcRecyclerViewAdapter(this, tempDataFolder);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        blockUpdates = false;

        init_BothSingleTons();
        initView();
        createUpdateUiHandler();
        setUIData();
        safeResumeAllThreads();
        txtPhone.setText(localDataObj.getPhoneNumber());
    }

    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        //when here, we need to
        //  1 - Copy the text into the user clipboard
        //  2 - Toast a message saying formula copied
        String formula = adapter.getItem(position);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Formula", formula);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Formula copied to clipboard", Toast.LENGTH_SHORT).show();;

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Calling onStart");
        init_BothSingleTons();
        safeResumeAllThreads();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Calling onResume");
        init_BothSingleTons();
        safeResumeAllThreads();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Calling onPause");
        safePauseAllThreads();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Calling onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Calling onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "Calling onRestart");
        init_BothSingleTons();
        safeResumeAllThreads();
    }


    private void init_BothSingleTons() {
        Log.d(TAG, "Inside init_BothSingleTons");
        Singleton_OperationValues.initInstance();
        localDataObj = Singleton_OperationValues.getInstance();
        singleton_Thread = Singleton_MainLoop.getInstance();
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
                //you should NEVER get here
                //the singleton thread should always be null
                //just in case, I'll go ahead and call the stop thread and continue...
                safeStopAllThreads();
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
                InitiateMainForLoopThread();
                LockStartButton();
            }
            else{
                //if here, then number is NOT valid
                //this error should come into two favours
                //one there is a letter of some kinda
                //OR there are not enough characters
                boolean lettersFound = false;
                boolean numberNotValid = false;
                int numberCount = 0;
                String rawNumbStr = txtPhone.getText().toString();
                for(char c : rawNumbStr.toCharArray()){
                    if(Character.isAlphabetic(c)){
                        lettersFound = true;
                        break;
                    }
                    if(Character.isDigit(c)){
                        numberCount++;
                    }
                }
                if(numberCount <= 3){
                    numberNotValid = true;
                }
                String errorMsg = "Number Not valid!";
                if(lettersFound){
                    errorMsg = "Number should not have any latin characters";
                }
                else if(numberNotValid){
                    errorMsg = "Number is to small to be a valid phone number";
                }
                txtError.setVisibility(View.VISIBLE);
                txtError.setText(errorMsg);
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        btnPause_Resume.setOnClickListener(v -> {
            String currentTxt = btnPause_Resume.getText().toString();
            if(currentTxt.equalsIgnoreCase("Pause")){
                setPauseResumeButtonTo_Pause(false);
                safePauseAllThreads();
            }else{
                setPauseResumeButtonTo_Pause(true);
                safeResumeAllThreads();
            }
        });

        btnStop.setOnClickListener(v -> {
            Log.d(TAG, "about to call stop all threads because btnStop.setOnClickListener");
            safeStopAllThreads();
            resetDataObj();
        });

    }

//    @SuppressLint("UseCompatLoadingForDrawables")
    private void setPauseResumeButtonTo_Pause(boolean settingToPause) {
        //if true is passed,
        //  set the text of the button to 'pause'
        //  update the icon to have the pause icon
        //
        //if false is passed
        //  set the text of the button to 'resume'
        //  update the icon
//        @SuppressLint("UseCompatLoadingForDrawables") Drawable myIcon;
        Drawable myIcon;
        if(settingToPause){
            myIcon = getResources().getDrawable(R.drawable.ic_pause);
            String PauseStr = "Pause";
            btnPause_Resume.setText(PauseStr);
        }else{
            myIcon = getResources().getDrawable(R.drawable.ic_resume);
            String resumeStr = "Resume";
            btnPause_Resume.setText(resumeStr);

        }
        btnPause_Resume.setIcon(myIcon);
    }

    private void LockStartButton() {
        //helper method that will do the following:
        //set the phone number.editable to false
        //grey out the phone number color
        txtPhone.setEnabled(false);
        txtPhone.setBackgroundColor(Color.GRAY);
    }

    private void UnlockStartButton() {
        //helper method that will do the following:
        //set the phone number.editable to true
        //un-grey out the phone number color
        txtPhone.setEnabled(true);
        int myColor = (Color.parseColor("#B7B773"));
        txtPhone.setBackgroundColor(myColor);
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
        txt_progressBar.setText(progressText);
        adapter.ClearData();
        adapter.AddDataList(localDataObj.nu_GetDataList());
        //txtResults.setText(localDataObj.getTextArea());

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
        txt_progressBar.setText(progressText);
    }

    private void InitiateMainForLoopThread() {
        Log.d(TAG, "Calling InitiateMainForLoopThread");
        //this function shall create a new thread, and start the MainForLoopThread2 operation
        adapter.ClearData();
        //txtResults.setText("");
        localDataObj.setTextArea("");
        if(singleton_Thread != null){
            //if here, well we have other threads a going
            //kill the threads, and set singleton to null
            singleton_Thread.stopAllThreads();
            singleton_Thread = null;
        }
        localDataObj.setPhoneNumber(txtPhone.getText().toString());
        Singleton_MainLoop.initInstance();
        singleton_Thread = Singleton_MainLoop.getInstance();
        singleton_Thread.MainForLoopThread(localDataObj, updateUIHandler);
        singleton_Thread.StartProcess(localDataObj);
        ShowStart_HidePauseStop(false);
    }

    private void updateProgressBar_and_Text() {
        //Log.d(TAG, "Calling updateProgressBar_and_Text");
        if(!blockUpdates){
            prbBar_progressBar.setProgress(localDataObj.operationForProgressBar(local_currentOperationsCompleted));
        }
        String progressTxt = local_currentOperationsCompleted + " / " + local_TotalExpected;
        txt_progressBar.setText(progressTxt);
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

    private void safeStopAllThreads() {
        Log.d(TAG, "safe STOP all threads called");
        if(singleton_Thread != null){
            singleton_Thread.stopAllThreads();
            singleton_Thread = null;
            //if we killed the thread, lets set the button text...
            btnPause_Resume.setText(R.string.strPause);
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
        setPauseResumeButtonTo_Pause(true);
    }

    private void safeResumeAllThreads() {
        Log.d(TAG, "safe RESUME all threads called");
        if(singleton_Thread != null){
            singleton_Thread.resumeAllThreads(updateUIHandler);
            ShowStart_HidePauseStop(false);
            LockStartButton();
        }
        setPauseResumeButtonTo_Pause(true);
    }

    private void safePauseAllThreads() {
        Log.d(TAG, "safe PAUSE all threads called");
        if(singleton_Thread != null){
            singleton_Thread.pauseAllThreads();
        }
        setPauseResumeButtonTo_Pause(false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "calling onSaveInstanceState");
        super.onSaveInstanceState(savedInstanceState);
        safePauseAllThreads();
        SaveDataObjState();
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "calling onRestoreInstanceState");
        //String tempTxt = localDataObj.getTextArea();
        String[] tempTxt = localDataObj.nu_GetDataList();
        super.onRestoreInstanceState(savedInstanceState);
        //if here, then there was a chance that the text was removed from the result area
        //cause, change from landscape to portrait or vice versa...
        adapter.ClearData();
        if(tempTxt != null && tempTxt.length >0){
            //txtResults.setText(tempTxt);
            adapter.AddDataList(tempTxt);
        }
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
            if(menuObjID == R.id.menu_home){
                menu.getItem(i).setVisible(false);
                break;
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //need to finish this stuff to complete the menu actions
        Intent intent;
        switch (item.getItemId()){
            case R.id.menu_settings:
                Log.d(TAG, "Menu button hit, going to menu_settings");
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                SaveDataObjState();
                startActivity(intent);
                break;
            case R.id.menu_about:
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
        //  update the current counter, and update the text area only
        //Part 2, remove text watcher
        //There is an issue on the resume function, as the text gets update from the localDataObj
        //the reset function is tripped....

        //part 1, save the data
        localDataObj.setProgressCount(local_currentOperationsCompleted);
        localDataObj.nu_saveData(adapter.getList());
        //localDataObj.setTextArea(txtResults.getText().toString());

        String rawPhone = String.valueOf(txtPhone.getText());
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
                            //if here, then we are going to post a message text of some kind
                            //in the text view area
                            Bundle bundle = msg.getData();
                            String strMSg = bundle.getString(MESSAGE_NAME_ID);
                            if(strMSg != null){
                                if(!strMSg.isEmpty()){
                                    String[] manyResults = strMSg.split("\n");
                                    adapter.addManyResult(manyResults);
                                }
                            }
                        }else if(messageTypeID == ONE_CALCULATION_COMPLETED){
                            //if here, then we are going to update the progress bar
                            //and update the text saying how many operation are completed
                            //vs how many more to go
                            //  FYI this method is depreciated, but we will keep it around
                            local_currentOperationsCompleted++;
                            updateProgressBar_and_Text();

                        }else if(messageTypeID == ONE_CALCULATION_COMPLETED_BATCH){
                            //if here, then we are doing a batch operation complete
                            //this was an issue when dealing with the UI thread being to slow
                            //so now we batch the stuff up and send it to the UI thread
                            //multi threading, isn't it fun????
                            Bundle bundle = msg.getData();
                            String strMSg = bundle.getString(MESSAGE_NAME_ID);
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
}