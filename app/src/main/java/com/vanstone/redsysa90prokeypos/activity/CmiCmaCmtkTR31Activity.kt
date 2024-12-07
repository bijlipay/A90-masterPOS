package com.vanstone.redsysa90prokeypos.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.InputFilter
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.vanstone.msgdialog.ConfirmDialog
import com.vanstone.msgdialog.MsgConfirmDialog
import com.vanstone.redsysa90prokeypos.R
import com.vanstone.redsysa90prokeypos.bean.CmiCmaCmtkInfo
import com.vanstone.redsysa90prokeypos.func.HexTextWatcher
import com.vanstone.redsysa90prokeypos.func.TR31TextWatcher
import com.vanstone.redsysa90prokeypos.params.Constants
import com.vanstone.redsysa90prokeypos.tools.DataBaseOpenHelper
import com.vanstone.redsysa90prokeypos.tools.PrintUtil
import com.vanstone.redsysa90prokeypos.tools.Utils
import com.vanstone.trans.api.PedApi
import com.vanstone.utils.CommonConvert

class CmiCmaCmtkTR31Activity : AppCompatActivity(), View.OnClickListener {

    private var etKeyBlock: EditText? = null
    private var etKCV: EditText? = null
    private var etCMTKIndex: EditText? = null
    private var btConfirmKeyBlock: Button? = null
    private var zcmkType = Constants.ZCMKType.KEY_AES256.ordinal
    private var keyType = Constants.KeyBlockType.CMI.ordinal
    private val msgConfirmDialog = MsgConfirmDialog()
    private var handler: CusHandler? = null
    private var keyInfo: CmiCmaCmtkInfo? = null
    private var hexTextWatcher: HexTextWatcher? = null
    private var tr31Watcher: TR31TextWatcher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    private fun initData() {
        val intent = intent
        zcmkType = intent.getIntExtra(Constants.EXTRA_LOAD_ZCMK, Constants.ZCMKType.KEY_AES256.ordinal)
        keyType = intent.getIntExtra(Constants.EXTRA_LOAD_OTHER, Constants.KeyBlockType.CMI.ordinal)
        etKeyBlock!!.addTextChangedListener(tr31Watcher)

        if (keyType == Constants.KeyBlockType.CMTK.ordinal){
            etCMTKIndex!!.visibility = View.VISIBLE
        }

        if (zcmkType == Constants.ZCMKType.KEY_AES256.ordinal) {
            etKeyBlock!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(112))
        } else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal) {
            etKeyBlock!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(80))
        }

        etKeyBlock!!.hint = resources.getString(R.string.hint_input_TR31)

        handler = CusHandler()
    }

    private fun initView() {
        setContentView(R.layout.activity_cmicmacmtk_tr)
        etKeyBlock = findViewById(R.id.etKeyBlock)
        etKCV = findViewById(R.id.etKCV)
        etCMTKIndex = findViewById(R.id.etCMTKIndex)
        btConfirmKeyBlock = findViewById(R.id.btConfirmKeyBlock)
        btConfirmKeyBlock!!.setOnClickListener(this)
        val spinnerList = ArrayList<String?>()
        spinnerList.add(Constants.KEY_TYPE_CIPHERTEXT_TDES)
        spinnerList.add(Constants.KEY_TYPE_TR31)
        hexTextWatcher = HexTextWatcher(etKeyBlock!!)
        tr31Watcher = TR31TextWatcher(etKeyBlock!!)
    }

    override fun onClick(p0: View?) {
        //TODO remove
       /* if(zcmkType == Constants.ZCMKType.KEY_AES256.ordinal){
            etKeyBlock!!.setText("D0112E6TX00N00006A95B13E082726AABD6E9FA886596A71DD2AB2D23222D4719047A673C812EA6AF5ED813E08E0D0BBDFD71B4479D8F483")
            etKCV!!.setText("AAAAAA")
        }else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal){
            etKeyBlock!!.setText("B0080E6TX00N00001A440FE0CE20A5EB38FBBC6FFDC9015A0F26923D85A6C5908F34223816837D67")
            etKCV!!.setText("AAAAAA")
        }*/

        var keyBlock = etKeyBlock!!.text.toString().trim()
        var textKCV = etKCV!!.text.toString().trim()

        if (keyBlock.isEmpty()) {
            Utils.showToast(applicationContext, resources.getString(R.string.hint_input_TR31))
            return
        }

        if ((zcmkType == Constants.ZCMKType.KEY_AES256.ordinal && keyBlock.length != 112) || (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal && keyBlock.length != 80)) {
            Utils.showToast(applicationContext, resources.getString(R.string.err_incorrect_tr32_len))
            return
        }

        if (textKCV.isEmpty() || textKCV.length != 6) {
            Utils.showToast(applicationContext, resources.getString(R.string.err_kcv_len))
            return
        }

        when (keyType) {
            Constants.KeyBlockType.CMI.ordinal -> {
                //TODO remove
                /*if(zcmkType == Constants.ZCMKType.KEY_AES256.ordinal){
                    keyBlock = "D0112E6TX00N00006A95B13E082726AABD6E9FA886596A71DD2AB2D23222D4719047A673C812EA6AF5ED813E08E0D0BBDFD71B4479D8F483"
                    textKCV = "A316F0"
                }else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal){
                    keyBlock = "B0080E6TX00N00001A440FE0CE20A5EB38FBBC6FFDC9015A0F26923D85A6C5908F34223816837D67"
                    textKCV = "A316F0"
                }*/

                loadCMI(zcmkType, keyBlock, textKCV)
            }

            Constants.KeyBlockType.CMA.ordinal -> {
                //TODO remove
                /*if(zcmkType == Constants.ZCMKType.KEY_AES256.ordinal){
                    keyBlock = "D0112E6TX00N000053C40EDD6710CABE8D2CC0C48738882F957A0EF30A8E91813F14A3F51D12FAE815A518A9A4DC6F7669DC8B2A6B275811"
                    textKCV = "121819"
                }else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal){
                    keyBlock = "B0080E6TX00N0000CDA5EB7351B2A1594BA265233090E2E454950D4E7C3AB544F2DA774C0A9C0E77"
                    textKCV = "121819"
                }*/

                loadCMA(zcmkType, keyBlock, textKCV)
            }

            Constants.KeyBlockType.CMTK.ordinal -> {
                //TODO remove
                /*if(zcmkType == Constants.ZCMKType.KEY_AES256.ordinal){
                    etKeyBlock!!.setText("D0112E6TX00N00004A631B41885601B39DE9A5676DF31574244B49D64FF9B8A52F20820DD618347BDD38FB6B776FEC1D1BB7F95996CE740B")
                    etCMTKIndex!!.setText("4")
                    etKCV!!.setText("83B0D8")

                    keyBlock = "D0112E6TX00N00004A631B41885601B39DE9A5676DF31574244B49D64FF9B8A52F20820DD618347BDD38FB6B776FEC1D1BB7F95996CE740B"
                    textKCV = "83B0D8"

                }else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal){
                    etKeyBlock!!.setText("B0080E6TX00N00004FFFD43FF3DE8355B535E01E3E21CA09536D90EC53747AD33BAE832C6C5E74B0")
                    etCMTKIndex!!.setText("4")
                    etKCV!!.setText("83B0D8")

                    keyBlock = "B0080E6TX00N00004FFFD43FF3DE8355B535E01E3E21CA09536D90EC53747AD33BAE832C6C5E74B0"
                    textKCV = "83B0D8"
                }*/

                val index = etCMTKIndex!!.text.toString().trim()
                if (index.isEmpty()){
                    Utils.showToast(applicationContext, resources.getString(R.string.input_cmtk_index))
                    return
                }

                if (index.toInt() !in 4 .. 13){
                    Utils.showToast(applicationContext, resources.getString(R.string.cmtk_index_arrange))
                    return
                }

                if(PedApi.isKeyExist_Api(1, index.toInt())){
                    val confirmDialog = ConfirmDialog()
                    confirmDialog.setConfirmText("YES")
                        .setCancelText("NO")
                        .setTvInfo("There is a key exists, replace it or not?")
                        .setCancelAble(false)
                        .setOnBtnClickListener(object : ConfirmDialog.OnBtnClickListener{
                            override fun onConfirmClick() {
                                confirmDialog.dismiss()
                                DataBaseOpenHelper.delete(DataBaseOpenHelper.TABLE_CMTK_KEY_INFO, " ${DataBaseOpenHelper.KEY_CMTK_INDEX}=?", arrayOf(index))
                                PedApi.PedErase(1, index.toInt())
                                loadCMTK(zcmkType, keyBlock, textKCV, index.toInt())
                            }

                            override fun onCancelClick() {
                                confirmDialog.dismiss()
                            }

                        }).show(supportFragmentManager, "KeyExistDialog")
                }else{
                    loadCMTK(zcmkType, keyBlock, textKCV, index.toInt())
                }
            }
        }

        etKeyBlock!!.setText("")
        etKCV!!.setText("")
        if (etCMTKIndex!!.isVisible){
            etCMTKIndex!!.setText("")
        }
    }

    private fun loadCMI(zcmkType: Int, keyBlock: String, textKCV: String) {
        val encryptedOut = ByteArray(8)

        val keyLen = if(zcmkType == Constants.ZCMKType.KEY_AES256.ordinal){
            32
        }else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal){
            16
        }else{
            0
        }

        var ret = PedApi.TR31_WriteKey_Api(Constants.CMI_INDEX, Constants.ZCMK_INDEX, keyBlock.toByteArray(Charsets.UTF_8), keyLen)
        if (ret != 0) {
            Utils.showToast(applicationContext, resources.getString(R.string.err_load_key_fail))
            etKeyBlock!!.text.clear()
            etKCV!!.text.clear()
            return
        }
        ret = PedApi.PEDDes_Api(Constants.CMI_INDEX, 0x03, 0x01, CommonConvert.ascStringToBCD("0000000000000000"), 8, encryptedOut)
        if (CommonConvert.bcdToASCString(encryptedOut.copyOfRange(0, 3)) == textKCV) {

            if(zcmkType == Constants.ZCMKType.KEY_AES256.ordinal) {
                Utils.showToast(applicationContext, resources.getString(R.string.load_aes256_cmi_success))
            }else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal){
                Utils.showToast(applicationContext, resources.getString(R.string.load_tdes_cmi_success))
            }

            DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_CMI_KEY_INFO)

            val contentValues = ContentValues()
            contentValues.put(DataBaseOpenHelper.KEY_KEY_TYPE, keyType)
            contentValues.put(DataBaseOpenHelper.KEY_KCV, textKCV)
            contentValues.put(DataBaseOpenHelper.KEY_CMI_INDEX, Constants.CMI_INDEX)
            contentValues.put(DataBaseOpenHelper.KEY_CI_INDEX, Constants.CI_INDEX)
            DataBaseOpenHelper.insert(DataBaseOpenHelper.TABLE_CMI_KEY_INFO, contentValues)

            val cmiKeyInfo = CmiCmaCmtkInfo(keyType, textKCV, Constants.CMI_INDEX)
            keyInfo = cmiKeyInfo
            PrintUtil.printCmiCmaCmtk(cmiKeyInfo, handler!!)

        } else {
            Utils.showToast(applicationContext, resources.getString(R.string.err_kcv))
            etKeyBlock!!.text.clear()
            etKCV!!.text.clear()
            PedApi.PedErase(1, Constants.CMI_INDEX)
        }
    }

    private fun loadCMA(zcmkType: Int, keyBlock: String, textKCV: String) {
        val encryptedOut = ByteArray(8)

        val keyLen = if(zcmkType == Constants.ZCMKType.KEY_AES256.ordinal){
            32
        }else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal){
            16
        }else{
            0
        }

        var ret = PedApi.TR31_WriteKey_Api(Constants.CMA_INDEX, Constants.ZCMK_INDEX, keyBlock.toByteArray(Charsets.UTF_8), keyLen)
        if (ret != 0) {
            Utils.showToast(applicationContext, resources.getString(R.string.err_load_key_fail))
            etKeyBlock!!.text.clear()
            etKCV!!.text.clear()
            return
        }
        ret = PedApi.PEDDes_Api(Constants.CMA_INDEX, 0x03, 0x01, CommonConvert.ascStringToBCD("0000000000000000"), 8, encryptedOut)
        if (CommonConvert.bcdToASCString(encryptedOut.copyOfRange(0, 3)) == textKCV) {

            if(zcmkType == Constants.ZCMKType.KEY_AES256.ordinal) {
                Utils.showToast(applicationContext, resources.getString(R.string.load_aes256_cma_success))
            }else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal){
                Utils.showToast(applicationContext, resources.getString(R.string.load_tdes_cma_success))
            }

            DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_CMA_KEY_INFO)

            val contentValues = ContentValues()
            contentValues.put(DataBaseOpenHelper.KEY_KEY_TYPE, keyType)
            contentValues.put(DataBaseOpenHelper.KEY_KCV, textKCV)
            contentValues.put(DataBaseOpenHelper.KEY_CMA_INDEX, Constants.CMA_INDEX)
            contentValues.put(DataBaseOpenHelper.KEY_CA_INDEX, Constants.CA_INDEX)
            DataBaseOpenHelper.insert(DataBaseOpenHelper.TABLE_CMA_KEY_INFO, contentValues)

            val cmaKeyInfo = CmiCmaCmtkInfo(keyType, textKCV, Constants.CMA_INDEX)
            keyInfo = cmaKeyInfo
            PrintUtil.printCmiCmaCmtk(cmaKeyInfo, handler!!)

        } else {
            Utils.showToast(applicationContext, resources.getString(R.string.err_kcv))
            etKeyBlock!!.text.clear()
            etKCV!!.text.clear()
            PedApi.PedErase(1, Constants.CMA_INDEX)
        }
    }

    private fun loadCMTK(zcmkType: Int, keyBlock: String, textKCV: String, cmtkIndex: Int) {
        val encryptedOut = ByteArray(8)

        val keyLen = if(zcmkType == Constants.ZCMKType.KEY_AES256.ordinal){
            32
        }else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal){
            16
        }else{
            0
        }

        var ret = PedApi.TR31_WriteKey_Api(cmtkIndex, Constants.ZCMK_INDEX, keyBlock.toByteArray(Charsets.UTF_8), keyLen)
        if (ret != 0) {
            Utils.showToast(applicationContext, resources.getString(R.string.err_load_key_fail))
            etKeyBlock!!.text.clear()
            etKCV!!.text.clear()
            etCMTKIndex!!.text.clear()
            return
        }
        ret = PedApi.PEDDes_Api(cmtkIndex, 0x03, 0x01, CommonConvert.ascStringToBCD("0000000000000000"), 8, encryptedOut)
        if (CommonConvert.bcdToASCString(encryptedOut.copyOfRange(0, 3)) == textKCV) {

            if(zcmkType == Constants.ZCMKType.KEY_AES256.ordinal) {
                Utils.showToast(applicationContext, resources.getString(R.string.load_aes256_cmtk_success))
            }else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal){
                Utils.showToast(applicationContext, resources.getString(R.string.load_tdes_cmtk_success))
            }

            val cursor = DataBaseOpenHelper.query(DataBaseOpenHelper.TABLE_CMTK_KEY_INFO, "where ${DataBaseOpenHelper.KEY_CMTK_INDEX}=${cmtkIndex.toInt()}")
            if (cursor.count > 0){
                DataBaseOpenHelper.delete(DataBaseOpenHelper.TABLE_CMTK_KEY_INFO, "where ${DataBaseOpenHelper.KEY_CMTK_INDEX}=?", arrayOf(cmtkIndex.toString()))
            }

            val contentValues = ContentValues()
            contentValues.put(DataBaseOpenHelper.KEY_KEY_TYPE, keyType)
            contentValues.put(DataBaseOpenHelper.KEY_KCV, textKCV)
            contentValues.put(DataBaseOpenHelper.KEY_CMTK_INDEX, cmtkIndex)
            contentValues.put(DataBaseOpenHelper.KEY_CTK_INDEX, cmtkIndex + 6)
            DataBaseOpenHelper.insert(DataBaseOpenHelper.TABLE_CMTK_KEY_INFO, contentValues)

            val cmtkKeyInfo = CmiCmaCmtkInfo(keyType, textKCV, cmtkIndex)
            keyInfo = cmtkKeyInfo
            PrintUtil.printCmiCmaCmtk(cmtkKeyInfo, handler!!)

        } else {
            Utils.showToast(applicationContext, resources.getString(R.string.err_kcv))
            etKeyBlock!!.text.clear()
            etKCV!!.text.clear()
            etCMTKIndex!!.text.clear()
            PedApi.PedErase(1, cmtkIndex)
        }
    }


    @SuppressLint("HandlerLeak")
    inner class CusHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val what = msg.what
            when (what) {

                Constants.STATUS_PRINT -> {
                    if (msgConfirmDialog.isVisible) {
                        msgConfirmDialog.dismiss()
                    }

                    when (msg.arg1) {
                        2, 3 -> {
                            msgConfirmDialog.setConfirmText("Confirm")
                                .setButtonVisible(true)
                                .setTvInfo(msg.obj.toString())
                                .setOnConfirmClickListener(object : MsgConfirmDialog.OnConfirmClickListener {
                                    override fun onClick() {
                                        if (keyInfo != null && handler != null) {
                                            PrintUtil.printCmiCmaCmtk(keyInfo!!, handler!!)
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