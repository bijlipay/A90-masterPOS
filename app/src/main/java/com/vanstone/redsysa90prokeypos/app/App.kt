package com.vanstone.redsysa90prokeypos.app

import android.app.Application
import android.content.Context
import cn.wch.ch34xuartdriver.CH34xUARTDriver
import com.vanstone.redsysa90prokeypos.tools.DataBaseOpenHelper
import java.util.ArrayList


class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this
        initSqlLite()
    }

    companion object {
        lateinit var context: Context
        lateinit var driver: CH34xUARTDriver
    }

    private fun initSqlLite() {
        val sqlList = ArrayList<String>()
        sqlList.add(DataBaseOpenHelper.SQL_CREATE_TABLE_ZCMK_INFO)
        sqlList.add(DataBaseOpenHelper.SQL_CREATE_TABLE_CMI_KEY_INFO)
        sqlList.add(DataBaseOpenHelper.SQL_CREATE_TABLE_CMA_KEY_INFO)
        sqlList.add(DataBaseOpenHelper.SQL_CREATE_TABLE_CMTK_KEY_INFO)
        sqlList.add(DataBaseOpenHelper.SQL_CREATE_TABLE_KEY_VER_INFO)
        sqlList.add(DataBaseOpenHelper.SQL_CREATE_TABLE_KEYIMPORT_INFO)
        sqlList.add(DataBaseOpenHelper.SQL_CREATE_TABLE_KEYEXPORT_INFO)
        DataBaseOpenHelper.getInstance(applicationContext, DataBaseOpenHelper.DB_NAME, 1, sqlList)
    }
}