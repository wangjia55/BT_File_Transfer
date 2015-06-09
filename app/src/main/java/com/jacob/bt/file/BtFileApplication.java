package com.jacob.bt.file;

import android.widget.Toast;

import com.jacob.bt.spp.core.BtManager;
import com.jacob.bt.spp.exception.BtInitException;

/**
 * Package : jacob.bt_file_transfer
 * Author : jacob
 * Date : 15-6-8
 * Description : 这个类是用来xxx
 */
public class BtFileApplication extends com.activeandroid.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            BtManager.getInstance().init();
        } catch (BtInitException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}

