package com.jacob.bt.file;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Package : com.jacob.bt.file
 * Author : jacob
 * Date : 15-6-9
 * Description : 这个类是用来xxx
 */
@Table(name = "t_ble_device")
public class BleDevice extends Model{

    @Column(name = "imei")
    private String imei;

    @Column(name = "imsi")
    private String imsi;

    @Column(name = "edrMac")
    private String edrMac;

    @Column(name = "bleMac")
    private String bleMac;

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getEdrMac() {
        return edrMac;
    }

    public void setEdrMac(String edrMac) {
        this.edrMac = edrMac;
    }

    public String getBleMac() {
        return bleMac;
    }

    public void setBleMac(String bleMac) {
        this.bleMac = bleMac;
    }


}
