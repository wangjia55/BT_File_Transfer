package com.jacob.bt.spp.core;

import com.jacob.bt.spp.impl.BtPullFileCallBack;
import com.jacob.bt.spp.impl.BtTransferDataCallBack;
import com.jacob.bt.spp.utils.CommandUtils;
import com.jacob.bt.spp.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Package : com.jacob.bt.spp.utils
 * Author : jacob
 * Date : 15-6-8
 * Description : 这个类是用来进行蓝牙文件传输的类
 */
class BtFileManager {
    private String mFileName = "";
    private String mCommand;
    private int mFileSize;
    private boolean mResponse = false;

    public static final int READ_MAX_SIZE = 38;
    public static final int WRITE_MAX_SIZE = 54;

    private BtSppConnector mBtSppConnector;

    private BtPullFileCallBack mPullFileCallBack;

    public BtFileManager(BtSppConnector btSppConnector) {
        mBtSppConnector = btSppConnector;
    }

    /**
     * 在读取文件的过程中，需要串行给设备发送命令
     */
    public void pullFile(String fileAddress, BtPullFileCallBack pullFileCallBack) {
        mFileName = fileAddress;
        if (mFileName == null || "".equals(mFileName)) {
            return;
        }
        this.mPullFileCallBack = pullFileCallBack;

        new PullFileThread().start();
    }

    /**
     * 在读取文件的过程中，需要串行给设备发送命令
     */
    private class PullFileThread extends Thread {

        public PullFileThread() {

        }

        /* */
        @Override
        public void run() {
            pullFile();
        }

        /*读取文件*/
        public void pullFile() {
            /*step 1: 发送一个 close file 的命令*/
            mCommand = CommandUtils.COMMAND_CLOSE_FILE_DATA;
            String fileCommand = CommandUtils.COMMAND_PREFIX + CommandUtils.COMMAND_CLOSE_FILE_DATA;
            mBtSppConnector.writeData(fileCommand.getBytes(), btPullFileCallBack);

            /*step 1-1: 等待回复，成功才执行第二个命令 */
            if (!waitResponse()) {
                return;
            }

             /*step 2: 设置需要读取的文件路径 */
            mCommand = CommandUtils.COMMAND_SET_FILE_PATH_DATA;
            fileCommand = CommandUtils.COMMAND_PREFIX + CommandUtils.COMMAND_SET_FILE_PATH_DATA + "S";
            for (int i = 0; i < mFileName.length(); i++) {
                fileCommand = fileCommand + String.format("%04x", mFileName.toCharArray()[i] & 0xFFFF);
            }
            mBtSppConnector.writeData(fileCommand.getBytes());

            /*step 2-1: 等待回复，成功才执行第二个命令 */
            if (!waitResponse()) {
                return;
            }

            /*step 3：打开文件，并且获取文件的大小 */
            mCommand = CommandUtils.COMMAND_OPEN_FILE_DATA;
            fileCommand = CommandUtils.COMMAND_PREFIX + String.format(CommandUtils.COMMAND_OPEN_FILE_DATA + "%08x%08x", CommandUtils.FILE_MODE_READ, 1);
            mBtSppConnector.writeData(fileCommand.getBytes());

             /*step 3-1: 等待回复，成功才执行第二个命令 */
            if (!waitResponse()) {
                return;
            }

              /*step 4: 创建本地文件，并初始化文件流 */
            File file = createStoreFilePath();
            OutputStream fos;
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                return;
            }

            int totalRead = 0;
            int readLen = 0;
            String content = "";

