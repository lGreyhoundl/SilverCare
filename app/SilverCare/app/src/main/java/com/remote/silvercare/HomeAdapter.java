package com.remote.silvercare;

import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeAdapter extends BaseAdapter {
    ArrayList<DeviceInform> items = new ArrayList<DeviceInform>();

    Context context;

    public void addItem(DeviceInform deviceInform){
        items.add(deviceInform);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        context = parent.getContext();
        DeviceInform deviceInform = items.get(position);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.home_item, parent, false);
        }

        TextView device_name = convertView.findViewById(R.id.device_name);
        TextView device_status = convertView.findViewById(R.id.device_status);

        device_name.setText(deviceInform.getDeviceName());

        if(deviceInform.getDeviceStatus().equals("true")){
            device_status.setText("움직임 감지됨");
            device_status.setTextColor(Color.parseColor("#1E90FF"));
        }
        else if(deviceInform.getDeviceStatus().equals("false")){
            device_status.setText("움직임 없음");
            device_status.setTextColor(Color.parseColor("#424242"));
        }
        else if(deviceInform.getDeviceStatus().equals("None")){
            device_status.setText("오류");
            device_status.setTextColor(Color.parseColor("#eb4034"));
        }
//        device_status.setText(deviceInform.getDeviceStatus());


//        if(deviceInform.getDeviceStatus().equals("움직임 감지됨")){
//            device_status.setTextColor(Color.parseColor("#1E90FF"));
//        }
//        else if(deviceInform.getDeviceStatus().equals("움직임 없음")){
//            device_status.setTextColor(Color.parseColor("#424242"));
//        }
//        device_status.setText(deviceInform.getDeviceStatus());

        return convertView;
    }


}
