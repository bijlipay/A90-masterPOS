package com.vanstone.redsysa90prokeypos.tools

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.vanstone.redsysa90prokeypos.params.Constants

object Utils {
    @JvmStatic
    fun printLogs(log: String){
        if (Constants.LOG_ENABLE) {
            Log.d(Constants.LOG_TAG, log)
        }
    }

    @JvmStatic
    fun showToast(context: Context, info: String) {
        Toast.makeText(context, info, Toast.LENGTH_SHORT).show()
    }

}