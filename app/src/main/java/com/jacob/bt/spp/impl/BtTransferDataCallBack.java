package com.jacob.bt.spp.impl;

/**
 * Package : com.jacob.bt.spp.core
 * Author : jacob
 * Date : 15-6-5
 * Description : 这个类是和蓝牙设备交换数据的接口类
 */
public interface BtTransferDataCallBack {
    void sendData(byte[] data);

    void readData(byte[] data);

    void transDataError(String reason);
}
