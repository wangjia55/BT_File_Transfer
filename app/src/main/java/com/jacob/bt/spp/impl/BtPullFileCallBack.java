package com.jacob.bt.spp.impl;

/**
 * Package : com.jacob.bt.spp.impl
 * Author : jacob
 * Date : 15-6-8
 * Description : 这个类是读取设备文件的接口
 */
public interface BtPullFileCallBack {

    void readData(String data);

    void readFail(String reason);
}
