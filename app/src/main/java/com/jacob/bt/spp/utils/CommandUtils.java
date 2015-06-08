package com.jacob.bt.spp.utils;

/**
 * Package : com.jacob.bt.spp.utils
 * Author : jacob
 * Date : 15-6-5
 * Description : 这个类是描述所有命令
 */
public class CommandUtils {

    //COMMAND {
    public static final String COMMAND_GET_IMEI = "Get IMEI";
    public static final String COMMAND_GET_IMEI_DATA = "gmi:";
    public static final String COMMAND_GET_IMSI = "Get IMSI";
    public static final String COMMAND_GET_IMSI_DATA = "gii:";
    public static final String COMMAND_GET_BATTERY = "Get Battery Status";
    public static final String COMMAND_GET_BATTERY_DATA = "gbi:";
    public static final String COMMAND_GET_GSM = "Get GSM Status";
    public static final String COMMAND_GET_GSM_DATA = "ggi:";
    public static final String COMMAND_GET_WIFI_MAC = "Get WIFI MAC";
    public static final String COMMAND_GET_WIFI_MAC_DATA = "gwm:";
    public static final String COMMAND_GET_BT_MAC = "Get BT MAC";
    public static final String COMMAND_GET_BT_MAC_DATA = "gbm:";
    public static final String COMMAND_REBOOT = "Reboot";
    public static final String COMMAND_REBOOT_DATA = "rb:";
    public static final String COMMAND_POWER_OFF = "Power Off";
    public static final String COMMAND_POWER_OFF_DATA = "pof:";

    public static final String COMMAND_OPEN_FILE_DATA = "fo:";
    public static final String COMMAND_SET_FILE_PATH_DATA = "fsp:";
    public static final String COMMAND_CLOSE_FILE_DATA = "fc:";
    public static final String COMMAND_FILE_READ_DATA = "fr:";
    public static final String COMMAND_FILE_WRITE_DATA = "fw:";

    public static final String COMMAND_PREFIX = "at+[1588]cft:";

    public static final int FILE_MODE_READ = 1;
    public static final int FILE_MODE_CREATE = 4;


    public static final String[] sCommandString = {
            COMMAND_GET_IMEI,
            COMMAND_GET_IMSI,
            COMMAND_GET_BATTERY,
            COMMAND_GET_GSM,
            COMMAND_GET_WIFI_MAC,
            COMMAND_GET_BT_MAC,
            COMMAND_REBOOT,
            COMMAND_POWER_OFF
    };

    public static String parseSendCommand(String command) {
        switch (command) {
            case CommandUtils.COMMAND_GET_IMEI:
                return CommandUtils.COMMAND_GET_IMEI_DATA;

            case CommandUtils.COMMAND_GET_IMSI:
                return CommandUtils.COMMAND_GET_IMSI_DATA;

            case CommandUtils.COMMAND_GET_BATTERY:
                return CommandUtils.COMMAND_GET_BATTERY_DATA;

            case CommandUtils.COMMAND_GET_GSM:
                return CommandUtils.COMMAND_GET_GSM_DATA;

            case CommandUtils.COMMAND_GET_WIFI_MAC:
                return CommandUtils.COMMAND_GET_WIFI_MAC_DATA;

            case CommandUtils.COMMAND_GET_BT_MAC:
                return CommandUtils.COMMAND_GET_BT_MAC_DATA;

            case CommandUtils.COMMAND_REBOOT:
                return CommandUtils.COMMAND_REBOOT_DATA;

            case CommandUtils.COMMAND_POWER_OFF:
                return CommandUtils.COMMAND_POWER_OFF_DATA;
            default:
                return "";
        }
    }


    public static String parseReadData(String command, byte[] data) {
        String data_str = new String(data);
        if (data_str.equals(":cmatruxze:0\t")) {
            return "\r\n";
        }
        byte[] data_int = new byte[8];
        switch (command) {
            case CommandUtils.COMMAND_GET_IMEI:
                byte[] imei = new byte[data.length - 2];
                System.arraycopy(data, 2, imei, 0, data.length - 2);
                return "IMEI: " + new String(imei) + "\r\n";

            case CommandUtils.COMMAND_GET_IMSI:
                byte[] imsi = new byte[data.length - 2];
                System.arraycopy(data, 2, imsi, 0, data.length - 2);
                return "IMSI: " + new String(imsi) + "\r\n";

            case CommandUtils.COMMAND_GET_BATTERY:
                System.arraycopy(data, 2, data_int, 0, 8);
                int battery_level = getInt32(data_int);

                System.arraycopy(data, 10, data_int, 0, 8);
                int battery_voltage = getInt32(data_int);
                return "Battery Level: " + String.valueOf(battery_level) + " __ Battery Voltage: " + String.valueOf(battery_voltage) + "mV\r\n";

            case CommandUtils.COMMAND_GET_GSM:
                System.arraycopy(data, 2, data_int, 0, 8);
                int gsm_dbm = getInt32(data_int);
                //gsm_dbm = ~gsm_dbm - 1;
                String text = "GSM rssi: " + String.valueOf(gsm_dbm)
                        + "dBm\r\nGSM Operator: ";

                String tmp = text;

                switch (data[11]) {
                    case '0':
                        text = tmp + "NO SIM";
                        break;
                    case '1':
                        text = tmp + "Unknown";
                        break;
                    case '2':
                        text = tmp + "CMCC";
                        break;
                    case '3':
                        text = tmp + "UNICOM";
                        break;
                    case '4':
                        text = tmp + "CNC";
                        break;
                    case '5':
                        text = tmp + "CNTELCOM";
                        break;
                    case '6':
                        text = tmp + "ALL";
                        break;
                }

                tmp = text + "\r\nSIM Status: ";

                switch (data[13]) {
                    case 'f':
                        text = tmp + "获取失败";
                        break;
                    case '0':
                        text = tmp + "无SIM卡或SIM卡损坏";
                        break;
                    case '1':
                        text = tmp + "SIM卡工作正常";
                        break;
                }
                return text + "\r\n";

            case CommandUtils.COMMAND_GET_WIFI_MAC:
                byte[] wifi_mac = new byte[data.length - 2];
                System.arraycopy(data, 2, wifi_mac, 0, 12);
                return "WF Mac: " + new String(wifi_mac) + "\r\n";

            case CommandUtils.COMMAND_GET_BT_MAC:
                byte[] btMac = new byte[data.length - 2];
                System.arraycopy(data, 2, btMac, 0, 12);
                return "BT Mac: " + new String(btMac) + "\r\n";
            default:
                return "";
        }

    }


    public static int getInt32(byte[] data) {
        int value = 0;
        for (int i = 0; i < 8; i++) {
            value <<= 4;
            if ((data[i] >= '0') && (data[i] <= '9')) {
                value += data[i] - '0';
            } else if ((data[i] >= 'a') && (data[i] <= 'f')) {
                value += data[i] - 'a' + 10;
            } else if ((data[i] >= 'A') && (data[i] <= 'F')) {
                value += data[i] - 'A' + 10;
            } else {
                return 0;
            }
        }
        return value;
    }

    public static int getInt8(byte[] data) {
        int value = 0;
        for (int i = 0; i < 2; i++) {
            value <<= 4;
            if ((data[i] >= '0') && (data[i] <= '9')) {
                value += data[i] - '0';
            } else if ((data[i] >= 'a') && (data[i] <= 'f')) {
                value += data[i] - 'a' + 10;
            } else if ((data[i] >= 'A') && (data[i] <= 'F')) {
                value += data[i] - 'A' + 10;
            } else {
                return -1;
            }
        }
        return value;
    }
}
