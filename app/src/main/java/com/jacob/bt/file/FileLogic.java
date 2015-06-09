package com.jacob.bt.file;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Package : com.jacob.bt.file
 * Author : jacob
 * Date : 15-6-9
 * Description : 这个类是用来xxx
 */
public class FileLogic {

    public static Intent sendAnalysisReport(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra("subject", file.getName()); //
        intent.putExtra("body", "Tracker传输文件"); //正文
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file)); //添加附件，附件为file对象
        intent.setType("*/*");//此处可发送多种文件
        return intent;
    }
}
