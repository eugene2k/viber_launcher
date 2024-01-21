package com.example.viberlauncher;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class LockedActivity  extends AppCompatActivity {
    Timer mTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unblock);
        Button b = findViewById(R.id.button);
        b.setOnClickListener(button -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            startActivity(intent);
        });
        Date date = new Date();
        long time = date.getTime();
        long minutes = (time / (1000*60))*(1000*60);
        long milliSecondsLeft = time % (1000*60);
        date.setTime(minutes);
        TextView clock = findViewById(R.id.clock);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
        clock.setText(format.format(date));

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            public void run() {
                Date date = new Date();
                long time = date.getTime();
                long minutes = (time / (1000*60))*(1000*60);
                date.setTime(minutes);
                TextView clock = findViewById(R.id.clock);
                SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
                clock.setText(format.format(date));
            }
        }, milliSecondsLeft, 1000*60);
    }
}
