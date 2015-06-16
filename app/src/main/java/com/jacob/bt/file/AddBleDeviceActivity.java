package com.jacob.bt.file;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.jacob.bt.file.logic.BleDevice;
import com.jacob.bt.file.logic.DataBaseHelper;

public class AddBleDeviceActivity extends Activity implements View.OnClickListener {

    private EditText mEditImei;
    private EditText mEditImsi;
    private EditText mEditEdrMac;
    public static final String IMEI = "imei";
    public static final String TYPE = "type";
    public static final int TYPE_EDIT_DEVICE = 10010;
    public static final int TYPE_CREATE_DEVICE = 10010;
    private BleDevice mBleDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ble_device);
        mEditImei = (EditText) findViewById(R.id.edit_text_device_imei);
        mEditImsi = (EditText) findViewById(R.id.edit_text_device_imsi);
        mEditEdrMac = (EditText) findViewById(R.id.edit_text_device_edrmac);
        findViewById(R.id.button_save).setOnClickListener(this);

        if (getIntent() != null) {
           int  type = getIntent().getIntExtra(TYPE, TYPE_CREATE_DEVICE);
            if (type == TYPE_EDIT_DEVICE) {
                String imei = getIntent().getStringExtra(IMEI);
                mBleDevice = DataBaseHelper.getInstance().getDeviceByImei(imei);
                if (mBleDevice != null) {
                    mEditImei.setText(mBleDevice.getImei());
                    mEditImsi.setText(mBleDevice.getImsi());
                    mEditEdrMac.setText(mBleDevice.getEdrMac());
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_save:
                if (mBleDevice == null) {
                    mBleDevice = new BleDevice();
                }
                mBleDevice.setImei(mEditImei.getText().toString());
                mBleDevice.setImsi(mEditImsi.getText().toString());
                mBleDevice.setEdrMac(mEditEdrMac.getText().toString());
                mBleDevice.save();

                Intent intent = new Intent();
                intent.putExtra("update", true);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

}
