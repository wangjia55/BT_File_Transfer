package com.jacob.bt.file;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jacob.ble.connector.core.BleConnectCallback;
import com.jacob.ble.connector.logic.BleCommand;
import com.jacob.ble.connector.logic.BleManager;
import com.jacob.ble.connector.utils.LogUtils;
import com.jacob.bt.spp.core.BtManager;
import com.jacob.bt.spp.impl.BtConnectCallBack;
import com.jacob.bt.spp.impl.BtPullFileCallBack;
import com.jacob.bt.spp.impl.BtTransferDataCallBack;


public class MainActivity extends FragmentActivity implements View.OnClickListener {

    public static final String TAG = "MainActivity";

    private TransFileItemView mTransItemScanDevice;
    private TransFileItemView mTransItemConnectDevice;
    private TransFileItemView mTransItemChangeSpp;
    private TransFileItemView mTransItemPullFile;
    private TransFileItemView mTransItemChangeGatt;
    private EditText mEditTextImsi;
    private Button mButtonStart;

    private BtManager mBtSppManager;
    private BleManager mBleManager;
    private BleDeviceInfo mBleDeviceInfo;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_START_BLE = 10;

    public static final int MSG_START_SCAN = 0x100;
    public static final int MSG_FOUND_DEVICE_SUCCESS = 0x101;
    public static final int MSG_FOUND_DEVICE_ERROR = 0x102;
    public static final int MSG_CONNECT_DEVICE_SUCCESS = 0x103;
    public static final int MSG_CHANGE_TO_SPP = 0x104;
    public static final int MSG_START_PULL_FILE = 0x105;
    public static final int MSG_PULL_FILE_SUCCESS = 0x106;
    public static final int MSG_PULL_FILE_ERROR = 0x107;
    public static final int MSG_CHANGE_TO_GATT_SUCCESS = 0x108;
    public static final int MSG_CHANGE_TO_GATT_FAIL = 0x109;

    public static final String COMMAND_TO_SPP = "90000000000000102";
    public static final String COMMAND_TO_GATT = "90000000000000101";

