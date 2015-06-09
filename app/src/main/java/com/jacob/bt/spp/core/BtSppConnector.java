package com.jacob.bt.spp.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jacob.bt.spp.impl.BtConnectCallBack;
import com.jacob.bt.spp.impl.BtTransferDataCallBack;
import com.jacob.bt.spp.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Package : com.jacob.bt.spp.core
 * Author : jacob
 * Date : 15-6-5
 * Description : 这个类是关于蓝牙SPP操作的核心类
 */
class BtSppConnector {
    public static final String TAG = "BtSppConnector";

    private final String ANDROID_BT_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private ConnectDeviceThread mConnectThread;
    private TransferDataThread mTransDataThread;
    private BtConnectCallBack mBtConnectCallBack;
    private BtTransferDataCallBack mBtTransDataCallBack;
    private ConnectState mConnectState = ConnectState.STATE_DISCONNECTED;

    public static final int MSG_CONNECT_SUCCESS = 0x100;
    public static final int MSG_CONNECT_FAIL = 0x101;
    public static final int MSG_READ_DATA = 0x102;

    private static final String PARAMS_STRING = "params_string";
    private static final String PARAMS_BYTE = "params_byte";

    public BtSppConnector() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CONNECT_SUCCESS:
                    if (mBtConnectCallBack != null) {
                        mBtConnectCallBack.deviceConnected();
                    }
                    break;
                case MSG_CONNECT_FAIL:
                    if (mBtConnectCallBack != null) {
                        String reason = msg.getData().getString(PARAMS_STRING);
                        mBtConnectCallBack.deviceDisconnected(reason);
                    }
                    break;
                case MSG_READ_DATA:
                    if (mBtTransDataCallBack != null) {
                        byte[] buffer = msg.getData().getByteArray(PARAMS_BYTE);
                        mBtTransDataCallBack.readData(buffer);
                    }
                    break;

            }

        }
    };

    public void connectDevice(BluetoothDevice bluetoothDevice, BtConnectCallBack btConnectCallBack) {
        if (mConnectState != ConnectState.STATE_DISCONNECTED) {
            LogUtils.LOGE(TAG, "bluetooth is not disconnect ,so you can not connect ,please disconnect device first!");
        }

        mBtConnectCallBack = btConnectCallBack;
        mBluetoothDevice = bluetoothDevice;
        mConnectState = ConnectState.STATE_CONNECTING;

        disconnect();

        mBluetoothAdapter.cancelDiscovery();
        mConnectThread = new ConnectDeviceThread(bluetoothDevice);
        mConnectThread.start();
    }


    /**
     * 断开蓝牙连接
     */
    public void disconnect() {
        mConnectState = ConnectState.STATE_DISCONNECTED;
        cancel();
        if (mBtConnectCallBack != null) {
            mBtConnectCallBack.deviceDisconnected("");
        }
        mTransDataThread = null;
        mConnectThread = null;
    }

    /**
     * 向蓝牙设备写命令
     */
    public void writeData(byte[] data, BtTransferDataCallBack btTransferDataCallBack) {
        mBtTransDataCallBack = btTransferDataCallBack;
        if (mBtTransDataCallBack != null) {
            mBtTransDataCallBack.sendData(data);
        }
        if (mTransDataThread == null) {
            mTransDataThread = new TransferDataThread();
            mTransDataThread.start();
        }
        mTransDataThread.write(data);
    }

    /**
     * 向蓝牙设备写命令
     */
    public void writeData(byte[] data) {
        if (mBtTransDataCallBack != null) {
            mBtTransDataCallBack.sendData(data);
        }

        if (mTransDataThread == null) {
            mTransDataThread = new TransferDataThread();
            mTransDataThread.start();
        }
        mTransDataThread.write(data);
    }

    /**
     * 获取当前设备连接的状态
     */
    public ConnectState getCurrentState() {
        if (mBluetoothSocket != null) {
            return mBluetoothSocket.isConnected() ? ConnectState.STATE_CONNECTED : ConnectState.STATE_DISCONNECTED;
        }
        return mConnectState;
    }

    public void dispose() {
        mConnectState = ConnectState.STATE_DISCONNECTED;
        mBluetoothAdapter.cancelDiscovery();
        disconnect();
    }


    /**
     * 这个线程是用于连接设备而开启的子线程
     */
    private class ConnectDeviceThread extends Thread {

        public ConnectDeviceThread(BluetoothDevice device) {
            try {
                if (mBluetoothSocket != null) {
                    mBluetoothSocket.close();
                }
                mBluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(ANDROID_BT_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                mBluetoothSocket.connect();
                mConnectState = ConnectState.STATE_CONNECTED;
                sendConnectSuccessMessage();
            } catch (IOException e) {
                cancel();
                mConnectState = ConnectState.STATE_DISCONNECTED;
                sendConnectFailMessage(e.getMessage());
            }

        }

    }


    /**
     * 数据传递子线程，当蓝牙连接成功后，通过这个子线程进行命令的发送和数据的交换
     */
    private class TransferDataThread extends Thread {
        public TransferDataThread() {
            if (mBluetoothSocket != null && mBluetoothSocket.isConnected()) {
                try {
                    mInputStream = mBluetoothSocket.getInputStream();
                    mOutputStream = mBluetoothSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void run() {
            if (mInputStream == null) {
                callbackTransDataError("inputStream is null ,can not read data");
                return;
            }
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = mInputStream.read(buffer);
                    if (bytes > 0) {
                        final byte[] temp_buffer = new byte[bytes];
                        System.arraycopy(buffer, 0, temp_buffer, 0, bytes);
                        sendReadDataMessage(temp_buffer);
                    }

                } catch (IOException e) {
                    callbackTransDataError(e.getMessage());
                    return;
                }
            }
        }

        public void write(byte[] bytes) {
            if (mOutputStream != null) {
                try {
                    mOutputStream.write(bytes);
                    mOutputStream.flush();
                } catch (IOException e) {
                    callbackTransDataError("write data error:" + e.getMessage());
                }
            }
        }
    }


    private void cancel() {
        try {
            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
                mBluetoothSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据传递异常信息的回调
     */
    private void callbackTransDataError(String reason) {
        if (mBtTransDataCallBack != null) {
            mBtTransDataCallBack.transDataError(reason);
        }
    }

    /**
     * 发送从蓝牙设备读取到的数据
     */
    private void sendReadDataMessage(byte[] buffer) {
        Message msg = Message.obtain();
        msg.what = MSG_READ_DATA;
        Bundle bundle = new Bundle();
        bundle.putByteArray(PARAMS_BYTE, buffer);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }


    /**
     * 发送连接设备成功的消息
     */
    private void sendConnectSuccessMessage() {
        Message msg = Message.obtain();
        msg.what = MSG_CONNECT_SUCCESS;
        mHandler.sendMessage(msg);
    }

    /**
     * 发送连接设备成功的消息
     */
    private void sendConnectFailMessage(String message) {
        Message msg = Message.obtain();
        msg.what = MSG_CONNECT_FAIL;
        Bundle bundle = new Bundle();
        bundle.putString(PARAMS_STRING, message);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
}
