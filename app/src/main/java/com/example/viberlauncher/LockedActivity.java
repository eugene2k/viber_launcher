package com.example.viberlauncher;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.Calendar;

import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LockedActivity  extends AppCompatActivity {
    BroadcastReceiver mClockTick = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setDateTime();
        }
    };
    BroadcastReceiver mBatteryChanged = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView batteryStatus = findViewById(R.id.batteryStatus);
            Integer batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            batteryStatus.setText(getString(R.string.charge_level, batteryLevel));
        }
    };
    BroadcastReceiver mBatteryOkay = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Button b = findViewById(R.id.button);
            b.setBackgroundColor(getResources().getColor(R.color.green));
            b.setText(R.string.make_calls);
            b.setEnabled(true);
        }
    };
    BroadcastReceiver mBatteryLow = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Button b = findViewById(R.id.button);
            b.setBackgroundColor(getResources().getColor(R.color.crimson));
            b.setText(R.string.charge_now);
            b.setEnabled(false);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unblock);
        Button b = findViewById(R.id.button);
        b.setOnClickListener(button -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
        b.setBackgroundColor(getResources().getColor(R.color.green));
        b.setText(R.string.make_calls);
        setDateTime();
        registerReceiver(mClockTick, new IntentFilter(Intent.ACTION_TIME_TICK));
        registerReceiver(mBatteryLow, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        registerReceiver(mBatteryOkay, new IntentFilter(Intent.ACTION_BATTERY_OKAY));
        registerReceiver(mBatteryChanged, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    protected void setDateTime() {
        Locale locale = new Locale("ru","RU");
        Date date = Calendar.getInstance().getTime();
        TextView clock = findViewById(R.id.clock);
        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm", locale);
        SimpleDateFormat weekday_format = new SimpleDateFormat("EEEE", locale);
        SimpleDateFormat date_format = new SimpleDateFormat("d MMMM", locale);
        SimpleDateFormat year_format = new SimpleDateFormat("y", locale);
        clock.setText(time_format.format(date));
        TextView yearView = findViewById(R.id.date_year);
        TextView dateView = findViewById(R.id.date);
        TextView weekDayView = findViewById(R.id.weekday);
        yearView.setText(year_format.format(date));
        weekDayView.setText(weekday_format.format(date).toUpperCase());
        dateView.setText(date_format.format(date).toUpperCase());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mClockTick);
        unregisterReceiver(mBatteryLow);
        unregisterReceiver(mBatteryOkay);
        unregisterReceiver(mBatteryChanged);
    }
}
