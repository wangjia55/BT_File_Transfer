package com.jacob.bt.file.logic;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * 这个类主要是将需要存放在软件中的所有设置信息集中起来统一的管理，便于以后的维护；
 */
public class NewPreferenceManager {
    private Context mContext;
    private static NewPreferenceManager mInstance;
    private static final String FILE_ADDRESS = "file_address";
    private static final String CURRENT_IMEI = "current_imei";

    private NewPreferenceManager(Context context) {
        mContext = context;
    }

    public static NewPreferenceManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NewPreferenceManager(context);
        }
        return mInstance;
    }


    public void saveFileAddress(String address) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(FILE_ADDRESS, address).commit();
    }

    public String getFileAddress() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString(FILE_ADDRESS, "c:\\r.status");
    }

    public void saveImei(String imei) {
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(CURRENT_IMEI, imei).commit();
    }

    public String getImei() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString(CURRENT_IMEI, "");
    }

}

