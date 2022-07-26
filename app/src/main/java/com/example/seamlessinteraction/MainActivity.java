package com.example.seamlessinteraction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.seamlessinteraction.databinding.ActivityMainBinding;

public class MainActivity extends Activity {

    private EditText mParticipantID;
    private TextView text;
    int counter = 0;
    String participantID = null;
    String fileName = null;
    String age = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // keeps screen on while in application
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mParticipantID = (EditText) findViewById(R.id.ParticipantID);
        Button nextButton = findViewById(R.id.nextButton);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView text = (TextView) findViewById(R.id.text);
                EditText inputText = (EditText) findViewById(R.id.ParticipantID);
                counter++;
                if (counter == 1) {
                    participantID = writeParticipantID();
                    text.setText("Enter Your Age:");
                    inputText.getText().clear();
                }
                else if (counter == 2){
                    age = writeAge();
                    transferValues();
                }
            }
        });
    }

    public String writeParticipantID() {
        String participantID = mParticipantID.getText().toString();
        return participantID;
    }

    public String writeAge(){
        String age = mParticipantID.getText().toString();
        return age;
    }

    public void transferValues(){
        String fileName = "Participant_" + participantID; // + ".txt";

        // Create the Intent object of this class Context() to Second_activity class
        Intent intent = new Intent(this, MainActivity2.class);
        // now by putExtra method put the value in key, value pair
        // key is message_key by this key we will receive the value, and put the string
        intent.putExtra("fileValue", fileName);
        intent.putExtra("participantID", participantID);
        intent.putExtra("age", age);
        startActivity(intent);
    }


}