package com.jacob.bt.file;

/**
 * Package : com.jacob.bt.file
 * Author : jacob
 * Date : 15-6-9
 * Description : 这个类是用来xxx
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jacob.bt.file.logic.BleDevice;

import java.util.ArrayList;
import java.util.List;

public class DeviceItemAdapter extends BaseAdapter {

    private List<BleDevice> bleDevices = new ArrayList<BleDevice>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public DeviceItemAdapter(Context context,List<BleDevice> bleDevices) {
        this.bleDevices = bleDevices;
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return bleDevices.size();
    }

    @Override
    public BleDevice getItem(int position) {
        return bleDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.layout_device_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.textViewImei = (TextView) convertView.findViewById(R.id.text_view_imei);
            viewHolder.textViewImsi = (TextView) convertView.findViewById(R.id.text_view_imsi);
            viewHolder.textViewEdrmac = (TextView) convertView.findViewById(R.id.text_view_edrmac);
            convertView.setTag(viewHolder);
        }
        initializeViews(getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(BleDevice bleDevice, ViewHolder holder) {
        holder.textViewImei.setText(bleDevice.getImei());
        holder.textViewImsi.setText(bleDevice.getImsi());
        holder.textViewEdrmac.setText(bleDevice.getEdrMac());
    }

    protected class ViewHolder {
        private TextView textViewImei;
        private TextView textViewImsi;
        private TextView textViewEdrmac;
    }
}

