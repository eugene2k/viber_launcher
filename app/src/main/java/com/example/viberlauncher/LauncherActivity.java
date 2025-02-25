package com.example.viberlauncher;


import android.content.Intent;
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

public class LauncherActivity extends AppCompatActivity {
    static class App implements ListAdapter.Item {
        public String name;
        public Intent launcherIntent;

        public App(String name, Intent launcherIntent) {
            this.name = name;
            this.launcherIntent = launcherIntent;
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

        ListAdapter<App> appsList = new ListAdapter<App>(this){
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
            void onAction(@NonNull App app) {
                startActivity(app.launcherIntent);
            }
        };
        List<ResolveInfo> allApps;
        {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            allApps = pManager.queryIntentActivities(intent, 0);
        }
        {
            Intent intent = new Intent(getApplicationContext(), AppSettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            appsList.add(new App(getString(R.string.app_settings), intent));
        }
        for (ResolveInfo ri : allApps) {
            Intent intent = pManager.getLaunchIntentForPackage(ri.activityInfo.packageName);
            if (intent != null) {
                App app = new App((String) ri.loadLabel(pManager), intent);
                appsList.add(app);
            }
        }
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(appsList);
    }
}