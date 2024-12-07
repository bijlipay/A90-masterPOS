package com.vanstone.redsysa90prokeypos.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.vanstone.redsysa90prokeypos.R
import com.vanstone.redsysa90prokeypos.app.App
import com.vanstone.redsysa90prokeypos.bean.CTKInfo
import com.vanstone.redsysa90prokeypos.bean.CmiCmaCmtkInfo
import com.vanstone.redsysa90prokeypos.bean.KeyVerInfo
import com.vanstone.redsysa90prokeypos.bean.KeyInfo
import com.vanstone.redsysa90prokeypos.params.Constants
import com.vanstone.redsysa90prokeypos.tools.DataBaseOpenHelper
import com.vanstone.redsysa90prokeypos.tools.Tools
import com.vanstone.redsysa90prokeypos.tools.Utils
import com.vanstone.trans.api.MathsApi
import com.vanstone.trans.api.PedApi
import com.vanstone.trans.api.SystemApi
import com.vanstone.utils.ByteUtils
import com.vanstone.utils.CommonConvert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.experimental.xor

class OperatorActivity : AppCompatActivity(), View.OnClickListener, CoroutineScope by MainScope() {

//    private var btInitPort: Button? = null
    private var spinnerTransferType: Spinner? = null
    private var btDerive: Button? = null
    private var pbSending: ProgressBar? = null
    private var masterPOS: Job? = null
    private var handler: CusHandler? = null
    private var spinnerAdapter: ArrayAdapter<String>? = null
    private var transferType = Constants.TRANSFER_TYPE_TDES


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initSpinner()
    }


    private fun initSpinner() {
        val spinnerZCMKTypeList = ArrayList<String?>()
        spinnerZCMKTypeList.add(Constants.TRANSFER_TYPE_TDES)
        spinnerZCMKTypeList.add(Constants.TRANSFER_TYPE_TR31)

        spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerZCMKTypeList)
        spinnerAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_item)
        spinnerTransferType!!.adapter = spinnerAdapter
        spinnerTransferType!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                if (position == 0) {
                    transferType = Constants.TRANSFER_TYPE_TDES
                    btDerive!!.text = resources.getString(R.string.btn_transfer_tdes)
                } else if (position == 1) {
                    transferType = Constants.TRANSFER_TYPE_TR31
                    btDerive!!.text = resources.getString(R.string.btn_transfer_tr31)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                transferType = Constants.TRANSFER_TYPE_TDES
                btDerive!!.text = resources.getString(R.string.btn_transfer_tdes)
            }
        }
    }

    private fun initView() {
        setContentView(R.layout.activity_operator)
//        btInitPort = findViewById(R.id.btInitPort)
        pbSending = findViewById(R.id.pbSending)
        btDerive = findViewById(R.id.btDerive)
        spinnerTransferType = findViewById(R.id.spinnerTransferType)
//        btInitPort!!.setOnClickListener(this)
        btDerive!!.setOnClickListener(this)

        handler = CusHandler()
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            /*R.id.btInitPort -> {
                var ret = Tools.initPort(App.context)
                if (ret != 0) {
                    return
                }

                ret = Tools.InitComPort()
                if (ret == 0) {
                    btDerive!!.isEnabled = true
                }
            }*/

            R.id.btDerive -> {
                Log.d("btDerive", "ButtonClicked")
                var ret = Tools.initPort(App.context)
                Log.d("btDerive", "initPort ret => " + ret)

                if (ret != 0) {
                    Log.d("btDerive", "initPort ret !=0 => " + "Fail to initialize USB")
                    Utils.showToast(applicationContext, "Fail to initialize USB")
                    return
                }

                ret = Tools.InitComPort()
                Log.d("btDerive", "InitComPort ret => " + ret)

                if (ret != 0) {
                    Log.d("btDerive", "InitComPort ret !=0 => " + "Fail to initialize USB")
                    Utils.showToast(applicationContext, "Fail to initialize USB")
                    return
                }
                //Utils.showToast(applicationContext, "Open port success")
                Log.d("btDerive",  "Open port success")

                pbSending!!.visibility = View.VISIBLE
                masterPOS = launch(Dispatchers.IO) {
                    val serialno = "Serial Number"
                    val dataToBeSent1 = ByteArray(2048)
                    var offset1 = 0
                    //Utils.showToast(applicationContext, serialno)
                    Log.d("btDerive",  serialno)
                    Log.d("btDerive",  transferType)
                    if (transferType == Constants.TRANSFER_TYPE_TDES) {
                        /**
                         *  ping request
                         *  start(1)
                         *  +Length(2)   //length of the packet
                         *  +Serial Number  //serial no.
                         *  +Checksum(1)
                         *  +end(1)
                         */

                        // 0x02
                        dataToBeSent1[offset1] = 0x02
                        offset1 += 1

                        // 2 bytes length
                        offset1 += 2

                        val serial = CommonConvert.StringToBytes(serialno)
                        serial.copyInto(dataToBeSent1,offset1,0, serial.size)
                        offset1 += serial.size


                        dataToBeSent1[1] = ((offset1 - 2) / 256).toByte()
                        dataToBeSent1[2] = ((offset1 - 2) % 256).toByte()

                        // Verification value
                        //Utils.printLogs("Data to be verified = ${CommonConvert.bcdToASCString(dataToBeSent1.copyOfRange(1, offset1))}")
                        dataToBeSent1[offset1] = MathsApi.XorCalc_Api(dataToBeSent1.copyOfRange(1, offset1), offset1 - 1).toByte()
                        offset1 += 1

                        // 0x03
                        dataToBeSent1[offset1] = 0x03
                        offset1 += 1
                    } else if (transferType == Constants.TRANSFER_TYPE_TR31){
                        /**
                         *  ping request
                         *  start(1)
                         *  +Length(2)   //length of the packet
                         *  +Serial Number  //serial no.
                         *  +Checksum(1)
                         *  +end(1)
                         */

                        // 0x02
                        dataToBeSent1[offset1] = 0x02
                        offset1 += 1

                        // 2 bytes length
                        offset1 += 2

                        val serial = CommonConvert.ascStringToBCD(serialno)
                        serial.copyInto(dataToBeSent1,offset1,0, serial.size)
                        offset1 += serial.size


                        dataToBeSent1[1] = ((offset1 - 2) / 256).toByte()
                        dataToBeSent1[2] = ((offset1 - 2) % 256).toByte()

                        // Verification value
                        //Utils.printLogs("Data to be verified = ${CommonConvert.bcdToASCString(dataToBeSent1.copyOfRange(1, offset1))}")
                        dataToBeSent1[offset1] = MathsApi.XorCalc_Api(dataToBeSent1.copyOfRange(1, offset1), offset1 - 1).toByte()
                        offset1 += 1

                        // 0x03
                        dataToBeSent1[offset1] = 0x03
                        offset1 += 1
                    }
                    // start to send data
                    Log.d("btDerive",  "data sent"+ CommonConvert.bytesToString(dataToBeSent1,0,offset1))
                    var totalSendLen1 = 0
                    var wIndex1 = 0
                    var sendBlockLen1 = 0
                    while (totalSendLen1 < offset1){
                        sendBlockLen1 = if ((offset1 - totalSendLen1) >= 32){
                            32
                        }else{
                            offset1 % 32
                        }
                        ret = App.driver.WriteData(dataToBeSent1.copyOfRange(wIndex1, wIndex1 + sendBlockLen1), sendBlockLen1)
                        //Utils.showToast(applicationContext, "ping successfully")
                        Log.d("btDerive",  "ping requested")
                        if (ret < sendBlockLen1) {
                            handler!!.sendEmptyMessage(Constants.ERR_SEND_DATA)
                            return@launch
                        }

                        //Utils.printLogs("Data has been sent : len = ${sendBlockLen1},  data = ${CommonConvert.bcdToASCString(dataToBeSent1.copyOfRange(wIndex1, wIndex1 + sendBlockLen1))}")
                        wIndex1 += sendBlockLen1
                        totalSendLen1 += sendBlockLen1

                        Thread.sleep(150)

                    }

                    //receiving the serial no
                    var isLenGet1 = false
                    var dataLen1 = 0
                    var totalLen1 = 0
                    var loc1 = 0
                    val allDataReceived1 = ByteArray(128)
                    val tempBuf1 = ByteArray(16)
                    val bLen1 = ByteArray(2)
                    var rlen1 = 0
                    // ----------------------------------------------------------------------------
                    val timerId1 = SystemApi.TimerSet_Api()
                    while (true) {
                        if (SystemApi.TimerCheck_Api(timerId1, 120 * 1000) != 0) {
                            handler!!.sendEmptyMessage(Constants.ERR_TIMEOUT)
                            return@launch
                        }

                        ByteUtils.memset(tempBuf1, 0, tempBuf1.size)
                        rlen1 = App.driver.ReadData(tempBuf1, tempBuf1.size)
                        Log.d("btDerive", "read data return $rlen1")

                        if (rlen1 > 0) {
                            Utils.printLogs(CommonConvert.bytesToHexString(ByteUtils.subBytes(tempBuf1, 0, rlen1)))
                            Log.d("btDerive", "read data $tempBuf1")
                            Log.d("btDerive",  "ping successfully")
                        }

                        if (rlen1 <= 0) {
                            continue
                        }

                        if (!isLenGet1) {
                            if (tempBuf1[0].toInt() != 0x02) {
                                handler!!.sendEmptyMessage(Constants.ERR_DATA_ERROR)
                                return@launch
                            }
                            if (rlen1 < 3) {
                                continue
                            }
                            isLenGet1 = true
                            ByteUtils.memcpy(bLen1, 0, tempBuf1, 1, 2)
                            val strLen = CommonConvert.bytes2HexString(bLen1)
                            dataLen1 = strLen.toInt(16)
                            totalLen1 = 1 + 2 + dataLen1 + 1
                            Utils.printLogs("dataLen = $dataLen1,   totalLen = $totalLen1")
                        }

                        ByteUtils.memcpy(allDataReceived1, loc1, tempBuf1, 0, rlen1)
                        loc1 += rlen1

                        Utils.printLogs("currentLoc = $loc1")

                        if (loc1 < totalLen1) {
                            continue
                        }
                        break
                    }
                    val slaveserial = allDataReceived1.copyOfRange(3,14)
                    val slserial = "0"+CommonConvert.bcdToASCString(slaveserial)
                    Utils.printLogs("d1 = ${slserial}")

                    //query the serial no by fetch the database
                    val keyList = mutableListOf<KeyInfo>()
                    val dataToBeSent2 = ByteArray(2048)
                    var decrypKeyout = ByteArray(16)
                    var encryptedin = ByteArray(16)
                    var offset2 = 0
                    var j = 0
                    var ret3 = queryKey(keyList)
                    if (ret3 != 0) {
                        handler!!.sendEmptyMessage(ret3)
                        return@launch
                    }
                    while (keyList[j].serialNo.equals(slserial,true))
                        j++
                    var ret2 = 0
                    encryptedin = CommonConvert.ascStringToBCD(keyList[j].key)
                    ret2 = PedApi.PEDDes_Api(Constants.ZCMK_INDEX, 0x83, 0x01, encryptedin, 16, decrypKeyout)
                    if (transferType == Constants.TRANSFER_TYPE_TDES) {
                        /**
                         *  key request
                         *  start(1)
                         *  +Length(2)      //length of the packet
                         *  +Serial Number  //serial no.
                         *  +index          //index to be stored
                         *  +key            //16 byte key
                         *  +kcv            //3 byte KCV
                         *  +Checksum(1)
                         *  +end(1)
                         */

                        // 0x02
                        dataToBeSent2[offset2] = 0x02
                        offset2 += 1

                        // 2 bytes length
                        offset2 += 2

                        val serial = CommonConvert.ascStringToBCD(slserial)
                        serial.copyInto(dataToBeSent2,offset2,0, serial.size)
                        offset2 += serial.size

                        dataToBeSent2[offset2] = 0x01
                        offset2 += 1

                        val keyval = decrypKeyout
                        keyval.copyInto(dataToBeSent2,offset2,0, keyval.size)
                        offset2 += keyval.size


                        val kcvval = CommonConvert.ascStringToBCD(keyList[j].kcv)
                        kcvval.copyInto(dataToBeSent2,offset2,0, kcvval.size)
                        offset2 += kcvval.size

                        dataToBeSent2[1] = ((offset2 - 2) / 256).toByte()
                        dataToBeSent2[2] = ((offset2 - 2) % 256).toByte()

                        // Verification value
                        //Utils.printLogs("Data to be verified = ${CommonConvert.bcdToASCString(dataToBeSent1.copyOfRange(1, offset1))}")
                        dataToBeSent2[offset2] = MathsApi.XorCalc_Api(dataToBeSent1.copyOfRange(1, offset2), offset1 - 1).toByte()
                        offset2 += 1

                        // 0x03
                        dataToBeSent2[offset2] = 0x03
                        offset2 += 1
                    } else if (transferType == Constants.TRANSFER_TYPE_TR31){
                        /**
                         *  key request
                         *  start(1)
                         *  +Length(2)      //length of the packet
                         *  +Serial Number  //serial no.
                         *  +index          //index to be stored
                         *  +key            //16 byte key
                         *  +kcv            //3 byte KCV
                         *  +Checksum(1)
                         *  +end(1)
                         */

                        // 0x02
                        dataToBeSent2[offset2] = 0x02
                        offset2 += 1

                        // 2 bytes length
                        offset2 += 2

                        val serial = CommonConvert.ascStringToBCD(slserial)
                        serial.copyInto(dataToBeSent2,offset2,0, serial.size)
                        offset2 += serial.size

                        dataToBeSent2[offset2] = 0x01
                        offset2 += 1

                        val keyval = decrypKeyout
                        keyval.copyInto(dataToBeSent2,offset2,0, keyval.size)
                        offset2 += keyval.size


                        val kcvval = CommonConvert.ascStringToBCD(keyList[j].kcv)
                        kcvval.copyInto(dataToBeSent2,offset2,0, kcvval.size)
                        offset2 += kcvval.size


                        dataToBeSent2[1] = ((offset2 - 2) / 256).toByte()
                        dataToBeSent2[2] = ((offset2 - 2) % 256).toByte()

                        // Verification value
                        //Utils.printLogs("Data to be verified = ${CommonConvert.bcdToASCString(dataToBeSent1.copyOfRange(1, offset1))}")
                        dataToBeSent2[offset2] = MathsApi.XorCalc_Api(dataToBeSent2.copyOfRange(1, offset2), offset1 - 1).toByte()
                        offset2 += 1

                        // 0x03
                        dataToBeSent2[offset2] = 0x03
                        offset2 += 1
                    }
                    // start to send data
                    var totalSendLen2 = 0
                    var wIndex2 = 0
                    var sendBlockLen2 = 0
                    while (totalSendLen2 < offset2){
                        sendBlockLen2 = if ((offset2 - totalSendLen2) >= 32){
                            32
                        }else{
                            offset2 % 32
                        }
                        ret = App.driver.WriteData(dataToBeSent1.copyOfRange(wIndex2, wIndex2 + sendBlockLen2), sendBlockLen1)
                        if (ret < sendBlockLen2) {
                            handler!!.sendEmptyMessage(Constants.ERR_SEND_DATA)
                            return@launch
                        }

                        //Utils.printLogs("Data has been sent : len = ${sendBlockLen1},  data = ${CommonConvert.bcdToASCString(dataToBeSent1.copyOfRange(wIndex1, wIndex1 + sendBlockLen1))}")
                        wIndex2 += sendBlockLen2
                        totalSendLen2 += sendBlockLen2

                        Thread.sleep(150)

                    }

                    //receiving the key status
                    var isLenGet2 = false
                    var dataLen2 = 0
                    var totalLen2 = 0
                    var loc2 = 0
                    val allDataReceived2 = ByteArray(128)
                    val tempBuf2 = ByteArray(16)
                    val bLen2 = ByteArray(2)
                    var rlen2 = 0
                    // ----------------------------------------------------------------------------
                    val timerId2 = SystemApi.TimerSet_Api()
                    while (true) {
                        if (SystemApi.TimerCheck_Api(timerId2, 120 * 1000) != 0) {
                            handler!!.sendEmptyMessage(Constants.ERR_TIMEOUT)
                            return@launch
                        }

                        ByteUtils.memset(tempBuf2, 0, tempBuf2.size)
                        rlen2 = App.driver.ReadData(tempBuf2, tempBuf2.size)

                        if (rlen2 > 0) {
                            Utils.printLogs(CommonConvert.bytesToHexString(ByteUtils.subBytes(tempBuf2, 0, rlen2)))
                        }

                        if (rlen2 <= 0) {
                            continue
                        }

                        if (!isLenGet2) {
                            if (tempBuf2[0].toInt() != 0x02) {
                                handler!!.sendEmptyMessage(Constants.ERR_DATA_ERROR)
                                return@launch
                            }
                            if (rlen2 < 3) {
                                continue
                            }
                            isLenGet2 = true
                            ByteUtils.memcpy(bLen2, 0, tempBuf2, 1, 2)
                            val strLen = CommonConvert.bytes2HexString(bLen2)
                            dataLen2 = strLen.toInt(16)
                            totalLen2 = 1 + 2 + dataLen2 + 1
                            Utils.printLogs("dataLen = $dataLen2,   totalLen = $totalLen2")
                        }

                        ByteUtils.memcpy(allDataReceived2, loc2, tempBuf2, 0, rlen2)
                        loc2 += rlen2

                        Utils.printLogs("currentLoc = $loc2")

                        if (loc2 < totalLen2) {
                            continue
                        }
                        break
                    }

                    var zcmk = 0
                    if (transferType ==Constants.TRANSFER_TYPE_TDES) {
                        zcmk = Constants.ZCMKType.KEY_TDES.ordinal
                    } else {
                        zcmk = Constants.ZCMKType.KEY_AES256.ordinal
                    }
                    var formatterdate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    var date = LocalDateTime.now().format(formatterdate)
                    var formattertime = DateTimeFormatter.ofPattern("hh:mm:ss")
                    var time = LocalDateTime.now().format(formattertime)
                    var contentValues = ContentValues()
                    contentValues.put(DataBaseOpenHelper.KEY_KEY_TYPE, zcmk)
                    contentValues.put(DataBaseOpenHelper.KEY_KCV, keyList[j].kcv)
                    Log.d(Constants.LOG_TAG, "" + keyList[j].kcv)
                    contentValues.put(DataBaseOpenHelper.DEVICE_SERIAL,keyList[j].serialNo)
                    Log.d(Constants.LOG_TAG, "" + keyList[j].serialNo)
                    contentValues.put(DataBaseOpenHelper.DATE, date)
                    Log.d(Constants.LOG_TAG, "" + date)
                    contentValues.put(DataBaseOpenHelper.TIME, time)
                    Log.d(Constants.LOG_TAG, "" + time)
                    contentValues.put(DataBaseOpenHelper.KEY_TMK_INDEX, 1)
                    DataBaseOpenHelper.insert(DataBaseOpenHelper.TABLE_KEYEXPORT_INFO,contentValues)



                    handler!!.sendEmptyMessage(Constants.STATUS_SUCCESS)


//                    var ciVersion: KeyVerInfo? = null
//                    var ctkVersion: KeyVerInfo? = null
//
//                    val cmiList = mutableListOf<CmiCmaCmtkInfo>()
//                    val cmaList = mutableListOf<CmiCmaCmtkInfo>()
//                    val cmtkList = mutableListOf<CmiCmaCmtkInfo>()
//                    val keyVerList = mutableListOf<KeyVerInfo>()
//
//                    var ret = queryKeyInfo(cmiList, cmaList, cmtkList, keyVerList)
//                    if (ret != 0) {
//                        handler!!.sendEmptyMessage(ret)
//                        return@launch
//                    }
//
//                    for (i in keyVerList.indices) {
//                        if (keyVerList[i].keyType == Constants.KeyBlockType.CMI.ordinal) {
//                            ciVersion = keyVerList[i]
//                        } else {
//                            ctkVersion = keyVerList[i]
//                        }
//                    }
//
//                    var isLenGet = false
//                    var dataLen = 0
//                    var totalLen = 0
//                    var loc = 0
//                    val allDataReceived = ByteArray(128)
//                    val tempBuf = ByteArray(16)
//                    val bLen = ByteArray(2)
//                    var rlen = 0
//
//                    // ----------------------------------------------------------------------------
//                    val timerId = SystemApi.TimerSet_Api()
//                    while (true) {
//                        if (SystemApi.TimerCheck_Api(timerId, 120 * 1000) != 0) {
//                            handler!!.sendEmptyMessage(Constants.ERR_TIMEOUT)
//                            return@launch
//                        }
//
//                        ByteUtils.memset(tempBuf, 0, tempBuf.size)
//                        rlen = App.driver.ReadData(tempBuf, tempBuf.size)
//
//                        if (rlen > 0) {
//                            Utils.printLogs(CommonConvert.bytesToHexString(ByteUtils.subBytes(tempBuf, 0, rlen)))
//                        }
//
//                        if (rlen <= 0) {
//                            continue
//                        }
//
//                        if (!isLenGet) {
//                            if (tempBuf[0].toInt() != 0x02) {
//                                handler!!.sendEmptyMessage(Constants.ERR_DATA_ERROR)
//                                return@launch
//                            }
//                            if (rlen < 3) {
//                                continue
//                            }
//                            isLenGet = true
//                            ByteUtils.memcpy(bLen, 0, tempBuf, 1, 2)
//                            val strLen = CommonConvert.bytes2HexString(bLen)
//                            dataLen = strLen.toInt(16)
//                            totalLen = 1 + 2 + dataLen + 1
//                            Utils.printLogs("dataLen = $dataLen,   totalLen = $totalLen")
//                        }
//
//                        ByteUtils.memcpy(allDataReceived, loc, tempBuf, 0, rlen)
//                        loc += rlen
//
//                        Utils.printLogs("currentLoc = $loc")
//
//                        if (loc < totalLen) {
//                            continue
//                        }
//                        break
//                    }
//
//                    val d1 = allDataReceived.copyOfRange(3, 11)
//                    Utils.printLogs("d1 = ${CommonConvert.bcdToASCString(d1)}")
//
//                    val d2 = ByteArray(d1.size)
//                    for (i in d1.indices) {
//                        d2[i] = d1[i] xor 0xFF.toByte()
//                    }
//
//                    val ci = ByteArray(16)
//                    ret = generateCI(ci, d1, d2)
//                    if (ret != 0) {
//                        handler!!.sendEmptyMessage(Constants.ERR_GEN_CI)
//                        return@launch
//                    }
//
//                    val ca = ByteArray(16)
//                    ret = generateCA(ca, d1, d2)
//                    if (ret != 0) {
//                        handler!!.sendEmptyMessage(Constants.ERR_GEN_CA)
//                        return@launch
//                    }
//
//                    val ctkList = ArrayList<CTKInfo>()
//                    ret = generateCMTK(cmtkList, ctkList, d1, d2)
//                    if (ret != 0) {
//                        handler!!.sendEmptyMessage(Constants.ERR_GEN_CTK)
//                        return@launch
//                    }
//
//                    val dataToBeSent = ByteArray(2048)
//                    var offset = 0
//
//                    if (transferType == Constants.TRANSFER_TYPE_TDES) {
//                        /**
//                         *  要发送出去的秘钥的数据格式
//                         *  0x02
//                         *  +Length(2)
//                         *  +TransferType(1)
//                         *  +CI_Idx(1)+CIKey(16)
//                         *  +CIVer_Idx(1)+CIVer(16)
//                         *  +CA_Idx(1)+CAKey(16)
//                         *  +CTK_Idx[0](1)+CTKKey[0](16)
//                         *  ......
//                         *  +CTK_Idx[9](1)+CTKKey[9](16)
//                         *  +CTKVer_Idx(1)+CTKVer(16)
//                         *  +XOR(1)
//                         *  +0x03
//                         */
//
//                        // 0x02
//                        dataToBeSent[offset] = 0x02
//                        offset += 1
//
//                        // 2 bytes length
//                        offset += 2
//
//                        dataToBeSent[offset] = 0.toByte()
//                        offset += 1
//
//                        // CI
//                        dataToBeSent[offset] = Constants.CI_INDEX.toByte()
//                        offset += 1
//                        ci.copyInto(dataToBeSent, offset, 0, ci.size)
//                        offset += ci.size
//
//                        // CI Version
//                        if (ciVersion != null) {
//                            dataToBeSent[offset] = Constants.CI_VER_INDEX.toByte()
//                            offset += 1
//                            val ciVer = CommonConvert.ascStringToBCD(ciVersion.keyVersion)
//                            ciVer.copyInto(dataToBeSent, offset, 0, ciVer.size)
//                            offset += ciVer.size
//                        } else {
//                            handler!!.sendEmptyMessage(Constants.ERR_CI_VER)
//                            return@launch
//                        }
//
//                        // CA
//                        dataToBeSent[offset] = Constants.CA_INDEX.toByte()
//                        offset += 1
//                        ca.copyInto(dataToBeSent, offset, 0, ca.size)
//                        offset += ca.size
//
//                        // CTK
//                        for (i in ctkList.indices) {
//                            dataToBeSent[offset] = ctkList[i].ctkIndex.toByte()
//                            offset += 1
//                            val ctk = CommonConvert.ascStringToBCD(ctkList[i].ctkValue)
//                            ctk.copyInto(dataToBeSent, offset, 0, ctk.size)
//                            offset += ctk.size
//                        }
//
//                        // CTK Version
//                        if (ctkVersion != null) {
//                            dataToBeSent[offset] = Constants.CTK_VER_INDEX.toByte()
//                            offset += 1
//                            val ctkVer = CommonConvert.ascStringToBCD(ctkVersion.keyVersion)
//                            ctkVer.copyInto(dataToBeSent, offset, 0, ctkVer.size)
//                            offset += ctkVer.size
//                        } else {
//                            handler!!.sendEmptyMessage(Constants.ERR_CTK_VER)
//                            return@launch
//                        }
//
//                        dataToBeSent[1] = ((offset - 2) / 256).toByte()
//                        dataToBeSent[2] = ((offset - 2) % 256).toByte()
//
//                        // Verification value
//                        Utils.printLogs("Data to be verified = ${CommonConvert.bcdToASCString(dataToBeSent.copyOfRange(1, offset))}")
//                        dataToBeSent[offset] = MathsApi.XorCalc_Api(dataToBeSent.copyOfRange(1, offset), offset - 1).toByte()
//                        offset += 1
//
//                        // 0x03
//                        dataToBeSent[offset] = 0x03
//                        offset += 1
//                    }else if(transferType == Constants.TRANSFER_TYPE_TR31){
//                        /**
//                         *  要发送出去的秘钥的数据格式
//                         *  0x02
//                         *  +Length(2)
//                         *  +TransferType(1)
//                         *  +RandomKey_Idx(1)+RandomKey(16)
//                         *  +CI_Idx(1)+CIKeyTR31()
//                         *  +CIVer_Idx(1)+CIVerTR31()
//                         *  +CA_Idx(1)+CAKeyTR31()
//                         *  +CTK_Idx[0](1)+CTKKeyTR31[0]()
//                         *  ......
//                         *  +CTK_Idx[9](1)+CTKKeyTR31[9]()
//                         *  +CTKVer_Idx(1)+CTKVerTR31()
//                         *  +XOR(1)
//                         *  +0x03
//                         */
//
//                        val tr31Out = ByteArray(80)
//
//                        // generate a random TDES key;
//                        val randomKeyStr = Tools.genHexStr(false, 32)
//                        Utils.printLogs("Random key generated")
//                        val randomKeyBCD = CommonConvert.ascStringToBCD(randomKeyStr)
//                        val writeKey = PedApi.PEDWriteMKey_Api(Constants.TEMP_KEY_INDEX, 0x03, randomKeyBCD)
//                        if (writeKey != 0) {
//                            handler!!.sendEmptyMessage(Constants.ERR_WRITE_RANDOM_KEY)
//                            return@launch
//                        }
//
//                        // 0x02
//                        dataToBeSent[offset] = 0x02
//                        offset += 1
//
//                        // 2 bytes length
//                        offset += 2
//
//                        dataToBeSent[offset] = 1.toByte()
//                        offset += 1
//
//                        // Random Key
//                        dataToBeSent[offset] = Constants.TEMP_KEY_INDEX.toByte()
//                        offset += 1
//                        randomKeyBCD.copyInto(dataToBeSent, offset, 0, randomKeyBCD.size)
//                        offset += randomKeyBCD.size
//
//                        var bundle = Bundle()
//                        bundle.putString("Algorithm", "T")
//                        bundle.putString("Key Usage", "K1")
//                        bundle.putString("Mode of Use", "X")
//                        bundle.putString("Key Version Number", "00")
//                        bundle.putString("Number of Optional", "00")
//                        bundle.putString("exportability", "N")
//
//                        // CI
//                        ByteUtils.memset(tr31Out, 0, tr31Out.size)
//                        dataToBeSent[offset] = Constants.CI_INDEX.toByte()
//                        offset += 1
//                        var len = PedApi.TR31Encrypt(2, Constants.TEMP_KEY_INDEX, ci, tr31Out, randomKeyBCD.size, bundle)
//                        if(len < 0){
//                            handler!!.sendEmptyMessage(Constants.ERR_GEN_TR31)
//                            return@launch
//                        }
//                        tr31Out.copyInto(dataToBeSent, offset, 0, len)
//                        offset += len
//
//                        // CI Version
//                        if (ciVersion != null) {
//                            dataToBeSent[offset] = Constants.CI_VER_INDEX.toByte()
//                            offset += 1
//                            val ciVer = CommonConvert.ascStringToBCD(ciVersion.keyVersion)
//                            ciVer.copyInto(dataToBeSent, offset, 0, ciVer.size)
//                            offset += ciVer.size
//                        } else {
//                            handler!!.sendEmptyMessage(Constants.ERR_CI_VER)
//                            return@launch
//                        }
//
//                        // CA
//                        bundle = Bundle()
//                        bundle.putString("Algorithm", "T")
//                        bundle.putString("Mode of Use", "X")
//                        bundle.putString("Key Version Number", "00")
//                        bundle.putString("Number of Optional", "00")
//                        bundle.putString("exportability", "N")
//                        bundle.putString("Key Usage", "M3")
//                        ByteUtils.memset(tr31Out, 0, tr31Out.size)
//                        dataToBeSent[offset] = Constants.CA_INDEX.toByte()
//                        offset += 1
//                        len = PedApi.TR31Encrypt(2, Constants.TEMP_KEY_INDEX, ca, tr31Out, randomKeyBCD.size, bundle)
//                        if(len < 0){
//                            handler!!.sendEmptyMessage(Constants.ERR_GEN_TR31)
//                            return@launch
//                        }
//                        tr31Out.copyInto(dataToBeSent, offset, 0, len)
//                        offset += len
//
//                        // CTK
//                        bundle = Bundle()
//                        bundle.putString("Algorithm", "T")
//                        bundle.putString("Mode of Use", "X")
//                        bundle.putString("Key Version Number", "00")
//                        bundle.putString("Number of Optional", "00")
//                        bundle.putString("exportability", "N")
//                        bundle.putString("Key Usage", "D0")
//                        for (i in ctkList.indices) {
//                            ByteUtils.memset(tr31Out, 0, tr31Out.size)
//                            dataToBeSent[offset] = ctkList[i].ctkIndex.toByte()
//                            offset += 1
//                            val ctk = CommonConvert.ascStringToBCD(ctkList[i].ctkValue)
//                            len = PedApi.TR31Encrypt(2, Constants.TEMP_KEY_INDEX, ctk, tr31Out, randomKeyBCD.size, bundle)
//                            if(len < 0){
//                                handler!!.sendEmptyMessage(Constants.ERR_GEN_TR31)
//                                return@launch
//                            }
//                            tr31Out.copyInto(dataToBeSent, offset, 0, len)
//                            offset += len
//                        }
//
//                        // CTK Version
//                        if (ctkVersion != null) {
//                            dataToBeSent[offset] = Constants.CTK_VER_INDEX.toByte()
//                            offset += 1
//                            val ctkVer = CommonConvert.ascStringToBCD(ctkVersion.keyVersion)
//                            ctkVer.copyInto(dataToBeSent, offset, 0, ctkVer.size)
//                            offset += ctkVer.size
//                        } else {
//                            handler!!.sendEmptyMessage(Constants.ERR_CTK_VER)
//                            return@launch
//                        }
//
//                        dataToBeSent[1] = ((offset - 2) / 256).toByte()
//                        dataToBeSent[2] = ((offset - 2) % 256).toByte()
//
//                        // Verification value
//                        Utils.printLogs("Data to be verified = ${CommonConvert.bcdToASCString(dataToBeSent.copyOfRange(1, offset))}")
//                        dataToBeSent[offset] = MathsApi.XorCalc_Api(dataToBeSent.copyOfRange(1, offset), offset - 1).toByte()
//                        offset += 1
//
//                        // 0x03
//                        dataToBeSent[offset] = 0x03
//                        offset += 1
//                    }
//
//
//                    Utils.printLogs("Data to be sent : len = ${offset},  data = ${CommonConvert.bcdToASCString(dataToBeSent.copyOfRange(0, offset))}")
//
//                    // start to send data
//                    var totalSendLen = 0
//                    var wIndex = 0
//                    var sendBlockLen = 0
//                    while (totalSendLen < offset){
//                        sendBlockLen = if ((offset - totalSendLen) >= 32){
//                            32
//                        }else{
//                            offset % 32
//                        }
//                        ret = App.driver.WriteData(dataToBeSent.copyOfRange(wIndex, wIndex + sendBlockLen), sendBlockLen)
//                        if (ret < sendBlockLen) {
//                            handler!!.sendEmptyMessage(Constants.ERR_SEND_DATA)
//                            return@launch
//                        }
//
//                        Utils.printLogs("Data has been sent : len = ${sendBlockLen},  data = ${CommonConvert.bcdToASCString(dataToBeSent.copyOfRange(wIndex, wIndex + sendBlockLen))}")
//                        wIndex += sendBlockLen
//                        totalSendLen += sendBlockLen
//
//                        Thread.sleep(150)
//
//                    }
////                    ret = App.driver.WriteData(dataToBeSent, offset)
////                    if (ret < offset) {
////                        handler!!.sendEmptyMessage(Constants.ERR_SEND_DATA)
////                        return@launch
////                    }
//
//                    handler!!.sendEmptyMessage(Constants.STATUS_SUCCESS)
                }
            }
        }
    }

    private fun generateCI(ci: ByteArray, d1: ByteArray, d2: ByteArray): Int {
        val temp = ByteArray(8)
        var ret = PedApi.PEDDes_Api(Constants.CMI_INDEX, 0x03, 0x01, d1, 8, temp)
        if (ret != 0) {
            return ret
        }

        Utils.printLogs("CI-temp1 = ${CommonConvert.bcdToASCString(temp)}")
        temp.copyInto(ci, 0, 0, temp.size)
        ByteUtils.memset(temp, 0, temp.size)

        ret = PedApi.PEDDes_Api(Constants.CMI_INDEX, 0x03, 0x01, d2, 8, temp)
        if (ret != 0) {
            return ret
        }

        Utils.printLogs("CI-temp2 = ${CommonConvert.bcdToASCString(temp)}")
        temp.copyInto(ci, 8, 0, temp.size)
        Utils.printLogs("CI = ${CommonConvert.bcdToASCString(ci)}")

        return 0
    }

    private fun generateCA(ca: ByteArray, d1: ByteArray, d2: ByteArray): Int {
        val temp = ByteArray(8)
        var ret = PedApi.PEDDes_Api(Constants.CMA_INDEX, 0x03, 0x01, d1, 8, temp)
        if (ret != 0) {
            return ret
        }

        Utils.printLogs("CA-temp1 = ${CommonConvert.bcdToASCString(temp)}")
        temp.copyInto(ca, 0, 0, temp.size)
        ByteUtils.memset(temp, 0, temp.size)

        ret = PedApi.PEDDes_Api(Constants.CMA_INDEX, 0x03, 0x01, d2, 8, temp)
        if (ret != 0) {
            return ret
        }

        Utils.printLogs("CA-temp2 = ${CommonConvert.bcdToASCString(temp)}")
        temp.copyInto(ca, 8, 0, temp.size)
        Utils.printLogs("CA = ${CommonConvert.bcdToASCString(ca)}")

        return 0
    }

    private fun generateCMTK(cmtkList: MutableList<CmiCmaCmtkInfo>, ctkList: ArrayList<CTKInfo>, d1: ByteArray, d2: ByteArray): Int {
        if (cmtkList.size == 10) {
            for (i in cmtkList.indices) {
                val cmtk = cmtkList[i]
                val temp = ByteArray(8)
                val ctk = ByteArray(16)
                var ret = PedApi.PEDDes_Api(cmtk.keyIndex, 0x03, 0x01, d1, 8, temp)
                if (ret != 0) {
                    return ret
                }

                Utils.printLogs("CTK-temp1 = ${CommonConvert.bcdToASCString(temp)}")
                temp.copyInto(ctk, 0, 0, temp.size)
                ByteUtils.memset(temp, 0, temp.size)

                ret = PedApi.PEDDes_Api(cmtk.keyIndex, 0x03, 0x01, d2, 8, temp)
                if (ret != 0) {
                    return ret
                }

                Utils.printLogs("CTK-temp2 = ${CommonConvert.bcdToASCString(temp)}")
                temp.copyInto(ctk, 8, 0, temp.size)
                Utils.printLogs("CTK = ${CommonConvert.bcdToASCString(ctk)}")

                val ctkInfo = CTKInfo(CommonConvert.bcdToASCString(ctk), cmtk.keyIndex + 6)
                ctkList.add(ctkInfo)
            }
        } else {
            return Constants.ERR_GEN_CTK
        }

        return 0
    }

    private fun queryKey(keyList: MutableList<KeyInfo>): Int {
        val keyInfo = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_KEYIMPORT_INFO, "")
        if (keyInfo.count != 1)
            return -1
        else {
            var i = 0
            while (keyInfo.moveToNext()) {
                val key = keyInfo.getString(1)
                val kcv = keyInfo.getString(2)
                val serial = keyInfo.getString(3)
                keyList.add(KeyInfo(key, kcv, serial))
            }
            if (i >= keyInfo.count)
                return  -1
        }
        return 0
    }

    private fun queryKeyInfo(cmiList: MutableList<CmiCmaCmtkInfo>, cmaList: MutableList<CmiCmaCmtkInfo>, cmtkList: MutableList<CmiCmaCmtkInfo>, keyVerList: MutableList<KeyVerInfo>): Int {
        val cmiInfo = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_CMI_KEY_INFO, "")
        val cmaInfo = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_CMA_KEY_INFO, "")
        val cmtkInfo = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_CMTK_KEY_INFO, "")
        val versionInfo = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_KEY_VER_INFO, "")

        if (cmiInfo.count != 1) {
            return -1
        } else {
            while (cmiInfo.moveToNext()) {
                val keyType = cmiInfo.getInt(1)
                val keyIndex = cmiInfo.getInt(2)
                val keyKCV = cmiInfo.getString(3)
                cmiList.add(CmiCmaCmtkInfo(keyType, keyKCV, keyIndex))
            }
        }

        if (cmaInfo.count != 1) {
            return -2
        } else {
            while (cmaInfo.moveToNext()) {
                val keyType = cmaInfo.getInt(1)
                val keyIndex = cmaInfo.getInt(2)
                val keyKCV = cmaInfo.getString(3)
                cmaList.add(CmiCmaCmtkInfo(keyType, keyKCV, keyIndex))
            }
        }

        if (cmtkInfo.count != 10) {
            return -3
        } else {
            while (cmtkInfo.moveToNext()) {
                val keyType = cmtkInfo.getInt(1)
                val keyIndex = cmtkInfo.getInt(2)
                val keyKCV = cmtkInfo.getString(3)
                cmtkList.add(CmiCmaCmtkInfo(keyType, keyKCV, keyIndex))
            }
        }

        if (versionInfo.count != 2) {
            return -4
        } else {
            while (versionInfo.moveToNext()) {
                val keyType = versionInfo.getInt(1)
                val verKCV = versionInfo.getString(2)
                val keyVer = versionInfo.getString(3)
                keyVerList.add(KeyVerInfo(keyType, verKCV, keyVer))
            }
        }

        return 0
    }

    @SuppressLint("HandlerLeak")
    inner class CusHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            masterPOS!!.cancel()
            pbSending!!.visibility = View.GONE
