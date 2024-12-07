package com.vanstone.redsysa90prokeypos.activity

import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.vanstone.msgdialog.MsgConfirmDialog
import com.vanstone.redsysa90prokeypos.R
import com.vanstone.redsysa90prokeypos.bean.ZCMKInfo
import com.vanstone.redsysa90prokeypos.func.HexTextWatcher
import com.vanstone.redsysa90prokeypos.params.Constants
import com.vanstone.redsysa90prokeypos.tools.DataBaseOpenHelper
import com.vanstone.redsysa90prokeypos.tools.PrintUtil
import com.vanstone.redsysa90prokeypos.tools.Utils
import com.vanstone.trans.api.PedApi
import com.vanstone.utils.CommonConvert
import kotlin.experimental.and
import kotlin.experimental.xor


class ZCMKActivity : AppCompatActivity(), View.OnClickListener {

    private var etComponent: EditText? = null
    private var btConfirm: Button? = null
    private var zcmkType = Constants.ZCMKType.KEY_AES256.ordinal
    private var currentComponent = 1
    private val compMap = mutableMapOf<String, String>()
    private var genResult = -1
    private var handler: CusHandler? = null
    private var zcmkInfo: ZCMKInfo? = null
    private val msgConfirmDialog = MsgConfirmDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initData()
    }

    private fun initData() {
        handler = CusHandler()

        val intent = intent
        zcmkType = intent.getIntExtra(Constants.EXTRA_LOAD_ZCMK, Constants.ZCMKType.KEY_AES256.ordinal)

        if (zcmkType == Constants.ZCMKType.KEY_AES256.ordinal) {
            etComponent!!.filters = arrayOf<InputFilter>(LengthFilter(64))
        } else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal) {
            etComponent!!.filters = arrayOf<InputFilter>(LengthFilter(32))
        }
    }

    private fun initView() {
        setContentView(R.layout.activity_zcmk)
        etComponent = findViewById(R.id.etComponent)
        btConfirm = findViewById(R.id.btConfirmCom)

        etComponent!!.hint = resources.getString(R.string.hint_input_component_1)
        etComponent!!.addTextChangedListener(HexTextWatcher(etComponent!!))
        btConfirm!!.setOnClickListener(this)


    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
            R.id.btConfirmCom -> {
                genResult = -1
                val str = etComponent!!.text.toString().trim()

                if (str.isEmpty()) {
                    if (currentComponent != 3) {
                        Utils.showToast(applicationContext, resources.getString(R.string.input_component))
                    } else {
                        Utils.showToast(applicationContext, resources.getString(R.string.et_input_kcv))
                    }

                    return
                }

                if (currentComponent != 3) {
                    if (zcmkType == Constants.ZCMKType.KEY_AES256.ordinal) {
                        if (str.length != 64) {
                            Utils.showToast(applicationContext, resources.getString(R.string.err_component_len))
                            return
                        }

                    } else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal) {
                        if (str.length != 32) {
                            Utils.showToast(applicationContext, resources.getString(R.string.err_component_len))
                            return
                        }
                    }
                } else {
                    if (str.length != 6) {
                        Utils.showToast(applicationContext, resources.getString(R.string.err_kcv_len))
                        return
                    }
                }


                when (currentComponent) {
                    1 -> {
                        currentComponent = 2
                        compMap[Constants.MAP_ZCMK_COMPONENT_1] = str
                        etComponent!!.setText("")
                        etComponent!!.hint = resources.getString(R.string.hint_input_component_2)
                    }

                    2 -> {
                        currentComponent = 3
                        compMap[Constants.MAP_ZCMK_COMPONENT_2] = str
                        etComponent!!.setText("")
                        etComponent!!.hint = resources.getString(R.string.et_input_kcv)
                        etComponent!!.filters = arrayOf<InputFilter>(LengthFilter(6))
                    }

                    3 -> {
                        val kcv = etComponent!!.text.toString().trim()

                        val zcmk = when (zcmkType) {
                            Constants.ZCMKType.KEY_AES256.ordinal -> {
                                ByteArray(32)
                            }

                            Constants.ZCMKType.KEY_TDES.ordinal -> {
                                ByteArray(16)
                            }

                            else -> {
                                null
                            }
                        }

                        genResult = generateZCMK(zcmk)

                        if (genResult == -1) {
                            Utils.showToast(applicationContext, resources.getString(R.string.err_zcmk_map))
                            currentComponent = 1
                            etComponent!!.hint = resources.getString(R.string.hint_input_component_1)
                            compMap.clear()

                            if (zcmkType == Constants.ZCMKType.KEY_AES256.ordinal) {
                                etComponent!!.filters = arrayOf<InputFilter>(LengthFilter(64))
                            } else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal) {
                                etComponent!!.filters = arrayOf<InputFilter>(LengthFilter(32))
                            }

                            return
                        }

                        if (zcmkType == Constants.ZCMKType.KEY_AES256.ordinal) {
                            genResult = PedApi.pedAESKeySave(1, 32, zcmk)
                            if (genResult == 1){
                                genResult = 0 // PedApi.pedAESKeySave返回1时表示成功，其他的表示失败，下面的代码中判断如果genResult是0则是成功，其他则是失败，所以这里如果API如果返回1，则将genResult置为0
                            }
                        } else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal) {
                            PedApi.PedErase_Api(1,Constants.ZCMK_INDEX)
                            genResult = PedApi.PEDWriteMKey_Api(Constants.ZCMK_INDEX, 0x03, zcmk)
                        }

                        if (genResult != 0) {
                            Utils.showToast(applicationContext, resources.getString(R.string.err_generate_zcmk))
                            currentComponent = 1
                            etComponent!!.hint = resources.getString(R.string.hint_input_component_1)
                            compMap.clear()

                            if (zcmkType == Constants.ZCMKType.KEY_AES256.ordinal) {
                                etComponent!!.filters = arrayOf<InputFilter>(LengthFilter(64))
                            } else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal) {
                                etComponent!!.filters = arrayOf<InputFilter>(LengthFilter(32))
                            }

                            return
                        } else {
                            val encryptedOut = ByteArray(16)
                            val enRet = when (zcmkType) {
                                Constants.ZCMKType.KEY_AES256.ordinal -> {
                                    PedApi.calAes_Api(Constants.ZCMK_INDEX, 1, CommonConvert.ascStringToBCD("00000000000000000000000000000000"), 16, CommonConvert.ascStringToBCD("00000000000000000000000000000000"), 16, encryptedOut)
                                    val tempByte = encryptedOut[0] and (0x80.toByte())
                                    for(i in 0 .. (encryptedOut.size - 2)){
                                        encryptedOut[i] = if((encryptedOut[i+1] and 0x80.toByte()) == 0x80.toByte()) {
                                            ((encryptedOut[i].toInt() shl 1) or 1).toByte()
                                        }else{
                                            ((encryptedOut[i].toInt() shl 1) or 0).toByte()
                                        }
                                    }

                                    encryptedOut[encryptedOut.size - 1] = (encryptedOut[encryptedOut.size - 1].toInt() shl 1).toByte()

                                    if(tempByte == (0x80.toByte())){
                                        for (i in encryptedOut.indices){
                                            val Rb = CommonConvert.ascStringToBCD("00000000000000000000000000000087")
                                            encryptedOut[i] = encryptedOut[i] xor Rb[i]
                                        }
                                    }

                                    PedApi.calAes_Api(Constants.ZCMK_INDEX, 1, CommonConvert.ascStringToBCD("00000000000000000000000000000000"), 16, encryptedOut, 16, encryptedOut)
                                }

                                Constants.ZCMKType.KEY_TDES.ordinal -> {
                                    PedApi.PEDDes_Api(Constants.ZCMK_INDEX, 0x03, 0x01, CommonConvert.ascStringToBCD("0000000000000000"), 8, encryptedOut)
                                }
                                else -> -1
                            }

                            if (enRet != 0) {
                                Utils.showToast(applicationContext, resources.getString(R.string.err_generate_zcmk))
                                currentComponent = 1
                                etComponent!!.hint = resources.getString(R.string.hint_input_component_1)
                                compMap.clear()
                                PedApi.PedErase(1, Constants.ZCMK_INDEX)

                                if (zcmkType == Constants.ZCMKType.KEY_AES256.ordinal) {
                                    etComponent!!.filters = arrayOf<InputFilter>(LengthFilter(64))
                                } else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal) {
                                    etComponent!!.filters = arrayOf<InputFilter>(LengthFilter(32))
                                }

                                return
                            }

                            val kcvStr = CommonConvert.bcdToASCString(encryptedOut.copyOfRange(0, 3))

                            if (kcvStr == kcv) {
                                Utils.showToast(applicationContext, resources.getString(R.string.generate_zcmk_success))

                                DataBaseOpenHelper.clear(DataBaseOpenHelper.TABLE_ZCMK_INFO)

                                val contentValues = ContentValues()
                                contentValues.put(DataBaseOpenHelper.KEY_KEY_TYPE, zcmkType) // TDES or AES256
                                contentValues.put(DataBaseOpenHelper.KEY_KCV, kcv)
                                contentValues.put(DataBaseOpenHelper.KEY_ZCMK_INDEX, Constants.ZCMK_INDEX)
                                DataBaseOpenHelper.insert(DataBaseOpenHelper.TABLE_ZCMK_INFO, contentValues)

                                zcmkInfo = ZCMKInfo(zcmkType, kcv, Constants.ZCMK_INDEX)
                                PrintUtil.printZCMK(zcmkInfo!!, handler!!)
                            } else {
                                Utils.showToast(applicationContext, resources.getString(R.string.err_kcv))
                                PedApi.PedErase_Api(1, Constants.ZCMK_INDEX)
                                //PedApi.PedErase(1, Constants.ZCMK_INDEX)
                            }
                        }

                        currentComponent = 1
                        etComponent!!.setText("")
                        etComponent!!.hint = resources.getString(R.string.hint_input_component_1)
                        compMap.clear()

                        if (zcmkType == Constants.ZCMKType.KEY_AES256.ordinal) {
                            etComponent!!.filters = arrayOf<InputFilter>(LengthFilter(64))
                        } else if (zcmkType == Constants.ZCMKType.KEY_TDES.ordinal) {
                            etComponent!!.filters = arrayOf<InputFilter>(LengthFilter(32))
                        }
                    }
                }
            }
        }
    }


    private fun generateZCMK(zcmk: ByteArray?): Int {
        if (compMap.size < 2 || zcmk == null) {
            return -1
        }

        val comp1Str = compMap[Constants.MAP_ZCMK_COMPONENT_1]
        val comp2Str = compMap[Constants.MAP_ZCMK_COMPONENT_2]

        val comp1 = CommonConvert.ascStringToBCD(comp1Str)
        val comp2 = CommonConvert.ascStringToBCD(comp2Str)

        for (i in comp1.indices) {
            zcmk[i] = comp1[i] xor comp2[i]
        }

        return 0
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
                    when (arg) {
                        2, 3 -> {
                            msgConfirmDialog.setConfirmText("Confirm")
                                .setButtonVisible(true)
                                .setTvInfo(msg.obj.toString())
                                .setOnConfirmClickListener(object : MsgConfirmDialog.OnConfirmClickListener {
                                    override fun onClick() {
                                        if (zcmkInfo != null && handler != null) {
                                            PrintUtil.printZCMK(zcmkInfo!!, handler!!)
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

    override fun onBackPressed() {
        compMap.clear()
        super.onBackPressed()
    }
}