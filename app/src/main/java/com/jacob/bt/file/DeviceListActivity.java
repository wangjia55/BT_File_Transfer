package com.jacob.bt.file;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.jacob.bt.file.logic.BleDevice;
import com.jacob.bt.file.logic.DataBaseHelper;
import com.jacob.bt.file.logic.NewPreferenceManager;

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
                NewPreferenceManager.getInstance(getApplicationContext()).saveImei(device.getImei());
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("device", device);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        updateList();

        this.registerForContextMenu(mListViewDevice);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // set context menu title
        menu.setHeaderTitle("请选择一个操作:");
        // add context menu item
        menu.add(0, 1, Menu.NONE, "编辑");
        menu.add(0, 2, Menu.NONE, "删除");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case 1:
                // do something
            {
                BleDevice bleDevice = mBleDeviceList.get(menuInfo.position);
                Intent intent = new Intent(this,AddBleDeviceActivity.class);
                intent.putExtra(AddBleDeviceActivity.TYPE,AddBleDeviceActivity.TYPE_EDIT_DEVICE);
                intent.putExtra(AddBleDeviceActivity.IMEI,bleDevice.getImei());
                startActivityForResult(intent,REQUEST_CODE_ADD_DEVICE);
            }
            break;
            case 2:
                //delete
            {
                BleDevice bleDevice = mBleDeviceList.get(menuInfo.position);
                bleDevice.delete();
                mBleDeviceList.remove(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }
            break;
            default:
                return super.onContextItemSelected(item);
        }
        return true;
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
