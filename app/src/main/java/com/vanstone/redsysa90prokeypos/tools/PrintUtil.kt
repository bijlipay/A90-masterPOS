package com.vanstone.redsysa90prokeypos.tools

import android.os.Handler
import android.os.Message
import com.vanstone.redsysa90prokeypos.bean.CmiCmaCmtkInfo
import com.vanstone.redsysa90prokeypos.bean.FileKeyVerInfo
import com.vanstone.redsysa90prokeypos.bean.KeyInfo
import com.vanstone.redsysa90prokeypos.bean.KeyVerInfo
import com.vanstone.redsysa90prokeypos.bean.ZCMKInfo
import com.vanstone.redsysa90prokeypos.params.Constants
import com.vanstone.trans.api.PrinterApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PrintUtil {
    fun printKeyinj (keyinjinfo: KeyInfo, handler: Handler) {
        var formatterdate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var date = LocalDateTime.now().format(formatterdate)
        var formattertime = DateTimeFormatter.ofPattern("hh:mm:ss")
        var time = LocalDateTime.now().format(formattertime)
        PrinterApi.PrnClrBuff_Api()
        PrinterApi.PrnFontSet_Api(24, 24, 0)
        PrinterApi.printSetGray_Api(10)
        PrinterApi.PrnLineSpaceSet_Api(0, 0)
        PrinterApi.printSetAlign_Api(1)
        PrinterApi.PrnStr_Api("Key Download Info")
        PrinterApi.PrnStr_Api("-------------------------------------------------------------------------------")
        PrinterApi.printSetAlign_Api(0)
        PrinterApi.PrnStr_Api("Datetime:    ${date} ${time}")
        PrinterApi.PrnStr_Api("Index:        0")
        PrinterApi.PrnStr_Api("KCV:         ${keyinjinfo.kcv}")
        PrinterApi.PrnStr_Api("serial no:   ${keyinjinfo.serialNo}")
        PrinterApi.printSetAlign_Api(1)
        PrinterApi.PrnStr_Api("-------------------------------------------------------------------------------")
        PrinterApi.PrnStr_Api("\n\n\n\n\n")
        printData(handler)
    }

    fun printZCMK(zcmkInfo: ZCMKInfo, handler: Handler) {
        PrinterApi.PrnClrBuff_Api()
        PrinterApi.PrnFontSet_Api(24, 24, 0)
        PrinterApi.printSetGray_Api(10)
        PrinterApi.PrnLineSpaceSet_Api(0, 0)
        PrinterApi.printSetAlign_Api(1)
        PrinterApi.PrnStr_Api("ZCMK Key Info")
        PrinterApi.PrnStr_Api("-------------------------------------------------------------------------------")

        PrinterApi.printSetAlign_Api(0)

        if (zcmkInfo.keyType == Constants.ZCMKType.KEY_AES256.ordinal) {
            PrinterApi.PrnStr_Api("ZCMK Type:                AES256")
        } else if (zcmkInfo.keyType == Constants.ZCMKType.KEY_TDES.ordinal) {
            PrinterApi.PrnStr_Api("ZCMK Type:                  TDES")
        }
        PrinterApi.PrnStr_Api("KCV of ZCMK:              ${zcmkInfo.kcv}")
        PrinterApi.PrnStr_Api("ZCMK Index:                    ${zcmkInfo.keyIndex}")
        PrinterApi.PrnStr_Api("\n\n\n\n\n")

        printData(handler)
    }

    fun printCmiCmaCmtk(keyInfo: CmiCmaCmtkInfo, handler: Handler) {
        PrinterApi.PrnClrBuff_Api()
        PrinterApi.PrnFontSet_Api(24, 24, 0)
        PrinterApi.printSetGray_Api(10)
        PrinterApi.PrnLineSpaceSet_Api(0, 0)
        if (keyInfo.keyType == Constants.KeyBlockType.CMI.ordinal) {
            PrinterApi.printSetAlign_Api(1)
            PrinterApi.PrnStr_Api("CMI Key Info")
            PrinterApi.PrnStr_Api("-------------------------------------------------------------------------------")
            PrinterApi.printSetAlign_Api(0)
            PrinterApi.PrnStr_Api("KCV of CMI:               ${keyInfo.kcv}")
            PrinterApi.PrnStr_Api("CMI Index:                     ${keyInfo.keyIndex}")
            PrinterApi.PrnStr_Api("\n\n")
        } else if (keyInfo.keyType == Constants.KeyBlockType.CMA.ordinal) {
            PrinterApi.printSetAlign_Api(1)
            PrinterApi.PrnStr_Api("CMA Key Info")
            PrinterApi.PrnStr_Api("-------------------------------------------------------------------------------")
            PrinterApi.printSetAlign_Api(0)
            PrinterApi.PrnStr_Api("KCV of CMA:               ${keyInfo.kcv}")
            PrinterApi.PrnStr_Api("CMA Index:                     ${keyInfo.keyIndex}")
            PrinterApi.PrnStr_Api("\n\n")
        } else if (keyInfo.keyType == Constants.KeyBlockType.CMTK.ordinal) {
            PrinterApi.printSetAlign_Api(1)
            PrinterApi.PrnStr_Api("CMTK Key Info")
            PrinterApi.PrnStr_Api("-------------------------------------------------------------------------------")
            PrinterApi.printSetAlign_Api(0)
            PrinterApi.PrnStr_Api("KCV of CMTK:              ${keyInfo.kcv}")
            if (keyInfo.keyIndex.toString().length == 1) {
                PrinterApi.PrnStr_Api("CMTK Index:                    ${keyInfo.keyIndex}")
            }else{
                PrinterApi.PrnStr_Api("CMTK Index:                   ${keyInfo.keyIndex}")
            }
            PrinterApi.PrnStr_Api("\n\n")
        }

        if (PrinterApi.PrnCheckPrnData_Api() == 0) {
            printData(handler)
        }
    }

    fun printKeyVer(cmiVer: FileKeyVerInfo, cmtkVer: FileKeyVerInfo, handler: Handler) {
        PrinterApi.PrnClrBuff_Api()
        PrinterApi.PrnFontSet_Api(24, 24, 0)
        PrinterApi.printSetGray_Api(10)
        PrinterApi.PrnLineSpaceSet_Api(0, 0)
        PrinterApi.printSetAlign_Api(1)
        PrinterApi.PrnStr_Api("CMI/CMTK Version Info")
        PrinterApi.PrnStr_Api("-------------------------------------------------------------------------------")
        PrinterApi.printSetAlign_Api(0)
        PrinterApi.PrnStr_Api("KCV of CMI Version:          ${cmiVer.kcv}")
        PrinterApi.PrnStr_Api("KCV of CMTK Version:         ${cmtkVer.kcv}")
        PrinterApi.PrnStr_Api("\n\n")

        printData(handler)
    }

    fun printAllKey(zcmkList: MutableList<ZCMKInfo>, cmiList: MutableList<CmiCmaCmtkInfo>, cmaList: MutableList<CmiCmaCmtkInfo>, cmtkList: MutableList<CmiCmaCmtkInfo>, keyVerList: MutableList<KeyVerInfo>, handler: Handler) {
        PrinterApi.PrnClrBuff_Api()
        PrinterApi.PrnFontSet_Api(24, 24, 0)
        PrinterApi.printSetGray_Api(10)
        PrinterApi.PrnLineSpaceSet_Api(0, 0)

        if (zcmkList.size > 0) {
            PrinterApi.printSetAlign_Api(1)
            PrinterApi.PrnStr_Api("ZCMK Key Info")
            PrinterApi.PrnStr_Api("-------------------------------------------------------------------------------")

            PrinterApi.printSetAlign_Api(0)

            if (zcmkList[0].keyType == Constants.ZCMKType.KEY_AES256.ordinal) {
                PrinterApi.PrnStr_Api("ZCMK Type:                AES256")
            } else if (zcmkList[0].keyType == Constants.ZCMKType.KEY_TDES.ordinal) {
                PrinterApi.PrnStr_Api("ZCMK Type:                  TDES")
            }
            PrinterApi.PrnStr_Api("KCV of ZCMK:              ${zcmkList[0].kcv}")
            PrinterApi.PrnStr_Api("ZCMK Index:                    ${zcmkList[0].keyIndex}")
            PrinterApi.PrnStr_Api("\n\n")
        }


        if (cmiList.size > 0) {
            PrinterApi.printSetAlign_Api(1)
            PrinterApi.PrnStr_Api("CMI Key Info")
            PrinterApi.PrnStr_Api("-------------------------------------------------------------------------------")

            PrinterApi.printSetAlign_Api(0)

            PrinterApi.PrnStr_Api("KCV of CMI:               ${cmiList[0].kcv}")
            PrinterApi.PrnStr_Api("CMI Index:                     ${cmiList[0].keyIndex}")

            if (keyVerList.size > 0) {
                for (i in keyVerList.indices) {
                    if (keyVerList[i].keyType == Constants.KeyBlockType.CMI.ordinal) {
                        PrinterApi.PrnStr_Api("KCV of CMI Version:          ${keyVerList[i].keyVersionKCV}")
                    }
                }
            }

            PrinterApi.PrnStr_Api("\n\n")
        }


        if (cmaList.size > 0) {
            PrinterApi.printSetAlign_Api(1)
            PrinterApi.PrnStr_Api("CMA Key Info")
            PrinterApi.PrnStr_Api("-------------------------------------------------------------------------------")

            PrinterApi.printSetAlign_Api(0)

            PrinterApi.PrnStr_Api("KCV of CMA:               ${cmaList[0].kcv}")
            PrinterApi.PrnStr_Api("CMA Index:                     ${cmaList[0].keyIndex}")
            PrinterApi.PrnStr_Api("\n\n")
        }


        if (cmtkList.size > 0) {
            PrinterApi.printSetAlign_Api(1)
            PrinterApi.PrnStr_Api("CMTK Info")
            PrinterApi.PrnStr_Api("-------------------------------------------------------------------------------")

            PrinterApi.printSetAlign_Api(0)

            for (i in cmtkList.indices) {
                PrinterApi.PrnStr_Api("KCV of CMTK:              ${cmtkList[i].kcv}")
                if (cmtkList[i].keyIndex.toString().length == 1) {
                    PrinterApi.PrnStr_Api("CMTK Index:                    ${cmtkList[i].keyIndex}")
                }else{
                    PrinterApi.PrnStr_Api("CMTK Index:                   ${cmtkList[i].keyIndex}")
                }
            }

            if (keyVerList.size > 0) {
                for (i in keyVerList.indices) {
                    if (keyVerList[i].keyType == Constants.KeyBlockType.CMTK.ordinal) {
                        PrinterApi.PrnStr_Api("KCV of CMTK Version:         ${keyVerList[i].keyVersionKCV}")
                    }
                }
            }

            PrinterApi.PrnStr_Api("\n\n")
        }

        if (PrinterApi.PrnCheckPrnData_Api() == 0) {
            printData(handler)
        }
    }


    private fun printData(handler: Handler): Int {
        var msg: String = ""
        val ret = PrinterApi.PrnStart_Api()
        when (ret) {
            2 -> msg = "Paper is not enough"
            3 -> msg = "Printer is too hot"
        }

        val message = Message.obtain()
        message.what = Constants.STATUS_PRINT
        message.arg1 = ret
        message.obj = msg
        handler.sendMessage(message)
        return -1
    }
}