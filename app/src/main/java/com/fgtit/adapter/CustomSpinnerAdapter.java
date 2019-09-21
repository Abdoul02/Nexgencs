package com.fgtit.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.fgtit.fingermap.R;

import java.util.List;

public class CustomSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    Context context;
    private List<String> asr;

    public CustomSpinnerAdapter(Context context,List<String> asr){
        this.context = context;
        this.asr=asr;
    }
    @Override
    public int getCount() {
        return asr.size();
    }

    @Override
    public Object getItem(int position) {
        return asr.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView txt = new TextView(context);
        txt.setPadding(16, 16, 16, 16);
        txt.setTextSize(20);
        txt.setGravity(Gravity.CENTER_VERTICAL);
        txt.setText(asr.get(position));
        txt.setTextColor(Color.parseColor("#000000"));
        return  txt;
    }

    public View getView(int i, View view, ViewGroup viewgroup) {
        TextView txt = new TextView(context);
        txt.setGravity(Gravity.CENTER);
        txt.setPadding(16, 16, 16, 16);
        txt.setTextSize(20);
        txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.drop_down, 0);
        txt.setText(asr.get(i));
        txt.setTextColor(Color.parseColor("#000000"));
        return  txt;
    }
}
