package com.example.viberlauncher;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    static class Contact implements ListAdapter.Item {
        public int id;
        public String label;

        Contact(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    BroadcastReceiver mBatteryLowReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(mBatteryChangedReceiver);
            TextView header = findViewById(R.id.header);
            header.setText(context.getString(R.string.charge_now));
            header.setBackgroundColor(getResources().getColor(R.color.crimson));
        }
    };

    BroadcastReceiver mBatteryOkayReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView header = findViewById(R.id.header);
            header.setBackgroundColor(Color.BLACK);
            registerReceiver(mBatteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
    };

    BroadcastReceiver mBatteryChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView header = findViewById(R.id.header);
            int chargeLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            header.setText(getString(R.string.charge_level, chargeLevel));
        }
    };

    CountDownTimer mInactivityTimer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView list = findViewById(R.id.list);

        LinearLayoutManager mgr = new LinearLayoutManager(this);
        list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        list.setLayoutManager(mgr);
        mInactivityTimer = new CountDownTimer(10000, 10001) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                Intent intent = new Intent(MainActivity.this, LockedActivity.class);
                startActivity(intent);
            }
        };
        list.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mInactivityTimer.cancel();
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                view.performClick();
                mInactivityTimer.start();
            }
            return false;
        });

        registerReceiver(mBatteryLowReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        registerReceiver(mBatteryOkayReceiver, new IntentFilter(Intent.ACTION_BATTERY_OKAY));
        registerReceiver(mBatteryChangedReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBatteryLowReceiver);
        unregisterReceiver(mBatteryOkayReceiver);
        unregisterReceiver(mBatteryChangedReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView list = findViewById(R.id.list);

        mInactivityTimer.start();
        if (!canReadContacts()) {
            requestPermission();
        } else {
            setContacts(list);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mInactivityTimer.cancel();
    }

    private boolean canReadContacts() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        String[] permissions = {android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.CALL_PHONE};
        ActivityCompat.requestPermissions(this, permissions, 100);
    }

    public void setContacts(RecyclerView list) {
        String filename = getString(R.string.preferences_file);
        SharedPreferences prefs = getApplicationContext().getSharedPreferences(filename, MODE_PRIVATE);
        String typeString = prefs.getString("typeString", "");
        ContentResolver content = getContentResolver();

        String[] projection = new String[]{
                ContactsContract.Data._ID,
                ContactsContract.Data.DISPLAY_NAME,
        };
        String selection = ContactsContract.Data.MIMETYPE + "=?";
        String[] selectionArgs = {typeString};
        Cursor cursor = content.query(ContactsContract.Data.CONTENT_URI,
                projection, selection, selectionArgs, null);
        ListAdapter<Contact> adapter = new ListAdapter<Contact>(this) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LinearLayout l = new LinearLayout(mContext);
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int hP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0f, metrics);
                {
                    ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                    l.setLayoutParams(p);
                    l.setPadding(0, 30, 0, 30);
                }
                {
                    TextView tv = new TextView(mContext);
                    tv.setTextSize(18);
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                    p.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
                    p.weight = 1;
                    p.setMarginStart(hP);
                    p.setMarginEnd(hP);
                    tv.setLayoutParams(p);
                    l.addView(tv);
                }
                {
                    Drawable phoneIcon = ResourcesCompat.getDrawable(
                            Resources.getSystem(),
                            android.R.drawable.sym_action_call,
                            null
                    );
                    assert (phoneIcon != null);
                    phoneIcon.setTint(Color.WHITE);
                    ImageButton b = new ImageButton(mContext);
                    b.setImageDrawable(phoneIcon);
                    b.setBackgroundResource(R.drawable.button_background);
                    int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 10.0f, metrics);
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(size, size);
                    p.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
                    p.weight = 0;
                    p.setMarginEnd(hP);
                    p.setMarginStart(hP);
                    b.setLayoutParams(p);
                    l.addView(b);
                }

                return new ViewHolder(l);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                LinearLayout layout = (LinearLayout) holder.getView();
                TextView item = (TextView) layout.getChildAt(0);
                item.setText(mList.get(position).getLabel());
                ImageButton b = (ImageButton) layout.getChildAt(1);
                b.setOnClickListener(view -> onAction(mList.get(position)));
            }

            @Override
            void onAction(Contact item) {
                mInactivityTimer.cancel();
                finish();
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .addCategory(Intent.CATEGORY_DEFAULT)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        .setDataAndType(Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, String.valueOf(item.id)),
                                typeString);
                startActivity(intent);
            }
        };
        assert (cursor != null);
        while (cursor.moveToNext()) {
            try {
                int id = cursor.getInt(0);
                String displayName = Objects.requireNonNull(cursor.getString(1));
                adapter.add(new Contact(id, displayName));
            } catch (Exception e) {
                StringBuilder msg = new StringBuilder(e.toString());
                msg.append("\n");
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    msg.append(stackTraceElement.toString());
                    msg.append("\n");
                }
                Log.e("ViberLauncher", msg.toString());
            }
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