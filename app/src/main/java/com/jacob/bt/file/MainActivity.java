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
import android.widget.TextView;
import android.widget.Toast;

import com.jacob.ble.connector.core.BleConnectCallback;
import com.jacob.ble.connector.logic.BleCommand;
import com.jacob.ble.connector.logic.BleManager;
import com.jacob.ble.connector.utils.LogUtils;
import com.jacob.bt.file.logic.BleDevice;
import com.jacob.bt.file.logic.DataBaseHelper;
import com.jacob.bt.file.logic.FileLogic;
import com.jacob.bt.file.logic.NewPreferenceManager;
import com.jacob.bt.file.logic.TransFileItem;
import com.jacob.bt.spp.core.BtManager;
import com.jacob.bt.spp.impl.BtConnectCallBack;
import com.jacob.bt.spp.impl.BtPullFileCallBack;

import java.io.File;


public class MainActivity extends FragmentActivity implements View.OnClickListener {

    public static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_CHOOSE_DEVICE = 100;
    private static final int REQUEST_CODE_FILE_ADDRESS = 110;

    private TransFileItemView mTransItemConnectDevice;
    private TransFileItemView mTransItemPullFile;

    private TextView mTextViewImei;
    private TextView mTextViewFileName;
    private Button mButtonStart;

    private String mFileName;
    private BtManager mBtSppManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BleDevice mBleDevice = new BleDevice();
    private static final int REQUEST_START_BLE = 10;

    public static final int MSG_START_SCAN = 0x100;
    public static final int MSG_CONNECT_DEVICE_SUCCESS = 0x103;
    public static final int MSG_START_PULL_FILE = 0x105;
    public static final int MSG_PULL_FILE_SUCCESS = 0x106;
    public static final int MSG_PULL_FILE_ERROR = 0x107;

    private static final int MSG_RESET_BLE = 0x210;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_START_SCAN:
                    mTransItemConnectDevice.showProgressState();
                    break;
                case MSG_CONNECT_DEVICE_SUCCESS:
                    mTransItemConnectDevice.showOKState();
                    break;
                case MSG_PULL_FILE_SUCCESS:
                    mTransItemPullFile.showOKState();
                    mButtonStart.setEnabled(true);
                    break;
                case MSG_PULL_FILE_ERROR:
                    mButtonStart.setEnabled(true);
                    mTransItemPullFile.showErrorState();
                    break;
                case MSG_START_PULL_FILE:
                    mTransItemPullFile.showProgressState();
                    break;
                case MSG_RESET_BLE:
                    if (mBluetoothAdapter != null) {
                        mBluetoothAdapter.enable();
                    }
                    mButtonStart.setEnabled(true);
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
                    if (mBtSppManager != null) {
                        mBtSppManager.dispose();
                    }
                    break;
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTransItemConnectDevice = (TransFileItemView) findViewById(R.id.transItemOne);
        mTransItemPullFile = (TransFileItemView) findViewById(R.id.transItemTwo);
        mButtonStart = (Button) findViewById(R.id.button_start);
        mButtonStart.setOnClickListener(this);
        findViewById(R.id.button_send_file).setOnClickListener(this);
        findViewById(R.id.button_reset).setOnClickListener(this);
        findViewById(R.id.button_spp).setOnClickListener(this);

        mTextViewImei = (TextView) findViewById(R.id.text_view_imei);
        mTextViewFileName = (TextView) findViewById(R.id.text_view_file_name);
        mTextViewFileName.setText(NewPreferenceManager.getInstance(getApplicationContext()).getFileAddress());
        String imei = NewPreferenceManager.getInstance(getApplicationContext()).getImei();
        if (!"".equals(imei)) {
            mTextViewImei.setText(imei);
            mBleDevice = DataBaseHelper.getInstance().getDeviceByImei(imei);
        }

        mTextViewImei.setOnClickListener(this);
        mTextViewFileName.setOnClickListener(this);

        mTransItemConnectDevice.setTransFileItem(TransFileItem.item_one);
        mTransItemPullFile.setTransFileItem(TransFileItem.item_two);

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
        mFileName = mTextViewFileName.getText().toString();
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
                mButtonStart.setEnabled(false);
                String imsi = mBleDevice.getImsi();
                if (imsi.length() != 15) {
                    return;
                }
                mHandler.sendEmptyMessage(MSG_START_SCAN);
                mBtSppManager.connect(mBleDevice.getEdrMac(), mBtConnectCallBack);

                break;
            case R.id.button_send_file:
                String fileName = getFileName(mFileName);
                File file = new File("/sdcard/btspp/" + fileName);
                Intent intent = FileLogic.sendAnalysisReport(getApplicationContext(), file);
                startActivity(intent);
                break;
            case R.id.button_reset:
                resetItemState();
                if (mBluetoothAdapter != null) {
                    mBluetoothAdapter.disable();
                    mHandler.sendEmptyMessageDelayed(MSG_RESET_BLE, 1000);
                }

                break;

            case R.id.text_view_imei:
                Intent intentImei = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(intentImei, REQUEST_CODE_CHOOSE_DEVICE);
                break;
            case R.id.text_view_file_name:
                Intent intentFile = new Intent(MainActivity.this, SetFileAddressActivity.class);
                startActivityForResult(intentFile, REQUEST_CODE_FILE_ADDRESS);
                break;
            case R.id.button_spp:
                BleDeviceConnectInfo connectInfo = new BleDeviceConnectInfo(mBleDevice.getImsi());
                BleManager.getInstance().scanAndConnectDevice(connectInfo, false, new BleConnectCallback() {

                    @Override
                    public void onConnectSuccess(BluetoothDevice bluetoothDevice) {
                        BleManager.getInstance().writeToDevice(BleCommand.getVerifyCommand(mBleDevice.getImei() + "02"));
                        Toast.makeText(getApplicationContext(), "已经发送切换SPP指令，请过4秒钟后进行文件读取", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onDeviceFound(BluetoothDevice bluetoothDevice) {

                    }

                    @Override
                    public void onError(int errorCode, String reason) {
                        Toast.makeText(getApplicationContext(), "切换通道失败，请确认是否已经在SPP通道或蓝牙已经打开", Toast.LENGTH_LONG).show();
                    }
                });

                break;
        }
    }

    private void resetItemState() {
        mTransItemConnectDevice.resetItemState();
        mTransItemPullFile.resetItemState();
    }


    private BtConnectCallBack mBtConnectCallBack = new BtConnectCallBack() {
        @Override
        public void deviceConnected() {
            mBtSppManager.pullFile(mFileName, mBtPullCallBack);
            mHandler.sendEmptyMessage(MSG_CONNECT_DEVICE_SUCCESS);
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

        if (requestCode == REQUEST_CODE_CHOOSE_DEVICE && data != null) {
            mBleDevice = (BleDevice) data.getSerializableExtra("device");
            updateViewByDevice();
        }

        if (requestCode == REQUEST_CODE_FILE_ADDRESS && data != null) {
            String fileAdd = data.getStringExtra("address");
            mTextViewFileName.setText(fileAdd);
        }
    }

    private void updateViewByDevice() {
        if (mBleDevice != null) {
            mTextViewImei.setText(mBleDevice.getImei());
        }
    }

    private String getFileName(String name) {
        String fileName = name;
        fileName = fileName.replace(":\\", "_");
        fileName = fileName.replace('\\', '_');
        return fileName;
    }
}
