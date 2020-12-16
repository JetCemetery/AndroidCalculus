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
    private Button btnPause;
    private ProgressBar prbBar_progressBar;
    private Handler updateUIHandler;
    private boolean blockUpdates;

    private String totalOperationExpected;
    private long currentOperationsCompleted;
    private Singleton_MainLoop singleton_Thread;
    private OperationValues dataObj;
//    private OperationValues dataObj2;
    private String myThread_STR = "THREAD_OBJECT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);
        Log.d(TAG, "Calling onCreate");
        txtPhone = findViewById(id.txt_phoneID);
        txtError = findViewById(id.errorText);
        btnStart = findViewById(id.btn_start);
        btnPause = findViewById(id.pauseBtn);
        prbBar_progressBar = findViewById(id.pgBar_Progress_bar);
        txt_progressBar2 = findViewById(id.txt_progress);
        txtResults = findViewById(id.txt_results);
        blockUpdates = false;

        if(defaultInitRequired()){
            Log.d(TAG, "The default Init IS required");
            String un_parsed_phone = String.valueOf(txtPhone.getText());
            dataObj = OperationValues_default.getDefaultValues(un_parsed_phone);
            currentOperationsCompleted = dataObj.getTotalOpCompleted();
            totalOperationExpected = dataObj.getTotalOperationExpected();
        }
        initView();
        createUpdateUiHandler();

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btnTxt = btnPause.getText().toString();
                String resumeTXT = "Resume";
                String PauseTXT = "Pause";
                if(btnTxt.equalsIgnoreCase(PauseTXT)){
                    //of here, then we need to pause threads, and change the text to say resume
                    btnPause.setText(resumeTXT);
                    safePauseAllThreads();
                }
                else if(btnTxt.equalsIgnoreCase(resumeTXT)){
                    btnPause.setText(PauseTXT);
                    safeResumeAllThreads();
                }
            }
        });
    }

    private void tempDebug() {
        Log.d(TAG, "tempDebug [start]");
        if(dataObj == null){
            Log.d(TAG, "dataObj == null");
        }
        if(singleton_Thread != null){
            if(singleton_Thread.debug_threadsActive()){
                Log.d(TAG, "tempDebug [end]");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Calling onResume");
        //before enabling/disabling button start, and resuming threads
        //lets check out if a data object was passed, and if it's different from what we have
        if(DataObjectPassedThoughIntent()){
            Log.d(TAG, "DataObjectPassedThoughIntent is true path");
            //if here, then the following happened
            //we did have intent, and we did have a dataObject
            //check to see if local data object is different from what was passed
            if(LocalDB_Object_is_Different_than_passed()){
                Log.d(TAG, "LocalDB_Object_is_Different_than_passed is true path");
                //if here, then we must get the passed object, and then update all the UI
                //to whatever is needed
                //we must also kill any threads if they were started
                if(singleton_Thread != null){
                    //cool, local singleton thread is not null
                    //kill it, and make it null
                    singleton_Thread.stopAllThreads();
                    singleton_Thread = null;
                }
                dataObj = getPassedDataObject();
                updateProgressBar_and_Text();
//                updateUI_Text();
            }else{
                //if here, then we need to update the UI stuff with the local data object
                //then resume any threads if applicable
                updateProgressBar_and_Text();
                if(singleton_Thread != null){
                    singleton_Thread.resumeAllThreads(updateUIHandler);
                }
            }

        }
        else{
            //if here, then no object was passed through with intent,
            //you don't need to do anything, except resume threads if applicable
            if(singleton_Thread == null){
                //if here, then no thread was started
                btnStart.setText("START");
                btnStart.setEnabled(true);
            }
            else{
                //if here, then thread WAS started
                //btnStart.setText("START");
                btnStart.setEnabled(false);
                safeResumeAllThreads();
            }
        }

//        if(singleton_Thread == null){
//            singleton_Thread = Singleton_MainLoop.getInstance();
//            if(singleton_Thread == null){
//                //if here, then thread operation never started!
//                Log.d(TAG, "Singleton Thread was never initiated, skip all work");
//            }
//            else{
//                //if here, then we need to restore the threaded operation!
//                Intent intent = this.getIntent();
//                verifyDataObject(intent);
////                safeResumeAllThreads();
//            }
//        }

        //Log.d(TAG, "On Resume function! in main");
        //the resume activity
        //check to see dataObj that was passed is different from what is saved locally

    }

    private OperationValues getPassedDataObject(){
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        return (OperationValues) bundle.getSerializable(OperationValues.DATA_OBJ_NAME);
    }

    private boolean LocalDB_Object_is_Different_than_passed() {
        //helper function that shall check to see if passed data object is different from local
        //if local is null return true
        //if passed data object is null, return false
        if(dataObj == null){
            return true;
        }
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

        //if here, we now need to compare the local DB object with what was passed
        if(objectsAreDifferent(dataObj, passedDataObj)){
            //if here, then the passed object has changed, and we need to return true
            return true;
        }

        //if here then we need to return false
        return false;
    }

    private boolean objectsAreDifferent(OperationValues localObj, OperationValues passedObj) {
        if(passedObj.identicalDataObj(localObj)){
            return false;
        }
        return true;
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
        Log.d(TAG, "In defaultInitRequired function, the default Init is NOT required");
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

    private void verifyDataObject(Intent srcIntent) {
        Log.d(TAG, "Calling verifyDataObject");
        //this function shall be called on every single activity change
        //the goal is simple
        //check to see if the intent that was invoked into here carries with it a data object
        //that data object holding all of the settings to run the app
        //if the intent does hold a new data object, then check to see if the specs match of the current data object
        //if they are different, then
        //you must kill and threads that are running in main loop -> secondary loop
        //update whatever
        //then save the data object that was passed into the local data object

        boolean resumeThreads_if_applicable = false;
        if(srcIntent != null && dataObj != null){
            //intent not null, good start, as well as dataObj not being null and all.
            Bundle bundle = srcIntent.getExtras();
            if(bundle != null){
                //bundle also not null, looking good

                OperationValues passedDataObj = (OperationValues) bundle.getSerializable(OperationValues.DATA_OBJ_NAME);
                if(passedDataObj != null){
//                    Log.d(TAG, "inside verifyDataObject, passedDataObj enum == " + passedDataObj.getCPU_OptionsEnum());
//                    Log.d(TAG, "inside verifyDataObject, dataObj enum == " + dataObj.getCPU_OptionsEnum());
                    //data object was present inside of intent, we are almost there, one more step
                    if(passedDataObj.identicalDataObj(dataObj)){
                        //if here, then local data object, has the same settings
                        //so do nothing!
                        //except resume threads if applicable
                        //  note, it okay to attempt to resume the threads even if the start button was never clicked
                        //  no threads were created, so nothing will happen, and no errors called.
                        Log.d(TAG, "Function verifyDataObject showed that current data obj and passed are the same");
                        resumeThreads_if_applicable = true;
                    }else{
                        Log.d(TAG, "Function verifyDataObject data object has been updated");
                        //if here, then we need to do the following
                        //over-write the local data object, with the passed data object
                        //kill any threads that are running from previous setting
                        //reset the progress text
                        dataObj = passedDataObj;
                        currentOperationsCompleted = 0;
                        safeStopAllThreads();
                    }
                }
            }
        }

        //at this point data object should have been set
        //and threads killed if applicable
        //if data object is not set, set to default
        //then go ahead and call upon the update GUI stuff
        Log.d(TAG, "Function verifyDataObject Next few steps");
        if(dataObj == null){
            Log.d(TAG, "Function verifyDataObject dataObj == null");
            currentOperationsCompleted = 0;
            safeStopAllThreads();
            dataObj = OperationValues_default.getDefaultValues();
        }
        else{
            Log.d(TAG, "Function verifyDataObject dataObj != null");
            //if here, then data object is all initialized
            //all that needs to be done is to resume the threads if applicable
            if(resumeThreads_if_applicable){

                safeResumeAllThreads();
            }

        }

    }

    private void initView() {
        Log.d(TAG, "Calling initView");
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
                //the next two lines are used to PREVENT the myThread_local from being deleted
                //that is all
//                String tempStr = myThread_local.preventGarbageCollector;
//                Toast.makeText(getApplicationContext(), tempStr, Toast.LENGTH_SHORT).show();
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
    }

    private void InitiateMainForLoopThread() {
        //this function shall create a new thread, and start the MainForLoopThread2 operation
        txtResults.setText("");
        if(singleton_Thread != null){
            singleton_Thread.stopAllThreads();
        }
        Singleton_MainLoop.initInstance();
        singleton_Thread = Singleton_MainLoop.getInstance();
        singleton_Thread.MainForLoopThread(dataObj,updateUIHandler);
        singleton_Thread.StartProcess();
    }

    private void updateProgressBar_and_Text() {
        if(!blockUpdates){
            prbBar_progressBar.setProgress(dataObj.operationForProgressBar(currentOperationsCompleted));
        }
        txt_progressBar2.setText(dataObj.getInitialProgressText());
        String progressTxt = currentOperationsCompleted + " / " + totalOperationExpected;
        txt_progressBar2.setText(progressTxt);
    }

    private void safeStopAllThreads() {
        if(singleton_Thread == null){
            singleton_Thread = Singleton_MainLoop.getInstance();
        }

        if(singleton_Thread != null){
            singleton_Thread.stopAllThreads();
            singleton_Thread = null;
        }
    }

    private void safeResumeAllThreads() {
//        createUpdateUiHandler();
        Log.d(TAG, "calling safeResumeAllThreads");
        if(singleton_Thread == null){
            singleton_Thread = Singleton_MainLoop.getInstance();
        }
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
        if(singleton_Thread == null){
            singleton_Thread = Singleton_MainLoop.getInstance();
        }
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
        tempDebug();
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
                bundle.putSerializable(OperationValues.DATA_OBJ_NAME, dataObj);
//                bundle.putSerializable(MainForLoopThread.DATA_OBJ_NAME, myThread_local);
                intent.putExtras(bundle);
                Log.d(TAG, "Data object packed and ready to be sent to next activity");
                tempDebug();
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