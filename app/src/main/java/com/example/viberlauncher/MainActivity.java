package com.example.viberlauncher;

//import android.Manifest;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    CountDownTimer mTimer;
    CountDownTimer mInactivityTimer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView list = findViewById(R.id.list);
        LinearLayoutManager mgr = new LinearLayoutManager(this);
        list.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        list.setLayoutManager(mgr);
        mTimer = new CountDownTimer(5000, 10000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                Intent intent = new Intent(MainActivity.this, LauncherActivity.class);
                startActivity(intent);
            }
        };
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
                mTimer.start();
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                mTimer.cancel();
                view.performClick();
                mInactivityTimer.start();
            }
            return false;
        });

        BroadcastReceiver br = new BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onReceive(Context context, Intent intent) {
                TextView header = findViewById(R.id.header);
                int chargeLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                header.setText(chargeLevel + "%");
            }
        };
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                unregisterReceiver(br);
                TextView header = findViewById(R.id.header);
                header.setText(context.getString(R.string.charge_level_low));
                header.setTextColor(Color.WHITE);
                header.setTextColor(Color.RED);
            }
        }, new IntentFilter(Intent.ACTION_BATTERY_LOW));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                registerReceiver(br, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            }
        }, new IntentFilter(Intent.ACTION_BATTERY_OKAY));
        registerReceiver(br, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView list = findViewById(R.id.list);
        ImageView img = findViewById(R.id.imageView);
        img.setVisibility(View.GONE);
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
        String[] permissions = {android.Manifest.permission.READ_CONTACTS};
        ActivityCompat.requestPermissions(this, permissions, 100);
    }

    public void setContacts(RecyclerView list) {
        ContentResolver content = getContentResolver();

        String[] projection = new String[]{
                ContactsContract.Data._ID,
                ContactsContract.Data.DISPLAY_NAME,
        };
        String selection = ContactsContract.Data.MIMETYPE + "=?";
        String[] selectionArgs = {"vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call"};
        Cursor cursor = content.query(ContactsContract.Data.CONTENT_URI,
                projection, selection, selectionArgs, null);
        ListAdapter<Contact> adapter = new ListAdapter<Contact>(this) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                RelativeLayout l = new RelativeLayout(mContext);
                TextView tv = new TextView(mContext);
                tv.setPadding(10, 0, 0, 0);
                tv.setTextSize(20);
                l.addView(tv);
                Drawable phoneIcon = ResourcesCompat.getDrawable(
                        Resources.getSystem(),
                        android.R.drawable.sym_action_call,
                        null
                );
                assert(phoneIcon != null);
                phoneIcon.setTint(Color.WHITE);
                CustomButton b = new CustomButton(mContext);
                b.setPadding(5,5,5,5);
                b.setDrawable(phoneIcon);
                l.addView(b);
                l.setGravity(Gravity.CENTER_VERTICAL);
                l.setPadding(0, 30, 20, 30);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(150,150);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.RIGHT_OF, tv.getId());
                b.setLayoutParams(params);

                return new ViewHolder(l);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                RelativeLayout layout = (RelativeLayout) holder.getView();
                TextView item = (TextView) layout.getChildAt(0);
                item.setText(mList.get(position).getLabel());
                CustomButton b = (CustomButton) layout.getChildAt(1);
                b.setOnClickListener(view -> onAction(mList.get(position)));
            }

            @Override
            void onAction(Contact item) {
                mInactivityTimer.cancel();
                ImageView img = findViewById(R.id.imageView);
                img.setVisibility(View.VISIBLE);
                Intent intent = new Intent(Intent.ACTION_VIEW)
                        .addCategory(Intent.CATEGORY_DEFAULT)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        .setDataAndType(Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, String.valueOf(item.id)),
                                "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call");
                startActivity(intent);
            }
        };
        assert(cursor != null);
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