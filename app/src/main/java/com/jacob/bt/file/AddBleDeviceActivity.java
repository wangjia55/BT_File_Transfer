package com.jacob.bt.file;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.jacob.bt.file.logic.BleDevice;

public class AddBleDeviceActivity extends Activity implements View.OnClickListener {

    private EditText mEditImei;
    private EditText mEditImsi;
    private EditText mEditEdrMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ble_device);
        mEditImei = (EditText) findViewById(R.id.edit_text_device_imei);
        mEditImsi = (EditText) findViewById(R.id.edit_text_device_imsi);
        mEditEdrMac = (EditText) findViewById(R.id.edit_text_device_edrmac);
        findViewById(R.id.button_save).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_save:
                BleDevice bleDevice = new BleDevice();
                bleDevice.setImei(mEditImei.getText().toString());
                bleDevice.setImsi(mEditImsi.getText().toString());
                bleDevice.setEdrMac(mEditEdrMac.getText().toString());
                bleDevice.save();

                Intent intent = new Intent();
                intent.putExtra("update",true);
                setResult(RESULT_OK,intent);
                finish();
                break;
        }
    }

}