    private String DEVICE_MAC = "28:6D:47:76:62:61";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_SCAN:
                    mTransItemScanDevice.showProgressState();
                    break;
                case MSG_FOUND_DEVICE_SUCCESS:
                    mTransItemScanDevice.showOKState();
                    mTransItemConnectDevice.showProgressState();
                    break;
                case MSG_FOUND_DEVICE_ERROR:
                    mTransItemScanDevice.showErrorState();
                    break;
                case MSG_CONNECT_DEVICE_SUCCESS:
                    mTransItemConnectDevice.showOKState();
                    mTransItemChangeSpp.showProgressState();
                    break;
                case MSG_CHANGE_TO_SPP:
                    mTransItemChangeSpp.showOKState();
                    mTransItemPullFile.showProgressState();
                    break;
                case MSG_PULL_FILE_SUCCESS:
                    mTransItemPullFile.showOKState();
                    mTransItemChangeGatt.showProgressState();
                    break;
                case MSG_PULL_FILE_ERROR:
                    mTransItemPullFile.showErrorState();
                    break;
                case MSG_START_PULL_FILE:
                    mTransItemPullFile.showProgressState();
                    break;
                case MSG_CHANGE_TO_GATT_SUCCESS:
                    mTransItemChangeGatt.showOKState();
                    break;
                case MSG_CHANGE_TO_GATT_FAIL:
                    mTransItemChangeGatt.showErrorState();
                    break;
            }
        }
    };

    private BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    LogUtils.LOGE(TAG, "BluetoothAdapter.STATE_ON");
                    break;
                case BluetoothAdapter.STATE_OFF:
                    LogUtils.LOGE(TAG, "BluetoothAdapter.STATE_OFF");
                    break;
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTransItemScanDevice = (TransFileItemView) findViewById(R.id.transItemOne);
        mTransItemConnectDevice = (TransFileItemView) findViewById(R.id.transItemTwo);
        mTransItemChangeSpp = (TransFileItemView) findViewById(R.id.transItemThree);
        mTransItemPullFile = (TransFileItemView) findViewById(R.id.transItemFour);
        mTransItemChangeGatt = (TransFileItemView) findViewById(R.id.transItemFive);
        mButtonStart = (Button) findViewById(R.id.button_start);
        mButtonStart.setOnClickListener(this);
        findViewById(R.id.button_send_file).setOnClickListener(this);
        mEditTextImsi = (EditText) findViewById(R.id.edit_text_imsi);

        mTransItemScanDevice.setTransFileItem(TransFileItem.item_one);
        mTransItemConnectDevice.setTransFileItem(TransFileItem.item_two);
        mTransItemChangeSpp.setTransFileItem(TransFileItem.item_three);
        mTransItemPullFile.setTransFileItem(TransFileItem.item_four);
        mTransItemChangeGatt.setTransFileItem(TransFileItem.item_five);


        // 优先判断设备是否支持ble， 再次判断ble是否开关， 如果没有打开就请求开启
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Not Support BLE", Toast.LENGTH_LONG).show();
            return;
        } else {
            if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_START_BLE);
            }
        }

        mBtSppManager = BtManager.getInstance();
        mBleManager = BleManager.getInstance();
    }


    @Override
    protected void onStart() {
        super.onStart();

        //注册ble 状态的广播，如果蓝牙 开／关 都会收到广播
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBleReceiver, intentFilter);
    }


    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBleReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_start:
                resetItemState();

                String imsi = mEditTextImsi.getText().toString();
                if (imsi.length() != 15) {
                    return;
                }
                mHandler.sendEmptyMessage(MSG_START_SCAN);
                mBleDeviceInfo = new BleDeviceInfo(imsi);
                mBleManager.scanAndConnectDevice(mBleDeviceInfo, false, mBleConnectCallBack);
                break;
            case R.id.button_send_file:
                break;
        }
    }

    private void resetItemState() {
        mTransItemScanDevice.resetItemState();
        mTransItemConnectDevice.resetItemState();
        mTransItemChangeSpp.resetItemState();
        mTransItemPullFile.resetItemState();
        mTransItemChangeGatt.resetItemState();
    }

    private BleConnectCallback mBleConnectCallBack = new BleConnectCallback() {
        @Override
        public void onConnectSuccess(BluetoothDevice bluetoothDevice) {
            LogUtils.LOGE(TAG, "onConnectSuccess");

            //注意： 这里是向设备写一条命令，这里根据实际的情况操作
            mHandler.sendEmptyMessage(MSG_CONNECT_DEVICE_SUCCESS);
            mBleManager.writeToDevice(BleCommand.getVerifyCommand(COMMAND_TO_SPP));
            mBleManager.disconnect();
            mHandler.sendEmptyMessage(MSG_CHANGE_TO_SPP);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBtSppManager.connect(DEVICE_MAC, mBtConnectCallBack);
                }
            }, 3000);

        }

        @Override
        public void onDeviceFound(BluetoothDevice bluetoothDevice) {
            LogUtils.LOGE(TAG, "onDeviceFound");
            mHandler.sendEmptyMessage(MSG_FOUND_DEVICE_SUCCESS);
        }

        @Override
        public void onError(int errorCode, String reason) {
            LogUtils.LOGE(TAG, "onError");
            mHandler.sendEmptyMessage(MSG_FOUND_DEVICE_ERROR);
        }
    };

    private BtConnectCallBack mBtConnectCallBack = new BtConnectCallBack() {
        @Override
        public void deviceConnected() {
            mBtSppManager.pullFile("c:\\autostart.txt", mBtPullCallBack);
            mHandler.sendEmptyMessage(MSG_START_PULL_FILE);
        }

        @Override
        public void deviceDisconnected(String reason) {
        }
    };

    private BtPullFileCallBack mBtPullCallBack = new BtPullFileCallBack() {
        @Override
        public void readData(String data) {
            mHandler.sendEmptyMessage(MSG_PULL_FILE_SUCCESS);
            mBtSppManager.writeData(new String(COMMAND_TO_GATT).getBytes(), new BtTransferDataCallBack() {
                @Override
                public void sendData(byte[] data) {
                    mHandler.sendEmptyMessage(MSG_CHANGE_TO_GATT_SUCCESS);
                }

                @Override
                public void readData(byte[] data) {

                }

                @Override
                public void transDataError(String reason) {
                    mHandler.sendEmptyMessage(MSG_CHANGE_TO_GATT_FAIL);
                }
            });
        }

        @Override
        public void readFail(String reason) {
            mHandler.sendEmptyMessage(MSG_PULL_FILE_ERROR);
        }
    };


    /**
     * 开启ble回执
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_START_BLE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "同意开启 BLE", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "拒绝开启 BLE", Toast.LENGTH_LONG).show();
            }
        }

    }
}
