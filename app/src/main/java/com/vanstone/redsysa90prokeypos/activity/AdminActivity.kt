package com.vanstone.redsysa90prokeypos.activity

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vanstone.msgdialog.ConfirmDialog
import com.vanstone.msgdialog.MsgConfirmDialog
import com.vanstone.msgdialog.MsgDialog
import com.vanstone.redsysa90prokeypos.R
import com.vanstone.redsysa90prokeypos.bean.CmiCmaCmtkInfo
import com.vanstone.redsysa90prokeypos.bean.KeyInfo
import com.vanstone.redsysa90prokeypos.bean.KeyVerInfo
import com.vanstone.redsysa90prokeypos.bean.KeysetsData
import com.vanstone.redsysa90prokeypos.bean.ZCMKInfo
import com.vanstone.redsysa90prokeypos.params.Constants
import com.vanstone.redsysa90prokeypos.tools.DataBaseOpenHelper
import com.vanstone.redsysa90prokeypos.tools.PrintUtil
import com.vanstone.redsysa90prokeypos.tools.Utils
import com.vanstone.trans.api.PedApi
import com.vanstone.utils.CommonConvert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.jahnen.libaums.core.UsbMassStorageDevice
import me.jahnen.libaums.core.fs.FileSystem
import me.jahnen.libaums.core.fs.UsbFile
import me.jahnen.libaums.core.fs.UsbFileInputStream
import me.jahnen.libaums.core.fs.UsbFileOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class AdminActivity : AppCompatActivity(), View.OnClickListener, CoroutineScope by MainScope() {

    private var btLoadZCMK: Button? = null
    private var btLoadCMI: Button? = null
    private var btLoadCMA: Button? = null
    private var btLoadCMTK: Button? = null
    private var btDelCMI: Button? = null
    private var btDelCMA: Button? = null
    private var btDelCMTK: Button? = null
    private var btDelZCMK: Button? = null
    private var btDelAll: Button? = null
    private var btPrint: Button? = null
    private var btLoadKeyVer: Button? = null
    private var btDelVer: Button? = null
    private var spinnerTitle: TextView? = null

    private var handler: CusHandler? = null
    private var deleteKeys: Job? = null
    private var msgDialog: MsgDialog? = null
    private var spinnerZCMKType: Spinner? = null
    private val msgConfirmDialog = MsgConfirmDialog()
    private var spinnerZCMKTypeAdapter: ArrayAdapter<String>? = null
    private var zcmkType = Constants.ZCMKType.KEY_AES256
    private val zcmkList = mutableListOf<ZCMKInfo>()
    private val cmiList = mutableListOf<CmiCmaCmtkInfo>()
    private val cmaList = mutableListOf<CmiCmaCmtkInfo>()
    private val cmtkList = mutableListOf<CmiCmaCmtkInfo>()
    private val keyVerList = mutableListOf<KeyVerInfo>()
    private val keyList = mutableListOf<KeyInfo>()
    private val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    private var storageDevice: UsbMassStorageDevice? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        handler = CusHandler()
    }

    private fun initView() {
        setContentView(R.layout.activity_admin)

        spinnerZCMKType = findViewById(R.id.spinnerZCMKType)
        btLoadZCMK = findViewById(R.id.btLoadZCMK)
        btLoadCMI = findViewById(R.id.btLoadCMI)
        btLoadCMA = findViewById(R.id.btLoadCMA)
        btLoadCMTK = findViewById(R.id.btLoadCMTK)
        btDelCMI = findViewById(R.id.btDelCMI)
        btDelCMA = findViewById(R.id.btDelCMA)
        btDelCMTK = findViewById(R.id.btDelCMTK)
        btDelAll = findViewById(R.id.btDelAll)
        btPrint = findViewById(R.id.btPrint)
        btDelZCMK = findViewById(R.id.btDelZCMK)
        btLoadKeyVer = findViewById(R.id.btLoadKeyVer)
        btDelVer = findViewById(R.id.btDelVer)
        spinnerTitle = findViewById(R.id.spinnerTitle)

        btLoadZCMK!!.setOnClickListener(this)
        btLoadCMI!!.setOnClickListener(this)
        btLoadCMA!!.setOnClickListener(this)
        btLoadCMTK!!.setOnClickListener(this)
        btDelCMI!!.setOnClickListener(this)
        btDelCMA!!.setOnClickListener(this)
        btDelCMTK!!.setOnClickListener(this)
        btDelZCMK!!.setOnClickListener(this)
        btDelAll!!.setOnClickListener(this)
        btLoadKeyVer!!.setOnClickListener(this)
        btPrint!!.setOnClickListener(this)
        btDelVer!!.setOnClickListener(this)

        initZCMKSpinner()
    }

    private fun initZCMKSpinner() {
        val spinnerZCMKTypeList = ArrayList<String?>()
        spinnerZCMKTypeList.add(Constants.KEY_ZCMK_AES256)
        spinnerZCMKTypeList.add(Constants.KEY_ZCMK_TDES)

        spinnerZCMKTypeAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerZCMKTypeList)
        spinnerZCMKTypeAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_item)
        spinnerZCMKType!!.adapter = spinnerZCMKTypeAdapter
        spinnerZCMKType!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                if (position == 0) {
                    btLoadZCMK!!.text = resources.getString(R.string.btn_load_aes256_zcmk)
                    btLoadCMI!!.text = resources.getString(R.string.btn_load_aes256_cmi)
                    btLoadCMA!!.text = resources.getString(R.string.btn_load_aes256_cma)
                    btLoadCMTK!!.text = resources.getString(R.string.btn_load_aes256_cmtk)

                    zcmkType = Constants.ZCMKType.KEY_AES256
                } else if (position == 1) {
                    btLoadZCMK!!.text = resources.getString(R.string.btn_load_tdes_zcmk)
                    btLoadCMI!!.text = resources.getString(R.string.btn_load_tdes_cmi)
                    btLoadCMA!!.text = resources.getString(R.string.btn_load_tdes_cma)
                    btLoadCMTK!!.text = resources.getString(R.string.btn_load_tdes_cmtk)

                    zcmkType = Constants.ZCMKType.KEY_TDES
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                btLoadZCMK!!.text = resources.getString(R.string.btn_load_aes256_zcmk)
                btLoadCMI!!.text = resources.getString(R.string.btn_load_aes256_cmi)
                btLoadCMA!!.text = resources.getString(R.string.btn_load_aes256_cma)
                btLoadCMTK!!.text = resources.getString(R.string.btn_load_aes256_cmtk)

                zcmkType = Constants.ZCMKType.KEY_AES256
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.btLoadZCMK -> {
                val intent = Intent(this@AdminActivity, ZCMKActivity::class.java)
                intent.putExtra(Constants.EXTRA_LOAD_ZCMK, zcmkType.ordinal)
                startActivity(intent)
            }

            R.id.btLoadCMI -> {
                val intent = Intent(this@AdminActivity, CmiCmaCmtkTR31Activity::class.java)
                intent.putExtra(Constants.EXTRA_LOAD_ZCMK, zcmkType.ordinal)
                intent.putExtra(Constants.EXTRA_LOAD_OTHER, Constants.KeyBlockType.CMI.ordinal)
                startActivity(intent)
            }

            R.id.btLoadCMA -> {
                val intent = Intent(this@AdminActivity, CmiCmaCmtkTR31Activity::class.java)
                intent.putExtra(Constants.EXTRA_LOAD_ZCMK, zcmkType.ordinal)
                intent.putExtra(Constants.EXTRA_LOAD_OTHER, Constants.KeyBlockType.CMA.ordinal)
                startActivity(intent)
            }

            R.id.btLoadCMTK -> {
                val intent = Intent(this@AdminActivity, CmiCmaCmtkTR31Activity::class.java)
                intent.putExtra(Constants.EXTRA_LOAD_ZCMK, zcmkType.ordinal)
                intent.putExtra(Constants.EXTRA_LOAD_OTHER, Constants.KeyBlockType.CMTK.ordinal)
                startActivity(intent)
            }

            R.id.btLoadKeyVer -> {
                //import key files
                readfile()
                //startActivity(Intent(this@AdminActivity, LoadKeyVerActivity::class.java))
            }

            R.id.btDelZCMK -> {
                var encryptedOut = ByteArray(16)
                val ret = PedApi.PEDDes_Api(Constants.ZCMK_INDEX, 0x03, 0x01, CommonConvert.ascStringToBCD("0000000000000000"), 8, encryptedOut)
                val kcv = CommonConvert.bcdToASCString(encryptedOut, 0, 3)
                Utils.showToast(applicationContext, "KCV value : $kcv")
                /*val dialog = ConfirmDialog()
                dialog.setCancelText("NO")
                    .setConfirmText("YES")
                    .setTvInfo("Delete the key?")
                    .setOnBtnClickListener(object : ConfirmDialog.OnBtnClickListener {
                        override fun onConfirmClick() {
                            //val ret = PedApi.PedErase(1, Constants.ZCMK_INDEX)
                            val ret = PedApi.PedErase_Api(1,Constants.ZCMK_INDEX)
                            if (ret) {
                                DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_ZCMK_INFO)
                                Utils.showToast(applicationContext, resources.getString(R.string.delete_key_success))
                            } else {
                                Utils.showToast(applicationContext, resources.getString(R.string.delete_key_fail))
                            }
                        }

                        override fun onCancelClick() {
                            dialog.dismiss()
                        }

                    })

                dialog.show(supportFragmentManager, "DelZCMK")*/
            }


            R.id.btDelCMI -> {
                val dialog = ConfirmDialog()
                dialog.setCancelText("NO")
                    .setConfirmText("YES")
                    .setTvInfo("Delete the key?")
                    .setOnBtnClickListener(object : ConfirmDialog.OnBtnClickListener {
                        override fun onConfirmClick() {
                            val ret = PedApi.PedErase(1, Constants.CMI_INDEX)
                            if (ret) {
                                DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_CMI_KEY_INFO)
                                Utils.showToast(applicationContext, resources.getString(R.string.delete_key_success))
                            } else {
                                Utils.showToast(applicationContext, resources.getString(R.string.delete_key_fail))
                            }
                        }

                        override fun onCancelClick() {
                            dialog.dismiss()
                        }

                    })

                dialog.show(supportFragmentManager, "DelCMI")
            }

            R.id.btDelCMA -> {
                val dialog = ConfirmDialog()
                dialog.setCancelText("NO")
                    .setConfirmText("YES")
                    .setTvInfo("Delete the key?")
                    .setOnBtnClickListener(object : ConfirmDialog.OnBtnClickListener {
                        override fun onConfirmClick() {
                            val ret = PedApi.PedErase(1, Constants.CMA_INDEX)
                            if (ret) {
                                DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_CMA_KEY_INFO)
                                Utils.showToast(applicationContext, resources.getString(R.string.delete_key_success))
                            } else {
                                Utils.showToast(applicationContext, resources.getString(R.string.delete_key_fail))
                            }
                        }

                        override fun onCancelClick() {
                            dialog.dismiss()
                        }

                    })

                dialog.show(supportFragmentManager, "DelCMA")
            }

            R.id.btDelCMTK -> {
                startActivity(Intent(this@AdminActivity, DeleteKeyActivity::class.java))
            }

            R.id.btDelVer -> {
                DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_KEY_VER_INFO)
            }

            R.id.btDelAll -> {
                val conDialog = ConfirmDialog()
                conDialog.setTvInfo("Delete all of the keys?")
                    .setConfirmText("YES")
                    .setCancelText("NO")
                    .setOnBtnClickListener(object : ConfirmDialog.OnBtnClickListener {
                        override fun onConfirmClick() {
                            msgDialog = MsgDialog()
                            msgDialog!!.setText(resources.getString(R.string.deleting_keys))
                            msgDialog!!.show(supportFragmentManager, "")
                            deleteKeys = launch(Dispatchers.IO) {
                                //val eraseR = PedApi.PedErase_Api()
                                val eraseR = 1
                                if (eraseR == 1) {
                                    //DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_ZCMK_INFO)
                                    DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_CMTK_KEY_INFO)
                                    DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_CMA_KEY_INFO)
                                    DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_CMI_KEY_INFO)
                                    DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_KEY_VER_INFO)
                                    DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_KEYIMPORT_INFO)
                                    DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_KEYEXPORT_INFO)
                                    handler!!.sendEmptyMessage(Constants.ERR_DEL_KEY_SUCCESS)
                                } else {
                                    handler!!.sendEmptyMessage(Constants.ERR_DEL_KEY_FAIL)
                                }
                            }
                        }

                        override fun onCancelClick() {
                            conDialog.dismiss()
                        }
                    })
                conDialog.show(supportFragmentManager, "DelAllKeys")
            }

            R.id.btPrint -> {
                //export the output file
                var path: String? = null
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + "keylog.nym"

                writefile("keylog.nvm")
/*
                zcmkList.clear()
                cmiList.clear()
                cmaList.clear()
                cmtkList.clear()
                keyVerList.clear()
                queryKeyInfo(zcmkList, cmiList, cmaList, cmtkList, keyVerList)
                if (zcmkList.size == 0 && cmiList.size == 0 && cmaList.size == 0 && cmtkList.size == 0) {
                    Utils.showToast(applicationContext, "No key exists")
                    return
                }

                if (keyVerList.size == 0) {
                    Utils.showToast(applicationContext, "No key version exists")
                    return
                }
                PrintUtil.printAllKey(zcmkList, cmiList, cmaList, cmtkList, keyVerList, handler!!)
*/
            }
        }
    }

    private fun initdevice() {
        val devices = UsbMassStorageDevice.getMassStorageDevices(this@AdminActivity)
        val usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        val permissionIntent = PendingIntent.getBroadcast(
            this@AdminActivity,
            0,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        for (device in devices) {
            storageDevice = device
            usbManager.requestPermission(device.usbDevice, permissionIntent)
        }
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

    private fun readfile(): Int {
        var strLine: String? = ""
        var mystring: String = ""
        var path: String? = null

        initdevice()
        storageDevice!!.init()
        val currentFs: FileSystem = storageDevice!!.partitions.get(0).fileSystem
        Log.d(Constants.LOG_TAG, "Capacity: " + currentFs.capacity)
        Log.d(Constants.LOG_TAG,"Occupied Space: " + currentFs.occupiedSpace)
        Log.d(Constants.LOG_TAG,"Free Space: " + currentFs.freeSpace)
        Log.d(Constants.LOG_TAG,"Chunk size: " + currentFs.chunkSize)
        val root = currentFs.rootDirectory
        val files = root.listFiles()
        var filename: UsbFile? = null
        for (file in files) {
            Log.d(Constants.LOG_TAG, file.name)
            if (!file.isDirectory) {
                Log.d(Constants.LOG_TAG, "" + file.length)
            }
            if (file.name.equals("TY", false)) {
                filename = file
                break
            }
        }
        val files1 = filename?.listFiles()
        if (!files1.isNullOrEmpty()) {
            var filename1: UsbFile? = null
            for (file1 in files1) {
                Log.d(Constants.LOG_TAG, file1.name)
                if (!file1.isDirectory) {
                    Log.d(Constants.LOG_TAG, "" + file1.length)
                }
                Log.d(Constants.LOG_TAG, "" + file1.absolutePath)
                if (file1.name.equals("keyset.nkf", false)) {
                    filename1 = file1
                    break
                }
                //file1.search(file1.absolutePath)
            }
/*            var files2 = filename1!!.listFiles()
            if (!files2.isNullOrEmpty()) {*/
                //val file: UsbFile = filename!!
                // read from a file
                val inputStream1: InputStream = UsbFileInputStream(filename1!!)
                //val buffer = ByteArray(currentFs.chunkSize)
                var bytes = inputStream1.readBytes()
                val fullString = String(bytes, StandardCharsets.UTF_8)
                Log.d(Constants.LOG_TAG + " fullString", fullString)

                storageDevice?.close()

                /*
                //   var keysetsdata = KeysetsData
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + "keyset.nkf"
                //path = "/storage/STRONTIUM/TY/keyset.nkf"
                val keyfile = File(path)
                if (!keyfile.exists()) {
                    return Constants.ERR_KEYFILE_NOT_EXISTS
                }
                val inputStream = FileInputStream(keyfile)
                val inputStreamReader = InputStreamReader(inputStream)
                val readBuffer = BufferedReader(inputStreamReader)
                DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_KEYIMPORT_INFO)
                while (strLine != null) {
                    strLine = readBuffer.readLine()
                    if (strLine != null) {
                        mystring = mystring + strLine
                    }
                }

                readBuffer.close()
                inputStreamReader.close()
                inputStream.close()*/
                var keysetsdata = Json.decodeFromString<KeysetsData>(fullString)

                DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_KEYIMPORT_INFO)

                var i = 0
                while (i < keysetsdata.keysets.size) {
                    var contentValues = ContentValues()
                    contentValues.put(DataBaseOpenHelper.DEVICE_SERIAL, keysetsdata.keysets[i].name)
                    Log.d(Constants.LOG_TAG, "" + keysetsdata.keysets[i].name)
                    contentValues.put(
                        DataBaseOpenHelper.KEY_KEY_TYPE,
                        keysetsdata.keysets[i].key[0].crypto
                    )
                    Log.d(Constants.LOG_TAG, "" + keysetsdata.keysets[i].key[0].crypto)
                    contentValues.put(DataBaseOpenHelper.KEY_KCV, keysetsdata.keysets[i].key[0].kcv)
                    Log.d(Constants.LOG_TAG, "" + keysetsdata.keysets[i].key[0].kcv)
                    contentValues.put(
                        DataBaseOpenHelper.ENCRYPTED_KEY,
                        keysetsdata.keysets[i].key[0].value
                    )
                    Log.d(Constants.LOG_TAG, "" + keysetsdata.keysets[i].key[0].value)
                    contentValues.put(
                        DataBaseOpenHelper.KEY_TMK_INDEX,
                        keysetsdata.keysets[i].key[0].index
                    )
                    Log.d(Constants.LOG_TAG, "" + keysetsdata.keysets[i].key[0].index)
                    DataBaseOpenHelper.insert(
                        DataBaseOpenHelper.TABLE_KEYIMPORT_INFO,
                        contentValues
                    )
                    i++
                }
                /*var formatterdate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                var date = LocalDateTime.now().format(formatterdate)
                var formattertime = DateTimeFormatter.ofPattern("hh:mm:ss")
                var time = LocalDateTime.now().format(formattertime)
                var contentValues1 = ContentValues()
                contentValues1.put(DataBaseOpenHelper.KEY_KEY_TYPE, 1)
                contentValues1.put(DataBaseOpenHelper.KEY_KCV, keysetsdata.keysets[0].key[0].kcv)
                Log.d(Constants.LOG_TAG, "" + keysetsdata.keysets[0].key[0].kcv)
                contentValues1.put(DataBaseOpenHelper.DEVICE_SERIAL, keysetsdata.keysets[0].name)
                Log.d(Constants.LOG_TAG, "" + keysetsdata.keysets[0].name)
                contentValues1.put(DataBaseOpenHelper.DATE, date)
                Log.d(Constants.LOG_TAG, "" + date)
                contentValues1.put(DataBaseOpenHelper.TIME, time)
                Log.d(Constants.LOG_TAG, "" + time)
                contentValues1.put(DataBaseOpenHelper.KEY_TMK_INDEX, 1)
                DataBaseOpenHelper.insert(
                    DataBaseOpenHelper.TABLE_KEYEXPORT_INFO,
                    contentValues1
                )
                Log.d(Constants.LOG_TAG, "" + "key export log wi")*/
                Utils.showToast(applicationContext, "import files successfully")
                return 0
            /*} else
                Utils.showToast(applicationContext, "file not found")*/
        } else
            Utils.showToast(applicationContext, "folder not found")
        return -1
    }

    private fun writefile(filename: String) {
        val keyexportInfo = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_KEYEXPORT_INFO, "")

        initdevice()
        storageDevice!!.init()
        val currentFs: FileSystem = storageDevice!!.partitions.get(0).fileSystem
        Log.d(Constants.LOG_TAG, "Capacity: " + currentFs.capacity)
        Log.d(Constants.LOG_TAG,"Occupied Space: " + currentFs.occupiedSpace)
        Log.d(Constants.LOG_TAG,"Free Space: " + currentFs.freeSpace)
        Log.d(Constants.LOG_TAG,"Chunk size: " + currentFs.chunkSize)
        val root = currentFs.rootDirectory
        val files = root.listFiles()
        for (file in files) {
            Log.d(Constants.LOG_TAG, file.name)
            if (!file.isDirectory)
                Log.d(Constants.LOG_TAG, "" + file.length)
            if (file.name.equals(filename, false)) {
                file.delete()
                break
            }
        }
        val outputfile = root.createFile(filename)

        val outputstream: OutputStream = UsbFileOutputStream(outputfile)
        var outputstreamwriter = OutputStreamWriter(outputstream)
        outputstreamwriter.write("Index, SerialNumber, Type, Usage, KSN, KCV, Result, Date, Time\n")
        if (keyexportInfo.count > 1) {
            while (keyexportInfo.moveToNext()) {
                //val keytype = keyexportInfo.getInt(1)
                val kcv = keyexportInfo.getString(2)
                Log.d(Constants.LOG_TAG, "KCV: $kcv")
                val serialno = keyexportInfo.getString(3)
                Log.d(Constants.LOG_TAG, "serial no: $serialno")
                val date = keyexportInfo.getString(4)
                Log.d(Constants.LOG_TAG, "date: $date")
                val time = keyexportInfo.getString(5)
                Log.d(Constants.LOG_TAG, "time: $time")
                val index = keyexportInfo.getInt(6)
                Log.d(Constants.LOG_TAG, "index: $index")
                val dataout = "${index}, ${serialno}, DES, KEK, N/A, ${kcv}, SUCC, ${date}, ${time}\n"
                outputstreamwriter.write(dataout)
            }
            outputstreamwriter.close()
            Utils.showToast(applicationContext, "Export successfully")
        } else
            Utils.showToast(applicationContext, "No keys to export ")
        storageDevice?.close()
    }

    private fun queryKeyInfo(zcmkList: MutableList<ZCMKInfo>, cmiList: MutableList<CmiCmaCmtkInfo>, cmaList: MutableList<CmiCmaCmtkInfo>, cmtkList: MutableList<CmiCmaCmtkInfo>, keyVerList: MutableList<KeyVerInfo>) {
        val zcmkInfo = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_ZCMK_INFO, "")
        val cmiInfo = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_CMI_KEY_INFO, "")
        val cmaInfo = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_CMA_KEY_INFO, "")
        val cmtkInfo = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_CMTK_KEY_INFO, "")
        val versionInfo = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_KEY_VER_INFO, "")

        if (zcmkInfo.count > 0) {
            while (zcmkInfo.moveToNext()) {
                val zcmkType = zcmkInfo.getInt(1)
                val zcmkKCV = zcmkInfo.getString(2)
                val keyIndex = zcmkInfo.getInt(3)
                zcmkList.add(ZCMKInfo(zcmkType, zcmkKCV, keyIndex))
            }
        }


        if (cmiInfo.count > 0) {
            while (cmiInfo.moveToNext()) {
                val keyType = cmiInfo.getInt(1)
                val keyIndex = cmiInfo.getInt(2)
                val keyKCV = cmiInfo.getString(3)
                cmiList.add(CmiCmaCmtkInfo(keyType, keyKCV, keyIndex))
            }
        }

        if (cmaInfo.count > 0) {
            while (cmaInfo.moveToNext()) {
                val keyType = cmaInfo.getInt(1)
                val keyIndex = cmaInfo.getInt(2)
                val keyKCV = cmaInfo.getString(3)
                cmaList.add(CmiCmaCmtkInfo(keyType, keyKCV, keyIndex))
            }
        }

        if (cmtkInfo.count > 0) {
            while (cmtkInfo.moveToNext()) {
                val keyType = cmtkInfo.getInt(1)
                val keyIndex = cmtkInfo.getInt(2)
                val keyKCV = cmtkInfo.getString(3)
                cmtkList.add(CmiCmaCmtkInfo(keyType, keyKCV, keyIndex))
            }
        }

        if (versionInfo.count > 0) {
            while (versionInfo.moveToNext()) {
                val keyType = versionInfo.getInt(1)
                val verKCV = versionInfo.getString(2)
                val keyVer = versionInfo.getString(3)
                keyVerList.add(KeyVerInfo(keyType, verKCV, keyVer))
            }
        }
    }

    @SuppressLint("HandlerLeak")
    inner class CusHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val what = msg.what
            if (what < 0) {
                if (msgDialog != null && msgDialog!!.isVisible) {
                    msgDialog!!.dismiss()
                }
            }

            when (what) {
                Constants.ERR_DEL_KEY_FAIL -> {
                    deleteKeys!!.cancel()
                    Utils.showToast(applicationContext, resources.getString(R.string.delete_key_fail))
                }

                Constants.ERR_DEL_KEY_SUCCESS -> {
                    deleteKeys!!.cancel()
                    Utils.showToast(applicationContext, resources.getString(R.string.delete_key_success))
                }

                Constants.STATUS_PRINT -> {
                    if (msgConfirmDialog.isVisible) {
                        msgConfirmDialog.dismiss()
                    }

                    val arg = msg.arg1
                    when (arg) {
                        0 -> {
                            zcmkList.clear()
                            cmiList.clear()
                            cmaList.clear()
                            cmtkList.clear()
                            keyVerList.clear()
                        }

                        2, 3 -> {
                            zcmkList.clear()
                            cmiList.clear()
                            cmaList.clear()
                            cmtkList.clear()
                            keyVerList.clear()
                            msgConfirmDialog.setConfirmText("Confirm")
                                .setButtonVisible(true)
                                .setTvInfo(msg.obj.toString())
                                .setOnConfirmClickListener(object : MsgConfirmDialog.OnConfirmClickListener {
                                    override fun onClick() {
                                        if (handler != null) {
                                            queryKeyInfo(zcmkList, cmiList, cmaList, cmtkList, keyVerList)
                                            PrintUtil.printAllKey(zcmkList, cmiList, cmaList, cmtkList, keyVerList, handler!!)
                                        }
                                    }
                                })
                            msgConfirmDialog.isCancelable = false
                            msgConfirmDialog.show(supportFragmentManager, "printZCMK")
                        }
                    }
                }
            }
        }
    }
}