package com.example.seamlessinteraction;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.w3c.dom.Text;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class MainActivity2 extends AppCompatActivity {

    private static final String SERVER_IP = "206.87.9.211"; // ubcsecure
    //private static final String SERVER_IP = "172.20.10.4"; // hotspot
    //private static final String SERVER_IP = "10.0.2.2"; // localhost
    public static final String PORT = "8765";
    private WebSocketClient mWebSocketClient;
    private boolean timerRunning;

    // {start delay (ms), vibration time (ms), sleep time (ms), vibration time (ms), sleep time (ms)...}
    long[] startingVibrationPattern = {0, 0, 100, 100, 100, 100, 100, 100, 100, 100, 100, 2000, 3500, 4000};
    long [] goalVibrationPattern = {0, 0, 100, 100, 100, 100, 100, 100, 100, 100, 100, 4000, 7000, 8000};
    long[] vibrationPattern = {0, 0, 100, 100, 100, 100, 100, 100, 100, 100, 100, 2000, 3500, 4000};
    long [] notificationVibrationPattern = {0, 1000};

    private TextView textView;
    private TextView timerText;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    int eventNumber = 0;
    int trial = 1;
    String trialString = String.valueOf(trial);

    // placeholder, but will be based on even/odd participant ID
    String type = null;
    int exerciseNum = 0;

    // list of events/windows/pages in the order they will be displayed
    int [] events = {20,15,1,2,3,4,5,6,7,8,9,10,1,11,12,13,1,14}; // TAKE OUT 15 AND TEST

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        // keeps screen on while in application
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Button nextButton = findViewById(R.id.button);
        Button backButton = findViewById(R.id.backButton);
        Button skipButton = findViewById(R.id.skipButton);

        connectWebSocket();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        Log.d("buttonCount", String.valueOf(events.length-1));


        checkOrder();

        // goes forward an event/window
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prevents "index out of bounds" error
                if (eventNumber == events.length-1){
                    if (trial == 3){
                        Log.d("trial", "1");
                        eventNumber = 0;  //FIND WHAT MAKES IT GO TO INDEX 0 (NOT HERE)
                        type = switchType(type);
                        trial = 1;
                        trialString = String.valueOf(trial);
                    }
                    else {
                        Log.d("trial", "2");
                        trial++;
                        trialString = String.valueOf(trial);
                        eventNumber = 1;
                    }
                }
                else {
                    Log.d("trial", "3");
                    eventNumber++;
                }
                //Log.d("buttonCount", "Next button pressed");
                //Log.d("trial", "Trial: " + trialString);
                String stringEventNumber = String.valueOf(eventNumber);
                Log.d("buttonCount", "Next: " + stringEventNumber);
                changeEvent(events[eventNumber]);
            }
        });

        // goes back an event/window
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prevents "index out of bounds" error
                if (eventNumber == 1){
                    eventNumber = 1;
                }
                else{
                    eventNumber--;
                }
                Log.d("buttonCount", "Back button pressed");
                String stringEventNumber = String.valueOf(eventNumber);
                //Log.d("buttonCount", "Back: " + stringEventNumber);
                changeEvent(events[eventNumber]);
            }
        });
    }

    // changes the event/window/page that the smartwatch displays
    public void changeEvent(int caseNum){
        TextView timerText = (TextView) findViewById(R.id.timerText);
        TextView textView = (TextView) findViewById(R.id.textView);
        Button nextButton = findViewById(R.id.button);
        Button backButton = findViewById(R.id.backButton);
        Button skipButton = findViewById(R.id.skipButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        String stringEventNumber = String.valueOf(eventNumber);
        String stringExerciseNum = String.valueOf(exerciseNum);
        //Log.d("buttonCount", "Array Index: " + stringEventNumber);
        Log.d("buttonCount", "Case Number: " + String.valueOf(caseNum));
        //Log.d("buttonCount", "Exercise Num: " + stringExerciseNum);
        /*if (exerciseNum == 0){
            exercise = "Jump Squats";
        }
        else if (exerciseNum == 1){
            exercise = "High Knees";
        }
        else{
            exercise = "Jumping Jacks";
        }*/

        // switch statement containing each event/window/page's details
        switch(caseNum){
            case 20:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Complete the SAM and NASA-TLX Survey");
                nextButton.setText("Done");
                break;
            case 1:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Complete the SAM Survey");
                nextButton.setText("Done");
                break;
            case 2:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Perform Jump Squats");
                nextButton.setText("Start");
                break;
            case 3:
                timerText.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                textView.setText("Perform Jump Squats");
                timer(5000);
                break;
            case 4:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Jump Squats Complete");
                nextButton.setText("Next");
                break;
            case 5:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Perform High Knees");
                nextButton.setText("Start");
                break;
            case 6:
                timerText.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                textView.setText("Perform High Knees");
                timer(5000);
                break;
            case 7:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("High Knees Complete");
                nextButton.setText("Next");
                break;
            case 8:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Perform Jumping Jacks");
                nextButton.setText("Start");
                break;
            case 9:
                timerText.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                textView.setText("Perform Jumping Jacks");
                timer(5000);
                break;
            case 10:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Jumping Jacks Complete");
                nextButton.setText("Next");
                break;
            case 11:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Breathing Guidance " + type);
                nextButton.setText("Start");
                break;
            case 12:
                timerText.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                textView.setText("Breathing Guidance " + type);
                resetVibration();
                timer(120000);
                guidanceVibrate(0);
                break;
            case 13:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Breathing Guidance " + type + " Complete");
                nextButton.setText("Next");
                break;
            case 14:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Trial " + trialString + " Complete");
                nextButton.setText("Next");
                break;
            case 15:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Trial " + trialString);
                nextButton.setText("Begin");
                break;
           /* default:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("");
                nextButton.setText("");
                break;*/
        }
    }

    // creates a countdown timer and displays it on the watch
    public void timer(long duration){
        TextView timerText = (TextView) findViewById(R.id.timerText);
        Button skipButton = findViewById(R.id.skipButton);
        Button cancelButton = findViewById(R.id.cancelButton);
        timerRunning = true;

        CountDownTimer countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String sDuration = String.format(Locale.ENGLISH, "%02d : %02d"
                        , TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        , TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                timerText.setText(sDuration);
            }

            @Override
            public void onFinish() {
                // stops the vibration guidance when the timer finishes
                if (duration > 100000) {
                    guidanceVibrate(1);
                    // resets the vibration guidance pattern if closed loop guidance is being given
                    if (type == "X"){
                        resetVibration();
                    }
                }
                // provides a vibration notification that the timer is finished
                notificationVibrate();
                eventNumber++;
                changeEvent(events[eventNumber]);
            }
        }.start();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (duration > 100000) {
                    guidanceVibrate(1);
                    // resets the vibration guidance pattern if closed loop guidance is being given
                    if (type == "X"){
                        resetVibration();
                    }
                }
                countDownTimer.cancel();
                eventNumber--;
                changeEvent(events[eventNumber]);
            }
        });

        // gives the ability to skip past timer if needed, ADD SKIP BUTTON TO TIMER() WITH CANCEL BUTTON
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (duration > 100000) {
                    guidanceVibrate(1);
                    // resets the vibration guidance pattern if closed loop guidance is being given
                    if (type == "X"){
                        resetVibration();
                    }
                }
                countDownTimer.cancel();
                eventNumber++;
                changeEvent(events[eventNumber]);
                String stringEventNumber = String.valueOf(eventNumber);
                Log.d("buttonCount", "Skip: " + stringEventNumber);
            }
        });
    }

    public String switchType(String type){
        if (type == "X"){
            type = "Y";
        }
        else {
            type = "X";
        }
        return type;
    }

    public void notificationVibrate(){
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        final int indexInPatternToRepeat = -1; //-1: don't repeat, 0: repeat
        vibrator.vibrate(notificationVibrationPattern, indexInPatternToRepeat);
    }


    private SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            String heart_rate = String.valueOf(event.values[0]);
            Log.d("hr",heart_rate);
            sendMessage(heart_rate);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST); // can replace "SensorManager.SENSOR_DELAY_NORMAL" with time in microseconds for sampling time
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorListener);
    }

    // connect app to web socket, link to processing web socket server
    public void connectWebSocket()  {
        URI uri;
        try {
            Log.i("Websocket", "Attempting to connect to Websocket");
            uri = new URI("ws://"+SERVER_IP+":8765");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri, new Draft_6455()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Intent intent = getIntent();
                // receive the value by getStringExtra() method
                // and key must be same which is send by first activity
                String fileName = intent.getStringExtra("fileValue");
                mWebSocketClient.send(fileName);
                //mWebSocketClient.send("Watch Connected");
                Log.i("Websocket", "Websocket Opened");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                receiveMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.i("Websocket", "Closed ");
                Log.i("Websocket","Connection closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
            }
            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void sendMessage(String message){
        /*Date currentTime = Calendar.getInstance().getTime(); // gets current time
        String currentTime_string = currentTime.toString();
        String space = " ";
        String comma = ",";*/
        Log.d("sending", message);
        mWebSocketClient.send(message);
    }

    // changes vibration guidance pattern when completion message is received
    public void receiveMessage(String msg){
        Log.i("Websocket", "MESSAGE RECEIVED: " + msg);
        // only changes pattern if the guidance is closed-loop (feedback)
        if (type == "X" && events[eventNumber] == 12){
            changePattern();
        }
    }

    public void changePattern(){
        for(int i=1; i<=3; i++){
            int listSize = vibrationPattern.length;
            vibrationPattern[listSize - i] += 500;
        }
        int length = vibrationPattern.length;
        Log.i("Websocket", "Pattern changed to: " + vibrationPattern[length - 3] + " " + vibrationPattern[length - 2] + " " + vibrationPattern[length - 1]);
        guidanceVibrate(0);
    }

    public void guidanceVibrate(int stop){
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (stop == 1){
            vibrator.cancel();
        }
        else {
            final int indexInPatternToRepeat = 0; //-1: don't repeat, 0: repeat
            if (type == "X") {
                vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
            }
            else{
                vibrator.vibrate(goalVibrationPattern, indexInPatternToRepeat);
            }
        }
    }

    public void checkOrder(){
        Intent intent = getIntent();
        // receive the value from the other activity by getStringExtra() method
        // and key must be same which is send by first activity
        String participantID_string = intent.getStringExtra("participantID");
        int participantID = Integer.parseInt(participantID_string);
        if (participantID % 2 == 0){
            type = "X";
        }
        else{
            type = "Y";
        }
    }

    public void resetVibration(){
        vibrationPattern = startingVibrationPattern;
        int length = vibrationPattern.length;
        Log.d("trial", "Pattern reset to: " + vibrationPattern[length - 3] + " " + vibrationPattern[length - 2] + " " + vibrationPattern[length - 1]);
        String reset = "reset";
        sendMessage(reset);
    }

}