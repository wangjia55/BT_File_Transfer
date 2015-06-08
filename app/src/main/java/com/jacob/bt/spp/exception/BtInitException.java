package com.jacob.bt.spp.exception;

/**
 * Package : com.jacob.bt.spp.exception
 * Author : jacob
 * Date : 15-6-6
 * Description : 这个类是蓝牙初始化异常的描述类
 */
public class BtInitException extends Exception {
    public BtInitException() {
    }

    public BtInitException(String detailMessage) {
        super(detailMessage);
    }

}
