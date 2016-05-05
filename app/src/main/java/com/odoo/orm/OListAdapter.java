package com.odoo.orm;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class OListAdapter extends CursorAdapter {
    public static final String TAG = OListAdapter.class.getSimpleName();
    private int res_id = 0;
    private OnViewBindListener mOnViewBindListener;
    private OnNewViewInflateListener mOnNewViewInflateListener;
    private Context mContext;

    public OListAdapter(Context context, Cursor c, int layout) {
        super(context, c, false);
        mContext = context;
        res_id = layout;
    }

    public int getResource() {
        return res_id;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (mOnNewViewInflateListener != null) {
            return mOnNewViewInflateListener.onNewView(context, cursor, parent);
        } else {
            return LayoutInflater.from(mContext).inflate(getResource(), parent,
                    false);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (mOnViewBindListener != null) {
            ListRow row = new ListRow(cursor);
            mOnViewBindListener.onViewBind(view, cursor, row);
        }
    }

    public void setOnViewBindListener(OnViewBindListener bindListener) {
        mOnViewBindListener = bindListener;
    }

    public void setOnNewViewInflateListener(OnNewViewInflateListener listener) {
        mOnNewViewInflateListener = listener;
    }

    public interface OnNewViewInflateListener {
        public View onNewView(Context context, Cursor cursor, ViewGroup parent);
    }

    public interface OnViewBindListener {
        public void onViewBind(View view, Cursor cursor, ListRow row);
    }
}