//            App.driver.CloseDevice()
            val what = msg.what
            when (what) {
                Constants.ERR_CMI_QUANTITY -> {
                    Utils.showToast(applicationContext, "Incorrect CMI quantity")
                }

                Constants.ERR_CMA_QUANTITY -> {
                    Utils.showToast(applicationContext, "Incorrect CMA quantity")
                }

                Constants.ERR_CMTK_QUANTITY -> {
                    Utils.showToast(applicationContext, "Incorrect CMTK quantity")
                }

                Constants.ERR_KEY_VERSION_QUANTITY -> {
                    Utils.showToast(applicationContext, "Incorrect key version quantity")
                }

                Constants.ERR_TIMEOUT -> {
                    Utils.showToast(applicationContext, "Timeout")
                }

                Constants.ERR_DATA_ERROR -> {
                    Utils.showToast(applicationContext, "Data error")
                }

                Constants.ERR_GEN_CI -> {
                    Utils.showToast(applicationContext, "Fail to generate CI key")
                }

                Constants.ERR_GEN_CA -> {
                    Utils.showToast(applicationContext, "Fail to generate CA key")
                }

                Constants.ERR_GEN_CTK -> {
                    Utils.showToast(applicationContext, "Fail to generate CTKs")
                }

                Constants.ERR_SEND_DATA ->{
                    Utils.showToast(applicationContext, "Fail to send keys")
                }

                Constants.ERR_CI_VER -> {
                    Utils.showToast(applicationContext, "CI version error")
                }

                Constants.ERR_CTK_VER -> {
                    Utils.showToast(applicationContext, "CTK version error")
                }

                Constants.ERR_WRITE_RANDOM_KEY -> {
                    Utils.showToast(applicationContext, "Fail to write random key")
                }

                Constants.ERR_GEN_TR31 -> {
                    Utils.showToast(applicationContext, "Fail to generate TR31 block")
                }

                Constants.STATUS_SUCCESS -> {
                    Utils.showToast(applicationContext, "Send success")
                }
            }
        }
    }
}