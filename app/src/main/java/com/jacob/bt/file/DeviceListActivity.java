package com.jacob.bt.file;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jacob.bt.file.logic.BleDevice;
import com.jacob.bt.file.logic.DataBaseHelper;

import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends Activity implements View.OnClickListener {

    private static final int REQUEST_CODE_ADD_DEVICE = 100;

    private ListView mListViewDevice;

    private DeviceItemAdapter mDeviceAdapter;

    private List<BleDevice> mBleDeviceList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        mListViewDevice = (ListView) findViewById(R.id.list_view_device);
        findViewById(R.id.button_add_device).setOnClickListener(this);

        mDeviceAdapter = new DeviceItemAdapter(DeviceListActivity.this, mBleDeviceList);
        mListViewDevice.setAdapter(mDeviceAdapter);
        mListViewDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BleDevice device = mDeviceAdapter.getItem(position);
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("device", device);
                intent.putExtras(bundle);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        updateList();
    }

    private void updateList() {
        mBleDeviceList.clear();
        List<BleDevice> list = DataBaseHelper.getInstance().getAllDevice();
        mBleDeviceList.addAll(list);
        mDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_add_device:
                Intent intent = new Intent(DeviceListActivity.this, AddBleDeviceActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_DEVICE);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_ADD_DEVICE && data != null) {
            boolean needUpdate = data.getBooleanExtra("update", false);
            if (needUpdate) {
                updateList();
            }
        }
    }
}
