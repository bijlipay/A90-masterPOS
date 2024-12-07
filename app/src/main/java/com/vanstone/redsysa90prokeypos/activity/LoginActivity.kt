package com.vanstone.redsysa90prokeypos.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vanstone.appsdk.client.ISdkStatue
import com.vanstone.redsysa90prokeypos.R
import com.vanstone.redsysa90prokeypos.app.App
import com.vanstone.redsysa90prokeypos.params.Constants
import com.vanstone.redsysa90prokeypos.tools.EncryptUtil
import com.vanstone.redsysa90prokeypos.tools.SPUtil
import com.vanstone.redsysa90prokeypos.tools.Utils
import com.vanstone.trans.api.SystemApi
import com.vanstone.utils.CommonConvert

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var etUserName: EditText? = null
    private var etUserPWD: EditText? = null
    private var btLogin: Button? = null
    private var btChange: Button? = null

    private val PERMISSIONS_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etUserName = findViewById<EditText>(R.id.etUserName)
        etUserPWD = findViewById<EditText>(R.id.etUserPWD)
        btLogin = findViewById<Button>(R.id.btLogin)
        btChange = findViewById<Button>(R.id.btChange)

        btLogin!!.setOnClickListener(this)
        btChange!!.setOnClickListener(this)

        reqPermission()
        initSDK()

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btLogin -> {
                val userName = etUserName!!.text.toString().trim()
                val password = etUserPWD!!.text.toString().trim()

                if (userName.isEmpty() || (userName != Constants.ADMIN_USER && userName != Constants.OPER_USER)){
                    Toast.makeText(App.context, resources.getString(R.string.err_account_not_exists), Toast.LENGTH_SHORT).show()
                    etUserName!!.setText("")
                    etUserPWD!!.setText("")
                    return
                }

                if (password.isEmpty() || password.length < 6){
                    etUserPWD!!.setText("")
                    Toast.makeText(App.context, resources.getString(R.string.err_password), Toast.LENGTH_SHORT).show()
                    return
                }

                val spAdminPWD = SPUtil.getString(App.context, Constants.SP_KEY_ADMIN_PWD, "")
                val spOperPWD = SPUtil.getString(App.context, Constants.SP_KEY_OPER_PWD, "")
                if(userName == Constants.ADMIN_USER) {
                    if (spAdminPWD!!.isEmpty()) {
                        if (password != Constants.DEFAULT_PWD) { // default password
                            etUserPWD!!.setText("")
                            Toast.makeText(App.context, resources.getString(R.string.err_password), Toast.LENGTH_SHORT).show()
                            return
                        } else {
                            val enPwdStr = EncryptUtil.getEnPWD(Constants.DEFAULT_PWD)
                            SPUtil.putString(App.context, Constants.SP_KEY_ADMIN_PWD, enPwdStr)
                            startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                            finish()
                            return
                        }
                    }

                    val enPwdInput = EncryptUtil.getEnPWD(password)
                    if(enPwdInput != spAdminPWD){
                        etUserPWD!!.setText("")
                        Toast.makeText(App.context, resources.getString(R.string.err_password), Toast.LENGTH_SHORT).show()
                        return
                    }

                    startActivity(Intent(this@LoginActivity, AdminActivity::class.java))
                    finish()
                }else{
                    if (spOperPWD!!.isEmpty()) {
                        if (password != Constants.DEFAULT_PWD) { // default password
                            etUserPWD!!.setText("")
                            Toast.makeText(App.context, resources.getString(R.string.err_password), Toast.LENGTH_SHORT).show()
                            return
                        } else {
                            val enPwdStr = EncryptUtil.getEnPWD(Constants.DEFAULT_PWD)
                            SPUtil.putString(App.context, Constants.SP_KEY_OPER_PWD, enPwdStr)
                            startActivity(Intent(this@LoginActivity, OperatorActivity::class.java))
                            finish()
                            return
                        }
                    }

                    val enPwdInput = EncryptUtil.getEnPWD(password)
                    if(enPwdInput != spOperPWD){
                        etUserPWD!!.setText("")
                        Toast.makeText(App.context, resources.getString(R.string.err_password), Toast.LENGTH_SHORT).show()
                        return
                    }

                    startActivity(Intent(this@LoginActivity, OperatorActivity::class.java))
                    finish()
                }
            }

            R.id.btChange -> {
                startActivity(Intent(this@LoginActivity, ChangePwdActivity::class.java))
            }
        }
    }


    private fun initSDK() {
        val curAppDir = this.filesDir.absolutePath
        SystemApi.SystemInit_Api(0, CommonConvert.StringToBytes("$curAppDir/\u0000"), this, object : ISdkStatue {
            override fun sdkInitSuccessed() {
            }

            override fun sdkInitFailed() {
                Utils.showToast(applicationContext, resources.getString(R.string.err_init_sdk))
            }
        })
    }

    private fun reqPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, resources.getString(R.string.permission_required), Toast.LENGTH_SHORT).show()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST)
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, resources.getString(R.string.permission_granted), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, resources.getString(R.string.permission_grant_fail), Toast.LENGTH_SHORT).show()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}