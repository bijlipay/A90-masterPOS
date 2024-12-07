package com.vanstone.redsysa90prokeypos.tools;

import static android.content.Context.USB_SERVICE;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import com.vanstone.redsysa90prokeypos.app.App;
import com.vanstone.redsysa90prokeypos.params.Constants;
import com.vanstone.trans.api.MathsApi;
import com.vanstone.trans.api.PedApi;
import com.vanstone.utils.ByteUtils;
import com.vanstone.utils.CommonConvert;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

public class Tools {
    public static DisplayHint displayHint = new DisplayHint();
    private static String strSN;
    private static String ACTION_USB_PERMISSION = "cn.wch.wchusbdriver.USB_PERMISSION";

    public static int initPort(Context context){

        App.driver = new CH34xUARTDriver((UsbManager)context.getSystemService(USB_SERVICE) , context, ACTION_USB_PERMISSION);

        if (!App.driver.UsbFeatureSupported()) {
            Utils.showToast(App.context, "USB host is not supported be the terminal");
            App.driver.CloseDevice();
            return Constants.ERR_PORT_OPEN_FAIL;
        }

        int retVal = App.driver.ResumeUsbList();
        switch (retVal) {
            case -1: { // ResumeUsbList方法用于枚举CH34X设备以及打开相关设备
                Utils.showToast(App.context, "Open port failed!");
                App.driver.CloseDevice();
                return Constants.ERR_PORT_OPEN_FAIL;
            }
            case 0: {
                if (!App.driver.UartInit()) { // 对串口设备进行初始化操作
                    Utils.showToast(context, "Device init failed!");
                    return Constants.ERR_INIT_DEVICE;
                }

//                Utils.showToast(App.context, "Init port success");
                return 0;
            }
            default: {
                Utils.showToast(context, "No permission");
                return retVal;
            }
        }
    }


    public static int InitComPort() {
        boolean isSetSuccess = App.driver.SetConfig(115200, int2Byte(8), int2Byte(1), int2Byte(0), int2Byte(0));
        if (isSetSuccess) {
            return 0;
        } else {
            return -1;
        }
    }

    private static byte int2Byte(int src) {
        byte temp = (byte) Integer.parseInt(src + "");
        return temp;
    }

    public static String genHexStr(boolean numberFlag, int length){
        String retStr = "";
        String strTable = numberFlag ? "0123456789" : "0123456789ABCDEF";
        int len = strTable.length();
        boolean bDone = true;
        do{
            retStr = "";
            int count = 0;
            for (int i = 0; i < length; i++){
                double dblR = Math.random() * len;
                int intR = (int)Math.floor(dblR);
                char c = strTable.charAt(intR);
                if ((c >= '0') && (c <= '9')){
                    count++;
                }
                retStr += strTable.charAt(intR);
            }
            if (count >= 2){
                bDone = false;
            }
        }while (bDone);

        return retStr;
    }

    public static void openCommDialog(String message, Context context) {
        displayHint.openCommDialog(message, context);
    }

    public static void closeCommDialog() {
        displayHint.closeCommDialog();
    }

    public static void displayHint(String message, Context context) {
        displayHint.displayHint(message, context);
    }
}
