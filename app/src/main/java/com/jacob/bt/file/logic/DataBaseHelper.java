package com.jacob.bt.file.logic;

import com.activeandroid.query.Select;

import java.util.List;

/**
 * 数据库操作辅助类
 */
public class DataBaseHelper {
    private volatile static DataBaseHelper dataBaseHelper = null;

    private DataBaseHelper() {
    }

    /**
     * 单例模式
     */
    public static DataBaseHelper getInstance() {
        if (dataBaseHelper == null) {
            dataBaseHelper = new DataBaseHelper();
        }
        return dataBaseHelper;
    }


    public List<BleDevice> getAllDevice() {
        return new Select().from(BleDevice.class).execute();
    }
}
