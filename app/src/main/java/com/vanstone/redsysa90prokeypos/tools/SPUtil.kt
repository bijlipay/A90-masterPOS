package com.vanstone.redsysa90prokeypos.tools

import android.content.Context
import android.content.SharedPreferences

/**
 * SharedPreferences:共享偏好，用来做数据存储，通过xml，存放标记性数据和设置信息
 */
object SPUtil {
    //文件名称为config
    private const val PREFERENCE_NAME = "config"

    private var sharedPreferences: SharedPreferences? = null

    /**
     * 写入Boolean变量至sharedPreferences中
     *
     * @param context 上下文环境
     * @param key     存储节点名称
     * @param value   存储节点的值
     */
    fun putBoolean(context: Context, key: String?, value: Boolean) {
        //(存储节点文件名称，读写方式)
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }
        sharedPreferences!!.edit().putBoolean(key, value).commit()
    }

    /**
     * 读取boolean标识从sharedPreferences中
     *
     * @param context 上下文环境
     * @param key     存储节点名称
     * @param value   没有此节点的默认值
     * @return 默认值或者此节点读取到的结果
     */
    fun getBoolean(context: Context, key: String?, value: Boolean): Boolean {
        //(存储节点文件名称,读写方式)
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }
        return sharedPreferences!!.getBoolean(key, value)
    }

    /**
     * 写入String变量至sharedPreferences中
     *
     * @param context 上下文环境
     * @param key     存储节点名称
     * @param value   存储节点的值String
     */
    fun putString(context: Context, key: String?, value: String?) {
        //存储节点文件的名称，读写方式
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }
        sharedPreferences!!.edit().putString(key, value).commit()
    }

    /**
     * 读取String标识从sharedPreferences中
     *
     * @param context  上下文环境
     * @param key      存储节点名称
     * @param defValue 没有此节点的默认值
     * @return 返回默认值或者此节点读取到的结果
     */
    fun getString(context: Context, key: String?, defValue: String?): String? {
        //存储节点文件的名称，读写方式
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }
        return sharedPreferences!!.getString(key, defValue)
    }

    /**
     * 写入int变量至sharedPreferences中
     *
     * @param context 上下文环境
     * @param key     存储节点名称
     * @param value   存储节点的值String
     */
    fun putInt(context: Context, key: String?, value: Int) {
        //存储节点文件的名称，读写方式
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }
        sharedPreferences!!.edit().putInt(key, value).commit()
    }

    /**
     * 读取int标识从sharedPreferences中
     *
     * @param context  上下文环境
     * @param key      存储节点名称
     * @param defValue 没有此节点的默认值
     * @return 返回默认值或者此节点读取到的结果
     */
    fun getInt(context: Context, key: String?, defValue: Int): Int {
        //存储节点文件的名称，读写方式
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }
        return sharedPreferences!!.getInt(key, defValue)
    }

    /**
     * 从sharedPreferences中移除指定节点
     *
     * @param context 上下文环境
     * @param key     需要移除节点的名称
     */
    fun remove(context: Context, key: String?) {
        //存储节点文件的名称，读写方式
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        }
        sharedPreferences!!.edit().remove(key).commit()
    }
}