package com.vanstone.redsysa90prokeypos.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.vanstone.msgdialog.MsgConfirmDialog
import com.vanstone.redsysa90prokeypos.R
import com.vanstone.redsysa90prokeypos.bean.CmiCmaCmtkInfo
import com.vanstone.redsysa90prokeypos.func.HexTextWatcher
import com.vanstone.redsysa90prokeypos.params.Constants
import com.vanstone.redsysa90prokeypos.tools.DataBaseOpenHelper
import com.vanstone.redsysa90prokeypos.tools.PrintUtil
import com.vanstone.redsysa90prokeypos.tools.Utils
import com.vanstone.trans.api.PedApi
import com.vanstone.utils.CommonConvert
import kotlin.experimental.xor

class CmiCmaCmtkComActivity : AppCompatActivity(), OnClickListener {

    private var etComponent: EditText? = null
    private var btConfirmCom: Button? = null
    private val msgConfirmDialog = MsgConfirmDialog()
    private var handler: CusHandler? = null
    private var keyType = Constants.KeyBlockType.CMI.ordinal
    private var currentCom = 0 // 0-component 1, 1-component 2, 2-component 3, 3-KCV, 4-index
    private val compMap = mutableMapOf<String, String>()
    private var keyInfo: CmiCmaCmtkInfo? = null
    private var kcv: String? = null
    private var index = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initData()
    }

    private fun initData() {
        handler = CusHandler()
        currentCom = 0
        val intent = intent
        keyType = intent.getIntExtra(Constants.EXTRA_LOAD_OTHER, Constants.KeyBlockType.CMI.ordinal)
    }

    private fun initView() {
        setContentView(R.layout.activity_cmicmacmtk_com)
        etComponent = findViewById(R.id.etComponent)
        btConfirmCom = findViewById(R.id.btConfirmCom)
        etComponent!!.addTextChangedListener(HexTextWatcher(etComponent!!))
        etComponent!!.filters = arrayOf(InputFilter.LengthFilter(32))
        etComponent!!.hint = resources.getString(R.string.hint_input_component_1)

        btConfirmCom!!.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        val text = etComponent!!.text.toString().trim()
        if (currentCom == 3) { // KCV
            if (text.isEmpty()) {
                Utils.showToast(applicationContext, resources.getString(R.string.et_input_kcv))
                return
            } else if (text.length != 6) {
                Utils.showToast(applicationContext, resources.getString(R.string.err_kcv))
                etComponent!!.setText("")
                return
            }

            kcv = text
        } else if (currentCom == 4) { // index
            if (text.isEmpty()) {
                Utils.showToast(applicationContext, resources.getString(R.string.input_key_index))
                return
            } else {
                val keyIndex = text.toInt()
                if (keyIndex !in 4..13) {
                    Utils.showToast(applicationContext, resources.getString(R.string.err_key_index))
                    return
                }
            }
        } else { // components
            if (text.isEmpty()) {
                Utils.showToast(applicationContext, resources.getString(R.string.input_component))
                return
            } else if (text.length != 32) {
                Utils.showToast(applicationContext, resources.getString(R.string.err_component_len))
                etComponent!!.setText("")
                return
            }
        }

        when (currentCom) {
            0 -> {
                currentCom = 1
                etComponent!!.hint = resources.getString(R.string.hint_input_component_2)
                etComponent!!.setText("")
                compMap[Constants.MAP_ZCMK_COMPONENT_1] = text
            }

            1 -> {
                currentCom = 2
                etComponent!!.hint = resources.getString(R.string.hint_input_component_3)
                etComponent!!.setText("")
                compMap[Constants.MAP_ZCMK_COMPONENT_2] = text
            }

            2 -> {
                currentCom = 3
                etComponent!!.hint = resources.getString(R.string.et_input_kcv)
                etComponent!!.filters = arrayOf(InputFilter.LengthFilter(6))
                etComponent!!.setText("")
                compMap[Constants.MAP_ZCMK_COMPONENT_3] = text
            }

            3 -> {
                etComponent!!.setText("")
                if (keyType == Constants.KeyBlockType.CMI.ordinal) {
                    currentCom = 0
                    etComponent!!.hint = resources.getString(R.string.hint_input_component_1)
                    etComponent!!.filters = arrayOf(InputFilter.LengthFilter(32))
                } else if (keyType == Constants.KeyBlockType.CMA.ordinal) {
                    currentCom = 0
                    etComponent!!.hint = resources.getString(R.string.hint_input_component_1)
                    etComponent!!.filters = arrayOf(InputFilter.LengthFilter(32))
                } else if (keyType == Constants.KeyBlockType.CMTK.ordinal) {
                    currentCom = 4
                    etComponent!!.hint = resources.getString(R.string.input_cmtk_index)
                    etComponent!!.filters = arrayOf(InputFilter.LengthFilter(2))
                    etComponent!!.inputType = InputType.TYPE_CLASS_NUMBER
                }

                kcv = text
                val encryptedOut = ByteArray(8)

                if (keyType == Constants.KeyBlockType.CMI.ordinal || keyType == Constants.KeyBlockType.CMA.ordinal) {
                    val key = ByteArray(16)
                    generateKey(key)

                    if (keyType == Constants.KeyBlockType.CMI.ordinal) {
                        var ret = PedApi.PEDWriteMKey_Api(Constants.CMI_INDEX, 0x03, key)
                        if (ret != 0) {
                            Utils.showToast(applicationContext, resources.getString(R.string.err_load_key_fail))
                            return
                        }

                        ret = PedApi.PEDDes_Api(Constants.CMI_INDEX, 0x03, 0x01, CommonConvert.ascStringToBCD("0000000000000000"), 8, encryptedOut)
                        if ((ret == 0) && (CommonConvert.bcdToASCString(encryptedOut.copyOfRange(0, 3)) == kcv)) {

                            Utils.showToast(applicationContext, resources.getString(R.string.load_cmi_success))
                            DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_CMI_KEY_INFO)

                            val contentValues = ContentValues()
                            contentValues.put(DataBaseOpenHelper.KEY_KEY_TYPE, keyType)
                            contentValues.put(DataBaseOpenHelper.KEY_KCV, kcv)
                            contentValues.put(DataBaseOpenHelper.KEY_CMI_INDEX, Constants.CMI_INDEX)
                            contentValues.put(DataBaseOpenHelper.KEY_CI_INDEX, Constants.CI_INDEX)
                            DataBaseOpenHelper.insert(DataBaseOpenHelper.TABLE_CMI_KEY_INFO, contentValues)

                            val cmiKeyInfo = CmiCmaCmtkInfo(keyType, kcv!!, Constants.CMI_INDEX)
                            keyInfo = cmiKeyInfo
                            PrintUtil.printCmiCmaCmtk(cmiKeyInfo, handler!!)

                        } else {
                            currentCom = 0
                            etComponent!!.hint = resources.getString(R.string.hint_input_component_1)
                            etComponent!!.filters = arrayOf(InputFilter.LengthFilter(32))
                            Utils.showToast(applicationContext, resources.getString(R.string.err_kcv))
                            etComponent!!.text.clear()
                            PedApi.PedErase(1, Constants.CMI_INDEX)
                        }
                    } else {
                        var ret = PedApi.PEDWriteMKey_Api(Constants.CMA_INDEX, 0x03, key)
                        if (ret != 0) {
                            Utils.showToast(applicationContext, resources.getString(R.string.err_load_key_fail))
                            return
                        }

                        ret = PedApi.PEDDes_Api(Constants.CMA_INDEX, 0x03, 0x01, CommonConvert.ascStringToBCD("0000000000000000"), 8, encryptedOut)
                        if ((ret == 0) && (CommonConvert.bcdToASCString(encryptedOut.copyOfRange(0, 3)) == kcv)) {

                            Utils.showToast(applicationContext, resources.getString(R.string.load_cma_success))
                            DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_CMA_KEY_INFO)

                            val contentValues = ContentValues()
                            contentValues.put(DataBaseOpenHelper.KEY_KEY_TYPE, keyType)
                            contentValues.put(DataBaseOpenHelper.KEY_KCV, kcv)
                            contentValues.put(DataBaseOpenHelper.KEY_CMA_INDEX, Constants.CMA_INDEX)
                            contentValues.put(DataBaseOpenHelper.KEY_CA_INDEX, Constants.CA_INDEX)
                            DataBaseOpenHelper.insert(DataBaseOpenHelper.TABLE_CMA_KEY_INFO, contentValues)

                            val cmaKeyInfo = CmiCmaCmtkInfo(keyType, kcv!!, Constants.CMA_INDEX)
                            keyInfo = cmaKeyInfo
                            PrintUtil.printCmiCmaCmtk(cmaKeyInfo, handler!!)

                        } else {
                            currentCom = 0
                            etComponent!!.hint = resources.getString(R.string.hint_input_component_1)
                            etComponent!!.filters = arrayOf(InputFilter.LengthFilter(32))
                            Utils.showToast(applicationContext, resources.getString(R.string.err_kcv))
                            etComponent!!.text.clear()
                            PedApi.PedErase(1, Constants.CMA_INDEX)
                        }
                    }
                }
            }

            4 -> {
                etComponent!!.setText("")
                currentCom = 0
                etComponent!!.hint = resources.getString(R.string.hint_input_component_1)
                etComponent!!.filters = arrayOf(InputFilter.LengthFilter(32))
                etComponent!!.addTextChangedListener(HexTextWatcher(etComponent!!))
                index = text.toInt()

                val encryptedOut = ByteArray(8)
                val key = ByteArray(16)
                generateKey(key)

                var ret = PedApi.PEDWriteMKey_Api(index, 0x03, key)
                if (ret != 0) {
                    Utils.showToast(applicationContext, resources.getString(R.string.err_load_key_fail))
                    return
                }

                ret = PedApi.PEDDes_Api(index, 0x03, 0x01, CommonConvert.ascStringToBCD("0000000000000000"), 8, encryptedOut)
                if ((ret == 0) && (CommonConvert.bcdToASCString(encryptedOut.copyOfRange(0, 3)) == kcv)) {

                    Utils.showToast(applicationContext, resources.getString(R.string.load_cmtk_success))
                    DataBaseOpenHelper.delete(DataBaseOpenHelper.TABLE_CMTK_KEY_INFO, " ${DataBaseOpenHelper.KEY_CMTK_INDEX}=?", arrayOf(text))
                    val contentValues = ContentValues()
                    contentValues.put(DataBaseOpenHelper.KEY_KEY_TYPE, keyType)
                    contentValues.put(DataBaseOpenHelper.KEY_KCV, kcv)
                    contentValues.put(DataBaseOpenHelper.KEY_CMTK_INDEX, index)
                    contentValues.put(DataBaseOpenHelper.KEY_CTK_INDEX, index - 2)
                    DataBaseOpenHelper.insert(DataBaseOpenHelper.TABLE_CMTK_KEY_INFO, contentValues)

                    val cmaKeyInfo = CmiCmaCmtkInfo(keyType, kcv!!, index)
                    keyInfo = cmaKeyInfo
                    PrintUtil.printCmiCmaCmtk(cmaKeyInfo, handler!!)

                } else {
                    currentCom = 0
                    etComponent!!.hint = resources.getString(R.string.hint_input_component_1)
                    etComponent!!.filters = arrayOf(InputFilter.LengthFilter(32))
                    Utils.showToast(applicationContext, resources.getString(R.string.err_kcv))
                    etComponent!!.text.clear()
                    PedApi.PedErase(1, index)
                }
            }
        }
    }


    private fun generateKey(key: ByteArray?): Int {
        if (compMap.size < 3 || key == null) {
            return -1
        }

        val comp1Str = compMap[Constants.MAP_ZCMK_COMPONENT_1]
        val comp2Str = compMap[Constants.MAP_ZCMK_COMPONENT_2]
        val comp3Str = compMap[Constants.MAP_ZCMK_COMPONENT_3]

        val comp1 = CommonConvert.ascStringToBCD(comp1Str)
        val comp2 = CommonConvert.ascStringToBCD(comp2Str)
        val comp3 = CommonConvert.ascStringToBCD(comp3Str)

        for (i in comp1.indices) {
            key[i] = comp1[i] xor comp2[i] xor comp3[i]
        }

        return 0
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