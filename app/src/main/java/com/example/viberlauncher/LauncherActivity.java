package com.example.viberlauncher;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class LauncherActivity extends AppCompatActivity {
    static class App implements ListItem {
        public String name;
        public Intent launcherIntent;

        public App(String name, Intent launcherIntent) {
            this.name = name;
            this.launcherIntent = launcherIntent;
        }

        @Override
        public String getText() {
            return name;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView list = findViewById(R.id.list);

        PackageManager pManager = getPackageManager();
        ListAdapter<App> appsList = new ListAdapter<>(this);
        List<ResolveInfo> allApps;
        {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            allApps = pManager.queryIntentActivities(intent, 0);
        }
        for (ResolveInfo ri : allApps) {
            Intent intent = pManager.getLaunchIntentForPackage(ri.activityInfo.packageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY|Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
            App app = new App((String) ri.loadLabel(pManager), intent);
            appsList.add(app);
        }
        list.setAdapter(appsList);
        list.setOnItemClickListener((adapterView, view, i, l) -> {
            App app = (App) adapterView.getAdapter().getItem(i);
            startActivity(app.launcherIntent);
        });
    }
}