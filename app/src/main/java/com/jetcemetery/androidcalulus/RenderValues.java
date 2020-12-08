package com.jetcemetery.androidcalulus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jetcemetery.androidcalulus.calcOperation.MainForLoopThread;
import com.jetcemetery.androidcalulus.calcOperation.simplePost;
import com.jetcemetery.androidcalulus.calcOperation2.MainForLoopThread2;

public class RenderValues extends AppCompatActivity {
    private static String TAG = "RenderValues";
    public final static String MESSAGE_NAME_ID ="My_data_msg";
    public final static int POST_MESSAGE_IN_RESULTS = 777;
    public final static int ONE_CALULATION_COMPLETED = 778;
    public final static int ONE_CALULATION_COMPLETED_BATCH = 779;

    private MainActivityDataObj dataObj;
    private Button btn_goBack, btn_saveData, btn_stats;
    private ProgressBar myProgressBar;
    private TextView resultsArea, myProgressBar_txt;
    private Handler updateUIHandler;
    private Runnable myRunnable;
    private long currentOperationsCompleted;
    private String totalOperationExpected;
    private MainForLoopThread2 myThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_render_values);
        btn_goBack = findViewById(R.id.btn_GoBack);
        btn_saveData = findViewById(R.id.btn_SaveData);
        btn_stats = findViewById(R.id.btn_SaveData);
        resultsArea = findViewById(R.id.renderResults);
        myProgressBar =findViewById(R.id.progress_bar);
        myProgressBar_txt =findViewById(R.id.progress_bar_txt);
        currentOperationsCompleted = 0;
        totalOperationExpected = "";
        createUpdateUiHandler();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dataObj = (MainActivityDataObj)getIntent().getSerializableExtra(MainActivityDataObj.DATAOBJ_NAME); //Obtaining data
            btn_saveData.setEnabled(true);
            btn_stats.setEnabled(true);
            //startNewThreadMainProcess();
            //Toast.makeText(getApplicationContext(), "Should start process!",Toast.LENGTH_SHORT).show();
            //MainForLoopThread mainThreadedProcess = new MainForLoopThread(dataObj, resultsArea);
            testRunThreadRequest();
        }
        else{
            //if here, then no data was sent
            //not sure how this happened but that's life
            btn_saveData.setEnabled(false);
            btn_stats.setEnabled(false);
        }

        btn_goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startNewThreadMainProcess();
            }
        });
        initProgressBar();
    }

    private void initProgressBar() {
        //this function will initialize the progress bar, and the text below to show progress
        if(dataObj == null){
            //should NEVER get here
            myProgressBar.setProgress(0);
            myProgressBar_txt.setText("0 / 0");
        }
        else{
            myProgressBar.setProgress(0);
            myProgressBar_txt.setText(dataObj.getInitialProgressText());
            totalOperationExpected = dataObj.getTotalOperationExpected();
        }
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
                        if(messageTypeID == POST_MESSAGE_IN_RESULTS){
                            //Log.d(TAG,"Got into the post message in results section");
                            //if here, then we are going to post a message text of some kind
                            //in the text view area
                            Bundle bundle = msg.getData();
                            String strMSg = bundle.getString(RenderValues.MESSAGE_NAME_ID);
                            //Log.d(TAG,"strMSg == " + strMSg);
                            if(strMSg != null){
                                //Log.d(TAG,"strMSg != null");
                                if(!strMSg.isEmpty()){
                                    resultsArea.append(strMSg);
                                }
                            }
                        }else if(messageTypeID == ONE_CALULATION_COMPLETED){
                            //Log.d(TAG,"Got into increment calculation");
                            //if here, then we are going to update the progress bar
                            //and update the text saying how many operation are completed
                            //vs how many more to go
                            currentOperationsCompleted++;
                            myProgressBar.setProgress(dataObj.operationForProgressBar(currentOperationsCompleted));
                            String progressTxt = String.valueOf(currentOperationsCompleted) + " / " + totalOperationExpected;
                            myProgressBar_txt.setText(progressTxt);
                        }else if(messageTypeID == ONE_CALULATION_COMPLETED_BATCH){
                            //if here, then we are doing a batch operation complete
                            //this was an issue when dealing with the UI thread being to slow
                            //so now we batch the stuff up and send it to the UI thread
                            //multi threading, isn't it fun????
                            Bundle bundle = msg.getData();
                            String strMSg = bundle.getString(RenderValues.MESSAGE_NAME_ID);
                            //Log.d(TAG,"strMSg == " + strMSg);
                            if(strMSg != null){
                                //Log.d(TAG,"strMSg != null");
                                if(!strMSg.isEmpty()){
                                    currentOperationsCompleted += Integer.valueOf(strMSg);
                                    myProgressBar.setProgress(dataObj.operationForProgressBar(currentOperationsCompleted));
                                    String progressTxt = String.valueOf(currentOperationsCompleted) + " / " + totalOperationExpected;
                                    myProgressBar_txt.setText(progressTxt);
                                }
                            }
                        }
                    }
                }
            };
        }
    }


    private void testRunThreadRequest() {
        //when here, create the new runnable
        //add it to the post delayed
        //then launch it
        //we need to have a runnable to we can remove the thread from the thread pool in case of a back button click and such
        resultsArea.setText("");
        myRunnable = new Runnable() {
            private long startTime = System.currentTimeMillis();
            public void run() {
                //MainForLoopThread mainThreadedProcess = new MainForLoopThread(dataObj, updateUIHandler);
                //mainThreadedProcess.run();
                myThread = new MainForLoopThread2(dataObj,updateUIHandler);
                myThread.StartProcess();
            }
        };
        myRunnable.run();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        myThread.pause();
    }
}