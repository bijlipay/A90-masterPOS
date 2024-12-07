package com.vanstone.redsysa90prokeypos.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vanstone.redsysa90prokeypos.R
import com.vanstone.redsysa90prokeypos.app.App
import com.vanstone.redsysa90prokeypos.params.Constants
import com.vanstone.redsysa90prokeypos.tools.EncryptUtil
import com.vanstone.redsysa90prokeypos.tools.SPUtil

class ChangePwdActivity : AppCompatActivity(), View.OnClickListener {

    private var etName: EditText? = null
    private var etOldPWD : EditText? = null
    private var etNewPWD1 : EditText? = null
    private var etNewPWD2 : EditText? = null
    private var btConfirm : Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pwd)
        etName = findViewById(R.id.etName)
        etOldPWD = findViewById<EditText>(R.id.etOldPWD)
        etNewPWD1 = findViewById<EditText>(R.id.etNewPWD1)
        etNewPWD2 = findViewById<EditText>(R.id.etNewPWD2)
        btConfirm = findViewById<Button>(R.id.btConfirm)

        btConfirm!!.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v!!.id == R.id.btConfirm){
            val userName = etName!!.text.toString().trim()
            val oldPwd = etOldPWD!!.text.toString().trim()
            val newPWD1 = etNewPWD1!!.text.toString().trim()
            val newPWD2 = etNewPWD2!!.text.toString().trim()

            if (userName.isEmpty() || (userName != Constants.ADMIN_USER && userName != Constants.OPER_USER)){
                Toast.makeText(App.context, resources.getString(R.string.err_username), Toast.LENGTH_SHORT).show()
                return
            }

            if (newPWD1.isEmpty() || newPWD2.isEmpty()){
                Toast.makeText(App.context, resources.getString(R.string.err_empty_new_pwd), Toast.LENGTH_SHORT).show()
                return
            }

            if (newPWD1.length < 6 || newPWD2.length < 6){
                Toast.makeText(App.context, resources.getString(R.string.err_pwd_len), Toast.LENGTH_SHORT).show()
                return
            }

            if (newPWD1 != newPWD2){
                etNewPWD1!!.setText("")
                etNewPWD2!!.setText("")
                Toast.makeText(App.context, resources.getString(R.string.err_new_pwd_not_match), Toast.LENGTH_SHORT).show()
                return
            }

            val spAdminPWD = SPUtil.getString(App.context, Constants.SP_KEY_ADMIN_PWD, "")
            val spOperPWD = SPUtil.getString(App.context, Constants.SP_KEY_OPER_PWD, "")

            if (userName == Constants.ADMIN_USER) {
                if (spAdminPWD!!.isEmpty()) {
                    if (oldPwd != Constants.DEFAULT_PWD) { // default password
                        Toast.makeText(App.context, resources.getString(R.string.err_password), Toast.LENGTH_SHORT).show()
                        return
                    }
                } else {
                    val enOldPWD = EncryptUtil.getEnPWD(oldPwd)
                    if (enOldPWD != spAdminPWD) {
                        etOldPWD!!.setText("")
                        Toast.makeText(App.context, resources.getString(R.string.err_password), Toast.LENGTH_SHORT).show()
                        return
                    }
                }

                val enNewPWD = EncryptUtil.getEnPWD(newPWD1)
                SPUtil.putString(App.context, Constants.SP_KEY_ADMIN_PWD, enNewPWD)
            }else {
                if (spOperPWD!!.isEmpty()) {
                    if (oldPwd != Constants.DEFAULT_PWD) { // default password
                        Toast.makeText(App.context, resources.getString(R.string.err_password), Toast.LENGTH_SHORT).show()
                        return
                    }
                } else {
                    val enOldPWD = EncryptUtil.getEnPWD(oldPwd)
                    if (enOldPWD != spOperPWD) {
                        etOldPWD!!.setText("")
                        Toast.makeText(App.context, resources.getString(R.string.err_password), Toast.LENGTH_SHORT).show()
                        return
                    }
                }

                val enNewPWD = EncryptUtil.getEnPWD(newPWD1)
                SPUtil.putString(App.context, Constants.SP_KEY_OPER_PWD, enNewPWD)
            }

            Toast.makeText(App.context, resources.getString(R.string.change_pwd_success), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}