package com.jacob.bt.spp.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.jacob.ble.connector.utils.LogUtils;
import com.jacob.bt.spp.exception.BtInitException;
import com.jacob.bt.spp.impl.BtConnectCallBack;
import com.jacob.bt.spp.impl.BtPullFileCallBack;
import com.jacob.bt.spp.impl.BtTransferDataCallBack;
import com.jacob.bt.spp.impl.IBtService;

/**
 * Package : com.jacob.bt.spp.core
 * Author : jacob
 * Date : 15-6-5
 * Description : 这个类是用来管理所有的蓝牙相关的操作
 */
public class BtManager implements IBtService {

    private static BtManager sInstance;

    private BluetoothAdapter mBluetoothAdapter;

    private BtSppConnector mBtSppConnector = new BtSppConnector();

    private BtFileManager mBtFileManager = new BtFileManager(mBtSppConnector);


    private BtManager() {
    }


    public static BtManager getInstance() {
        if (sInstance == null) {
            sInstance = new BtManager();
        }
        return sInstance;
    }


    /**
     * 初始化，检查蓝牙是否可用
     */
    public void init() throws BtInitException {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            throw new BtInitException("设备不支持蓝牙 ！");
        }
    }

    /**
     * 通过蓝牙地址去连接一个设备
     */
    @Override
    public void connect(String address, BtConnectCallBack btConnectCallBack) throws IllegalArgumentException {
        if (address == null || address.length() == 0) {
            throw new IllegalArgumentException("MAC地址不能为空！");
        }
        if (mBluetoothAdapter != null) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            connect(device, btConnectCallBack);
        }
    }

    /**
     * 获取当前设备连接的状态
     */
    public ConnectState getCurrentState() {
        return mBtSppConnector.getCurrentState();
    }

    /**
     * 直接连接某个设备
     */
    @Override
    public void connect(BluetoothDevice device, BtConnectCallBack btConnectCallBack) {
        mBtSppConnector.connectDevice(device, btConnectCallBack);
    }

    /**
     * 断开连接
     */
    @Override
    public void disconnect() {
        mBtSppConnector.disconnect();
    }

    /**
     * 向设备写数据
     */
    @Override
    public void writeData(byte[] data, BtTransferDataCallBack btTransferDataCallBack) {
        LogUtils.LOGE("TAG", "send——>:" + new String(data));
        mBtSppConnector.writeData(data, btTransferDataCallBack);
    }

    /**
     * 关闭蓝牙
     */
    @Override
    public void closeBluetooth() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
    }

    @Override
    public void pullFile(String fileAddress,BtPullFileCallBack pullFileCallBack) {
        mBtFileManager.pullFile(fileAddress,pullFileCallBack);
    }


    public int getBluetoothState() {
        return mBluetoothAdapter.getState();
    }

    public void dispose() {
        mBtSppConnector.dispose();
    }
}
