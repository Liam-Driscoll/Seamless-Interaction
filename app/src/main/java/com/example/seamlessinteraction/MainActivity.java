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
                writeParticipantID();
            }
        });
    }

    public void writeParticipantID() {
        String participantID = mParticipantID.getText().toString();
        String fileName = "Participant_" + participantID; // + ".txt";

        // Create the Intent object of this class Context() to Second_activity class
        Intent intent = new Intent(this, MainActivity2.class);
        // now by putExtra method put the value in key, value pair
        // key is message_key by this key we will receive the value, and put the string
        intent.putExtra("fileValue", fileName);
        startActivity(intent);
    }
}