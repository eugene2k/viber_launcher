package com.example.viberlauncher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppSettingsActivity extends AppCompatActivity {
    static class ChosenApp implements ListAdapter.Item {
        public String name;
        public String cursorType;

        public ChosenApp(String name, String cursorType) {
            this.name = name;
            this.cursorType = cursorType;
        }

        @Override
        public String getLabel() {
            return name;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView list = findViewById(R.id.list);
        PackageManager pManager = getPackageManager();

        ListAdapter<AppSettingsActivity.ChosenApp> appsList = new ListAdapter<AppSettingsActivity.ChosenApp>(this){
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                TextView tv = new TextView(mContext);
                tv.setPadding(30, 30, 30, 30);
                tv.setTextSize(20);
                return new ViewHolder(tv);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                TextView item = (TextView) holder.getView();
                item.setText(mList.get(position).getLabel());
                item.setOnClickListener(view -> onAction(mList.get(position)));
            }

            @Override
            void onAction(@NonNull AppSettingsActivity.ChosenApp app) {
                String prefs = getString(R.string.preferences_file);
                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences(prefs, MODE_PRIVATE).edit();
                editor.putString("typeString", app.cursorType);
                editor.apply();
                finish();
            }
        };
        List<ResolveInfo> allApps;
        {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            allApps = pManager.queryIntentActivities(intent, 0);
        }
        String typeString;
        for (ResolveInfo ri : allApps) {
            switch(ri.activityInfo.packageName) {
                case "com.viber.voip": typeString = "vnd.android.cursor.item/vnd.com.viber.voip.viber_number_call"; break;
                case "com.whatsapp": typeString = "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"; break;
                case "org.telegram.messenger": typeString = "vnd.android.cursor.item/vnd.org.telegram.messenger.android.call"; break;
                default: continue;
            }
            AppSettingsActivity.ChosenApp app = new AppSettingsActivity.ChosenApp(ri.loadLabel(pManager).toString(), typeString);
            appsList.add(app);
        }
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(appsList);
    }
}