package com.example.viberlauncher;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    static class LauncherTimer {
        Timer mTimer;
        boolean mScheduled;

        void schedule(TimerTask task, long delay) {
            if (mScheduled) mTimer.cancel();
            else mScheduled = true;
            mTimer = new Timer();
            mTimer.schedule(task, delay);
        }
        void cancel() {
            if (mScheduled) {
                mTimer.cancel();
                mScheduled = false;
            }
        }
    }
    static class Contact implements com.example.viberlauncher.ListItem {
        public int id;
        public String label;
        Contact(int id, String label) {
            this.id = id;
            this.label = label;
        }
        @Override
        public String getText() {
            return label;
        }
    }
    LauncherTimer mTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView list = findViewById(R.id.list);
        mTimer = new LauncherTimer();
        list.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent = new Intent("com.example.viberlauncher.LauncherActivity")
                                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                        startActivity(intent);
                    }
                }, 5000);
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                mTimer.cancel();
                view.performClick();
            }
            return false;
        });
        list.setOnItemClickListener((adapterView, view, i, l) -> {
            int id = ((Contact)adapterView.getAdapter().getItem(i)).id;
            Intent intent = new Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_DEFAULT)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                    .setDataAndType(Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, String.valueOf(id)),
                            "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call");
            startActivity(intent);
        });

        if (!canReadContacts()) {
            requestPermission();
        } else {
            setContacts(list);
        }
    }

    private boolean canReadContacts() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        String[] permissions = {android.Manifest.permission.READ_CONTACTS};
        ActivityCompat.requestPermissions(this, permissions, 100);
    }
    public void setContacts(ListView list) {
        ContentResolver content = getContentResolver();

        String[] projection = new String[]{
                ContactsContract.Data._ID,
                ContactsContract.Data.DISPLAY_NAME,
        };
        String selection = ContactsContract.Data.MIMETYPE + "=?";
        String[] selectionArgs = {"vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call"};
        Cursor cursor = content.query(ContactsContract.Data.CONTENT_URI,
                projection, selection, selectionArgs, null);
        ListAdapter<Contact> adapter = new ListAdapter<>(this);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String displayName = cursor.getString(1);
            adapter.add(new Contact(id, displayName));
        }
        cursor.close();
        list.setAdapter(adapter);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setContacts(findViewById(R.id.list));
                } else {
                    Toast.makeText(this, "The app was not allowed to read your contact", Toast.LENGTH_LONG).show();
                    System.exit(0);
                }
        }
    }
}