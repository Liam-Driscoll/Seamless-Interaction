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
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity2 extends AppCompatActivity {

    private static final String SERVER_IP = "206.87.9.22"; // ubcsecure
    //private static final String SERVER_IP = "172.20.10.4"; // hotspot
    //private static final String SERVER_IP = "10.0.2.2"; // localhost
    public static final String PORT = "8765";
    private WebSocketClient mWebSocketClient;

    int initial = 1500;
    int goal = 6000;
    int pulse = 30;
    int pulseDelay = 300;
    int steps = 16;
    int goalInhaleIntervalDelay = goal - (2 * pulse + pulseDelay);
    int goalExhaleIntervalDelay = goal - (3 * pulse + 2 * pulseDelay);
    int initialInhaleIntervalDelay = initial - (2 * pulse + pulseDelay);
    int initialExhaleIntervalDelay = initial - (3 * pulse + 2 * pulseDelay);
    int inhaleIntervalDelay = initialInhaleIntervalDelay;
    int exhaleIntervalDelay = initialExhaleIntervalDelay;
    int inhaleStep = (goalInhaleIntervalDelay-inhaleIntervalDelay)/steps;
    int exhaleStep = (goalExhaleIntervalDelay-exhaleIntervalDelay)/steps;

    // vibration pattern layout:
    // {start delay (ms), vibration time (ms), sleep time (ms), vibration time (ms), sleep time (ms)...}
    long [] goalVibrationPattern = {0, pulse, pulseDelay, pulse, pulseDelay, 0, goalInhaleIntervalDelay, pulse, pulseDelay, pulse, pulseDelay, pulse, pulseDelay, 0, goalExhaleIntervalDelay};
    long[] vibrationPattern = {0, pulse, pulseDelay, pulse, inhaleIntervalDelay, pulse, pulseDelay, pulse, pulseDelay, pulse, exhaleIntervalDelay};
    long [] notificationVibrationPattern = {0, 1000};

    private SensorManager mSensorManager;
    private Sensor mSensor;

    int eventNumber = 0;
    int trial = 1;
    int trialTotal = 1;
    String trialString = String.valueOf(trial);

    long vibrationTime = 0;

    // Based on even/odd participant ID
    String type = null;
    double currentHR = 0;
    int maxHR = 0;
    double targetZoneHR = 0.5; // target HR is a fraction of the maximum HR
    int baselineCount = 0;
    double baselineSum = 0;
    int baselineHR = 0;
    String baselineHR_message;
    String patternDuration;

    boolean[] patternComplete = {false};
    boolean[] heartRateComplete = {false};

    String participantID_string;
    String participantID_message;

    // list of events/windows/pages in the order they will be displayed
    String [] events = {"Welcome to the Study","Prepare Baseline HR","Measure Baseline HR","Completed Baseline HR","NASA-TLX Survey","Begin Trial",
            "SAM Survey","Prepare Exercise","Perform Exercise","Completed Exercise","SAM Survey","Prepare Breathing Guidance",
            "Perform Breathing Guidance","Completed Breathing Guidance","SAM Survey","Completed Trial", "Post Study Questionnaire", "Study Complete"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        // keeps screen on while in application
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Button nextButton = findViewById(R.id.button);
        Button backButton = findViewById(R.id.backButton);
        Button skipButton = findViewById(R.id.skipButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        connectWebSocket();
        checkOrder();
        checkAge();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        Log.d("buttonCount", String.valueOf(events.length-1));

        // goes forward an event/window
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prevents "index out of bounds" error
                if (eventNumber == events.length-3){
                    if (trial == 3){
                        Log.d("trial", "1");
                        eventNumber = 4;
                        type = switchType(type);
                        trial = 1;
                        trialString = String.valueOf(trial);
                        trialTotal++;
                    }
                    else {
                        Log.d("trial", "2");
                        trial++;
                        trialTotal++;
                        trialString = String.valueOf(trial);
                        eventNumber = 5;
                    }
                }
                else if (trialTotal >= 6 && eventNumber == 4){
                    eventNumber = events.length-2;  // second last event
                }
                else if (eventNumber == 3){
                    eventNumber += 2;
                }
                else {
                    Log.d("trial", "3");
                    eventNumber++;
                }
                //Log.d("buttonCount", "Next button pressed");
                //Log.d("trial", "Trial: " + trialString);
                String stringEventNumber = String.valueOf(eventNumber);
                Log.d("buttonCount", "Next; " + stringEventNumber);
                changeEvent(events[eventNumber]);
            }
        });

        // goes back an event/window
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // prevents "index out of bounds" error
                if (eventNumber == 5){
                    eventNumber = 5;
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

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventNumber--;
                changeEvent(events[eventNumber]);
            }
        });

        // gives the ability to skip past timer if needed, ADD SKIP BUTTON TO TIMER() WITH CANCEL BUTTON
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventNumber++;
                changeEvent(events[eventNumber]);
            }
        });
    }

    // changes the event/window/page that the smartwatch displays
    public void changeEvent(String caseNum){
        TextView timerText = findViewById(R.id.timerText);
        TextView textView = findViewById(R.id.textView);
        Button nextButton = findViewById(R.id.button);
        Button backButton = findViewById(R.id.backButton);
        Button skipButton = findViewById(R.id.skipButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        // switch statement containing each event/window/page's details
        switch(caseNum){
            case "Prepare Baseline HR":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Prepare for Baseline Heart Rate Measurement");
                nextButton.setText("Start");
                break;
            case "Measure Baseline HR":
                timerText.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.GONE);
                backButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                textView.setText("Baseline Heart Rate Measurement");
                timer(60000);
                break;
            case "Completed Baseline HR":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Baseline Heart Rate Measurement Complete");
                nextButton.setText("Next");
                break;
            case "NASA-TLX Survey":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Complete the NASA-TLX Survey");
                nextButton.setText("Done");
                break;
            case "SAM Survey":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Complete the SAM Survey");
                nextButton.setText("Done");
                break;
            case "Prepare Exercise":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Prepare to Perform High Knees");
                nextButton.setText("Start");
                break;
            case "Perform Exercise":
                timerText.setVisibility(View.GONE);
                backButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                textView.setText("Perform High Knees");
                timer(180000);
                break;
            case "Completed Exercise":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("High Knees Complete");
                nextButton.setText("Next");
                break;
            case "Prepare Breathing Guidance":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Prepare for Breathing Guidance " + type);
                nextButton.setText("Start");
                resetVibration(initialInhaleIntervalDelay, initialExhaleIntervalDelay);
                break;
            case "Perform Breathing Guidance":
                timerText.setVisibility(View.GONE);
                backButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.VISIBLE);
                textView.setText("Breathing Guidance " + type);
                resetVibration(initialInhaleIntervalDelay, initialExhaleIntervalDelay);
                guidanceVibrate(0);
                timer(300000);
                break;
            case "Completed Breathing Guidance":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Breathing Guidance " + type + " Complete");
                nextButton.setText("Next");
                break;
            case "Completed Trial":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Trial " + trialString + " Complete");
                nextButton.setText("Next");
                break;
            case "Begin Trial":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Trial " + trialString);
                nextButton.setText("Begin");
                break;
            case "Post Study Questionnaire":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Complete the Post Study Questionnaire");
                nextButton.setText("Done");
                break;
            case "Study Complete":
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                backButton.setVisibility(View.GONE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("Study Complete\n \nThanks for Participating!");
                break;
            default:
                timerText.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
                backButton.setVisibility(View.VISIBLE);
                skipButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                textView.setText("ERROR");
                nextButton.setText("");
                break;
        }
    }

    // creates a countdown timer and displays it on the watch
    public void timer(long duration){
        TextView timerText = findViewById(R.id.timerText);
        Button skipButton = findViewById(R.id.skipButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        CountDownTimer countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                String sDuration = String.format(Locale.ENGLISH, "%02d : %02d"
                        , TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        , TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                timerText.setText(sDuration);

                if (heartRateComplete[0] == true){
                    cancel();
                    heartRateComplete[0] = false;
                }
            }

            @Override
            public void onFinish() {
                Log.d("onFinish", "pattern complete: " + patternComplete[0]);
                // changes open loop pattern when the current pattern has finished (open loop increases continually)
                if (events[eventNumber] == "Perform Breathing Guidance" && type == "Y") {
                    changePattern();
                }
                else if (events[eventNumber] == "Perform Breathing Guidance" && type == "X" && patternComplete[0]){
                    changePattern();
                    patternComplete[0] = false;
                }
                else if (events[eventNumber] == "Perform Breathing Guidance" && type == "X" && patternComplete[0] == false){
                    timer(vibrationTime);
                }
                // calculates baseline heart rate once the measurement period is finished
                else if (events[eventNumber] == "Measure Baseline HR"){
                    notificationVibrate();
                    baselineHR = (int) (baselineSum / baselineCount);
                    baselineHR_message = String.valueOf(baselineHR);
                    Log.d("hrBaseline", String.valueOf(baselineHR));
                    eventNumber++;
                    changeEvent(events[eventNumber]);
                }
                else if (events[eventNumber] == "Perform Breathing Guidance" || events[eventNumber] == "Perform Exercise"){
                    cancel();
                    notificationVibrate();
                    eventNumber++;
                    changeEvent(events[eventNumber]);
                }
            }
        }.start();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (events[eventNumber] == "Perform Breathing Guidance") {
                    guidanceVibrate(1);
                    // resets the vibration guidance pattern if closed loop guidance is being given
                    resetVibration(initialInhaleIntervalDelay, initialExhaleIntervalDelay);

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
                if (events[eventNumber] == "Perform Breathing Guidance") {
                    guidanceVibrate(1);
                    // resets the vibration guidance pattern
                    resetVibration(initialInhaleIntervalDelay, initialExhaleIntervalDelay);

                }
                // still calculates baseline heart rate if the measurement period is skipped prematurely
                else if (events[eventNumber] == "Measure Baseline HR"){
                    baselineHR = (int) (baselineSum / baselineCount);
                    baselineHR_message = String.valueOf(baselineHR);
                    Log.d("hrBaseline", String.valueOf(baselineHR));
                }
                countDownTimer.cancel();
                eventNumber++;
                changeEvent(events[eventNumber]);
            }
        });
    }

    // switches to guidance type that has not yet been used
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
            currentHR = Float.parseFloat(heart_rate);
            if (events[eventNumber] == "Perform Exercise"){
                elevatedHR();
            }
            else if (events[eventNumber] == "Perform Breathing Guidance"){
                relaxedHR();
            }
            else if (events[eventNumber] == "Measure Baseline HR"){
                measureBaselineHR(currentHR);
            }

            Log.d("hr",heart_rate);
            createMessage(participantID_message, heart_rate, baselineHR_message, patternDuration, events[eventNumber], trialString, type);
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

    // sends message to server via websocket
    public void sendMessage(String message){
        /*Date currentTime = Calendar.getInstance().getTime(); // gets current time
        String currentTime_string = currentTime.toString();
        String space = " ";
        String comma = ",";*/
        Log.d("sending", message);
        try {
            mWebSocketClient.send(message);
        }
        catch (WebsocketNotConnectedException e){
            System.out.println("ERROR "+e);
        }
    }

    // changes vibration guidance pattern when completion message is received
    public void receiveMessage(String msg){
        Log.i("onFinish", "MESSAGE RECEIVED: " + msg);
        // only changes pattern if the guidance is closed-loop (feedback)
        if (type == "X" && events[eventNumber] == "Perform Breathing Guidance"){
            patternComplete[0] = true;
        }
    }

    public void createMessage(String pID, String hr, String baselineHR, String patternDuration, String event, String trial, String guidanceType){
        String delimiter = ",";
        String event_string = String.valueOf(event);
        String message = String.join(delimiter, pID, hr, baselineHR, patternDuration, trial, guidanceType, event_string);
        sendMessage(message);
        Log.d("createMessage", "Created message: " + message);
    }

    // changes vibration pattern
    public void changePattern(){
        // prevents vibration pattern from increasing beyond goal pattern
        if (inhaleIntervalDelay < goalInhaleIntervalDelay) {
            for (int i=0; i<vibrationPattern.length; i++){
                if (vibrationPattern[i] == inhaleIntervalDelay){
                    vibrationPattern[i] += inhaleStep;
                    inhaleIntervalDelay = (int) vibrationPattern[i];
                }
                else if (vibrationPattern[i] == exhaleIntervalDelay){
                    vibrationPattern[i] += exhaleStep;
                    exhaleIntervalDelay = (int) vibrationPattern[i];

                }
            }
        }
        patternDuration = String.valueOf(inhaleIntervalDelay+2*pulse+pulseDelay);
        guidanceVibrate(0);

        int length = vibrationPattern.length;
        Log.i("pattern", "Pattern changed to:  Inhale Interval Delay - " + vibrationPattern[4] + "   Exhale Interval Delay - " + vibrationPattern[10]);
    }

    // plays vibration pattern based on type of guidance (open/Y or closed/X)
    public void guidanceVibrate(int stop){
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (stop == 1){
            vibrator.cancel();
        }
        else {
            final int indexInPatternToRepeat = 0; //-1: don't repeat, 0: repeat

            vibrationTime = 0;
            for (int i=0; i<vibrationPattern.length; i++){
                vibrationTime += vibrationPattern[i];
            }
            timer(vibrationTime);  // changes open loop pattern once the current pattern has finished (based on timer), so that open loop increases continually
            vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
        }
    }

    // determines order of guidance (open -> closed OR closed -> open) based on participant ID
    public void checkOrder(){
        Intent intent = getIntent();
        // receive the value from the other activity by getStringExtra() method
        // and key must be same which is send by first activity
        participantID_string = intent.getStringExtra("participantID");
        participantID_message = "P" + participantID_string;
        int participantID = Integer.parseInt(participantID_string);
        if (participantID % 2 == 0){
            type = "X";
        }
        else{
            type = "Y";
        }
    }

    // receives value of age from MainActivity
    public void checkAge(){
        Intent intent = getIntent();
        String age_string = intent.getStringExtra("age");
        int age = Integer.parseInt(age_string);
        hrZone(age);
    }

    // determines maximum heart rate based on age (https://www.heart.org/en/healthy-living/fitness/fitness-basics/target-heart-rates)
    public void hrZone(int age){
        if (19 <= age && age < 30){
            maxHR = 200;
        }
        else if (30 <= age && age < 35){
            maxHR = 190;
        }
        else if (35 <= age && age < 40){
            maxHR = 185;
        }
        else if (40 <= age && age < 45){
            maxHR = 180;
        }
        else if (45 <= age && age < 50){
            maxHR = 175;
        }
        else if (50 <= age && age < 55){
            maxHR = 170;
        }
        else if (55 <= age && age < 60){
            maxHR = 165;
        }
        else if (60 <= age && age < 65){
            maxHR = 160;
        }
        else if (65 <= age && age < 70){
            maxHR = 155;
        }
        else if (age >= 70){
            maxHR = 150;
        }
        else{
            maxHR = 200;
        }
    }

    public void elevatedHR(){
        if (currentHR >= maxHR*targetZoneHR){
            heartRateComplete[0] = true;
            guidanceVibrate(1);
            notificationVibrate();   // provides a vibration notification that the timer is finished
            eventNumber++;
            changeEvent(events[eventNumber]);
        }
    }

    public void relaxedHR(){
        if (currentHR < baselineHR*1.05){
            heartRateComplete[0] = true;
            guidanceVibrate(1);
            notificationVibrate();  // provides a vibration notification that the timer is finished
            resetVibration(initialInhaleIntervalDelay, initialExhaleIntervalDelay);
            eventNumber++;
            changeEvent(events[eventNumber]);
        }
    }

    public void measureBaselineHR(double hr){
        if (hr > 0) {
            baselineCount++;
            baselineSum += hr;
        }
    }

    // resets vibration pattern if guidance is over or interrupted (i.e. back, skip buttons pressed)
    public void resetVibration(int inhaleReset, int exhaleReset){
        for (int i=0; i<vibrationPattern.length; i++){
            if (vibrationPattern[i] == inhaleIntervalDelay){
                vibrationPattern[i] = inhaleReset;
            }
            else if (vibrationPattern[i] == exhaleIntervalDelay){
                vibrationPattern[i] = exhaleReset;
            }
        }
        inhaleIntervalDelay = inhaleReset;
        exhaleIntervalDelay = exhaleReset;

        int length = vibrationPattern.length;
        Log.i("pattern", "Pattern Reset To:  Inhale - " + vibrationPattern[4] + "   Exhale - " + vibrationPattern[10]);
        patternDuration = String.valueOf(inhaleIntervalDelay+2*pulse+pulseDelay);
        String reset = "reset";
        sendMessage(reset);
    }

}