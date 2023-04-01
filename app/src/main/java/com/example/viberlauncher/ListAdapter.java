package com.example.viberlauncher;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

abstract class ListAdapter<T extends ListAdapter.Item> extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    interface Item {
        String getLabel();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public View getView() {
            return this.itemView;
        }
    }
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
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    abstract public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);
    abstract public void onBindViewHolder(@NonNull ViewHolder holder, int position);


    abstract void onAction(T item);
}