package com.vanstone.redsysa90prokeypos.tools

import com.vanstone.redsysa90prokeypos.params.Constants
import com.vanstone.trans.api.MathsApi
import com.vanstone.utils.CommonConvert

object EncryptUtil {
    fun getEnPWD(pwd: String): String{
        if(pwd.isEmpty()){
            return ""
        }

        val enPWD = ByteArray(8)
        val bcdPwd = CommonConvert.ascStringToBCD(pwd, 8)
        MathsApi.Des3Calc_Api(bcdPwd, enPWD, CommonConvert.hexStringToByte(Constants.KEY), 1)
        return CommonConvert.bytes2HexString(enPWD)
    }
}