package com.example.viberlauncher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
interface ListItem {
    String getText();
}
class ListAdapter<T extends ListItem> extends BaseAdapter {

    ArrayList<T> mList;
    Context mContext;

    public ListAdapter(Context ctx) {
        mContext = ctx;
        mList = new ArrayList<>();
    }

    public void add(T item) {
        mList.add(item);
    }
    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            TextView tv = new TextView(mContext);
            tv.setPadding(50,20,10,20);
            tv.setTextSize(24);
            view = tv;
        }
        TextView tv = (TextView) view;
        tv.setText(mList.get(i).getText());
        return view;
    }
}