              /*step 5: 通过循环读取文件内容，最后再组装成字符串*/
            while (totalRead < mFileSize) {
                byte[] buf = new byte[READ_MAX_SIZE];
                readLen = fileRead(buf, mFileSize - totalRead);
                if (readLen < 0) {
                    break;
                }
                try {
                    fos.write((new String(buf).getBytes()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                content = content + new String(buf);
                totalRead += readLen;
            }

            LogUtils.LOGE(" Read File end ---->:", content);

            if (mPullFileCallBack != null) {
                mPullFileCallBack.readData(content);
            }

             /*step 6:关闭文件流*/
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private int fileRead(byte[] buf, int len) {
            int readSize, readLen, totalRead = 0, value;
            String command;

            readSize = (len > READ_MAX_SIZE ? READ_MAX_SIZE : len);
            command = CommandUtils.COMMAND_PREFIX + CommandUtils.COMMAND_FILE_READ_DATA + String.format("%08x", readSize);
            while (readSize > 0) {
                mCommand = CommandUtils.COMMAND_FILE_READ_DATA;
                mBtSppConnector.writeData(command.getBytes());

                if (waitResponse() == false) {
                    return -1;
                }

                if (mReceiveBuffer == null) {
                    return -2;
                }
                readLen = CommandUtils.getInt8(mReceiveBuffer);
                if (readLen < 0) {
                    return -3;
                }
                if (readLen == 0) {
                    break;
                }

                for (int i = 0; i < readLen; i++) {
                    byte[] data = new byte[2];
                    System.arraycopy(mReceiveBuffer, 2 + 2 * i, data, 0, 2);
                    value = CommandUtils.getInt8(data);
                    buf[totalRead + i] = Integer.valueOf(value).byteValue();
                }
                totalRead += readLen;
                len -= readLen;
                readSize -= readLen;
            }
            return totalRead;
        }

        /**
         * 等待命令的回复,每隔500ms检测一次返回结果，如果1.5秒内没有回复，认为失败
         */
        private boolean waitResponse() {
            int count = 0;
            mResponse = false;
            while (count < 3) {
                try {
                    count++;
                    sleep(500);
                    if (mResponse == true) {
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mPullFileCallBack != null) {
                mPullFileCallBack.readFail("Device no Response when pull file!");
            }
            return false;
        }
    }


    private byte[] mReceiveBuffer;
    /**
     * 读取文件的CallBack
     */
    private BtTransferDataCallBack btPullFileCallBack = new BtTransferDataCallBack() {

        @Override
        public void sendData(byte[] data) {
            LogUtils.LOGE("file sendData-->:", new String(data));
        }

        @Override
        public void readData(byte[] data) {
            LogUtils.LOGE("file back-->:", new String(data));
            String readData = new String(data);
            if (readData.equals(":cmatruxze:0\t")) {
                mResponse = true;
            } else if (readData.startsWith(":cmatruxze:0")) {
                mResponse = true;
            } else if ("Ok".equals(readData)) {
                mResponse = true;
            } else {
                mResponse = true;
                switch (mCommand) {
                    case CommandUtils.COMMAND_OPEN_FILE_DATA:
                        byte[] fileSize = new byte[data.length - 2];
                        System.arraycopy(data, 2, fileSize, 0, data.length - 2);
                        String[] stringArray = new String(fileSize).split(",");

                        try {
                            mFileSize = Integer.parseInt(stringArray[0]);
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        }
                        break;

                    case CommandUtils.COMMAND_FILE_READ_DATA:
                        mResponse = true;
                        if (new String(data).equals("Fail:Read fail.")) {
                            mReceiveBuffer = null;
                        } else {
                            mReceiveBuffer = new byte[data.length - 2 - 1];
                            System.arraycopy(data, 2, mReceiveBuffer, 0, data.length - 2 - 1);
                        }
                        break;
                }
            }
        }

        @Override
        public void transDataError(String reason) {
            LogUtils.LOGE("file transDataError-->:", reason);
            mResponse = false;
        }
    };


    /**
     * 创建一个本地的文件，用于存储从设备文件读取的内容
     */
    private File createStoreFilePath() {
        File file = null;
        String file_name = mFileName;
        file_name = file_name.replace(":\\", "_");
        file_name = file_name.replace('\\', '_');
        File pathFile = new File("/sdcard/btspp/");
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        file = new File("/sdcard/btspp/" + file_name);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
