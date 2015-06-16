package com.jacob.bt.file;

import android.bluetooth.BluetoothDevice;

import com.jacob.ble.connector.core.BleConnectInfo;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by jianhaohong on 5/15/15.
 */
public class BleDeviceConnectInfo implements BleConnectInfo {

    private String imbt;

    public BleDeviceConnectInfo(String imbt) {
        this.imbt = imbt;
    }

    @Override
    public UUID getWriteCharacteristicUUID() {
        return UUID.fromString("00002A1A-0000-1000-8000-00805F9B34FB");
    }

    @Override
    public UUID getServiceUUID() {
        return UUID.fromString("0000110F-0000-1000-8000-00805F9B34FB");
    }

    @Override
    public UUID getReadCharacteristicUUID() {
        return UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");
    }

    @Override
    public UUID getCharacteristicDescriptorUUID() {
        return null;
    }

    @Override
    public UUID getNotificationService() {
        return null;
    }

    @Override
    public boolean shouldConnectDevice(BluetoothDevice bluetoothDevice, byte[] bytes) {

        int startIndex = 9;
        byte[] imbtBytes = Arrays.copyOfRange(bytes, startIndex, startIndex + imbt.length());
        String scanImbt = new String(imbtBytes);
        if (imbt.equals(scanImbt)) {
            return true;
        } else {
            return false;
        }
    }


}
