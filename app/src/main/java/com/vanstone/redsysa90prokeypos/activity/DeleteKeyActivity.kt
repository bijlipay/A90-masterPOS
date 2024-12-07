package com.vanstone.redsysa90prokeypos.activity

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.vanstone.msgdialog.ConfirmDialog
import com.vanstone.redsysa90prokeypos.R
import com.vanstone.redsysa90prokeypos.tools.DataBaseOpenHelper
import com.vanstone.redsysa90prokeypos.tools.Utils
import com.vanstone.trans.api.PedApi

class DeleteKeyActivity : AppCompatActivity() {
    private var etKeyIndex: EditText? = null
    private var btnDel: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_key)
        etKeyIndex = findViewById(R.id.etkeyIndex)
        btnDel = findViewById(R.id.btnDel)

        btnDel!!.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                val strIndex = etKeyIndex!!.text.toString()

                if (strIndex.isNullOrEmpty()) {
                    Utils.showToast(applicationContext, resources.getString(R.string.input_cmtk_index))
                    return
                }

                val keyIndex = strIndex.toInt()

                if (keyIndex < 4 || keyIndex > 13){
                    Utils.showToast(applicationContext, resources.getString(R.string.err_key_index))
                    return
                }


                val dialog = ConfirmDialog()
                dialog.setCancelText("NO")
                    .setConfirmText("YES")
                    .setTvInfo("Delete the key?")
                    .setOnBtnClickListener(object : ConfirmDialog.OnBtnClickListener{
                    override fun onConfirmClick() {
                        val result = PedApi.PedErase(1, keyIndex)
                        if (result) {
                            DataBaseOpenHelper.delete(DataBaseOpenHelper.TABLE_CMTK_KEY_INFO, " ${DataBaseOpenHelper.KEY_CMTK_INDEX}=?", arrayOf("$keyIndex"))
                            Utils.showToast(applicationContext, resources.getString(R.string.delete_key_success))
                        } else {
                            Utils.showToast(applicationContext, resources.getString(R.string.delete_key_fail))
                        }
                    }

                    override fun onCancelClick() {
                        dialog.dismiss()
                    }

                })

                dialog.show(supportFragmentManager, "DelCMTK")

                etKeyIndex!!.setText("")
            }
        })
    }
}