package com.vanstone.redsysa90prokeypos.activity

import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.vanstone.msgdialog.MsgConfirmDialog
import com.vanstone.redsysa90prokeypos.R
import com.vanstone.redsysa90prokeypos.bean.FileKeyVerInfo
import com.vanstone.redsysa90prokeypos.params.Constants
import com.vanstone.redsysa90prokeypos.tools.DataBaseOpenHelper
import com.vanstone.redsysa90prokeypos.tools.PrintUtil
import com.vanstone.redsysa90prokeypos.tools.Utils
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class LoadKeyVerActivity : AppCompatActivity(), OnClickListener{
    private var etCMIVerKCV: EditText? = null
    private var etCMTKVerKCV: EditText? = null
    private var btConfirmVerKCV: Button? = null
    private var handler: CusHandler? = null
    private var cmiVer: FileKeyVerInfo? = null
    private var cmtkVer: FileKeyVerInfo? = null
    private val msgConfirmDialog = MsgConfirmDialog()
    private val fileKeyVerInfoList = mutableListOf<FileKeyVerInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        handler = CusHandler()

        val ret = checkKeyFile()
        if (ret != 0) {
            Utils.showToast(applicationContext, resources.getString(R.string.err_key_file_not_exists))
        }
    }

    private fun initView() {
        setContentView(R.layout.activity_load_key_ver)

        etCMIVerKCV = findViewById(R.id.etCMIVerKCV)
        etCMTKVerKCV = findViewById(R.id.etCMTKVerKCV)
        btConfirmVerKCV = findViewById(R.id.btConfirmVerKCV)

        btConfirmVerKCV!!.setOnClickListener(this)
    }



    private fun checkKeyFile(): Int {
        var path: String? = null

        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + Constants.FILE_TDES

        Utils.printLogs("Key File Path:  $path")

        val keyFile = File(path)

        if (!keyFile.exists()) {
            return Constants.ERR_KEYFILE_NOT_EXISTS
        }

        readFile(keyFile)
        Utils.printLogs("list size = ${fileKeyVerInfoList.size}")

        return 0
    }

    private fun readFile(file: File) {
        var strLine: String? = ""
        var keyVerKCV: String? = null
        var keyVer: String? = null

        fileKeyVerInfoList.clear()

        val inputStream = FileInputStream(file)
        val inputStreamReader = InputStreamReader(inputStream)
        val readBuffer = BufferedReader(inputStreamReader)
        while (strLine != null) {
            strLine = readBuffer.readLine()
            if (strLine != null) {
                keyVerKCV = strLine.substring(1, 4)
                keyVer = strLine.substring(7, 39)
                fileKeyVerInfoList.add(FileKeyVerInfo(keyVerKCV, keyVer))
            }
        }

        readBuffer.close()
        inputStreamReader.close()
        inputStream.close()
    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.btConfirmVerKCV -> {
                val kcvCMIVer = etCMIVerKCV!!.text.toString().trim()
                val kcvCMTKVer = etCMTKVerKCV!!.text.toString().trim()

                if (kcvCMIVer.length != 3 || kcvCMTKVer.length != 3){
                    Utils.showToast(applicationContext, resources.getString(R.string.err_kcv_len))
                    return
                }

                for (info in fileKeyVerInfoList){
                    if (info.kcv == kcvCMIVer){
                        cmiVer = info
                        break
                    }
                }

                for (info in fileKeyVerInfoList){
                    if (info.kcv == kcvCMTKVer){
                        cmtkVer = info
                        break
                    }
                }

                if (cmiVer == null || cmtkVer == null){
                    Utils.showToast(applicationContext, resources.getString(R.string.err_no_key_matches))
                    return
                }

                DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_KEY_VER_INFO)

                var contentValues = ContentValues()
                contentValues.put(DataBaseOpenHelper.KEY_KEY_TYPE, Constants.KeyBlockType.CMI.ordinal) // TDES or AES256
                contentValues.put(DataBaseOpenHelper.KEY_VERSION_KCV, cmiVer!!.kcv)
                contentValues.put(DataBaseOpenHelper.KEY_VERSION, cmiVer!!.keyVersion)
                DataBaseOpenHelper.insert(DataBaseOpenHelper.TABLE_KEY_VER_INFO, contentValues)

                contentValues = ContentValues()
                contentValues.put(DataBaseOpenHelper.KEY_KEY_TYPE, Constants.KeyBlockType.CMTK.ordinal) // TDES or AES256
                contentValues.put(DataBaseOpenHelper.KEY_VERSION_KCV, cmtkVer!!.kcv)
                contentValues.put(DataBaseOpenHelper.KEY_VERSION, cmtkVer!!.keyVersion)
                DataBaseOpenHelper.insert(DataBaseOpenHelper.TABLE_KEY_VER_INFO, contentValues)

                etCMIVerKCV!!.setText("")
                etCMTKVerKCV!!.setText("")

                PrintUtil.printKeyVer(cmiVer!!, cmtkVer!!, handler!!)
            }
        }
    }

    inner class CusHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                Constants.STATUS_PRINT -> {

                    if (msgConfirmDialog.isVisible) {
                        msgConfirmDialog.dismiss()
                    }

                    val arg = msg.arg1
                    when(arg){
                        2, 3 -> {
                            msgConfirmDialog.setConfirmText("Confirm")
                                .setButtonVisible(true)
                                .setTvInfo(msg.obj.toString())
                                .setOnConfirmClickListener(object : MsgConfirmDialog.OnConfirmClickListener {
                                    override fun onClick() {
                                        if (cmiVer != null && cmtkVer != null && handler != null) {
                                            PrintUtil.printKeyVer(cmiVer!!, cmtkVer!!, handler!!)
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