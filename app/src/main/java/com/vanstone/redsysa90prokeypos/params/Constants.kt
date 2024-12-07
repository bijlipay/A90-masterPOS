package com.vanstone.redsysa90prokeypos.params

class Constants{
    companion object{

        const val MAP_ZCMK_COMPONENT_1 = "ZCMK_COMPONENT_1"
        const val MAP_ZCMK_COMPONENT_2 = "ZCMK_COMPONENT_2"
        const val MAP_ZCMK_COMPONENT_3 = "ZCMK_COMPONENT_3"

        const val EXTRA_LOAD_ZCMK = "KEY_ZCMK_TYPE"
        const val EXTRA_LOAD_OTHER = "KEY_OTHER_TYPE"

        const val KEY = "8A5C2D9F6E2B3E5C5C3E2B6E9F2D5C8A"
        const val DEFAULT_PWD = "000000"
        const val SP_KEY_ADMIN_PWD = "admin_password"
        const val SP_KEY_OPER_PWD = "oper_password"
        const val ADMIN_USER = "99"
        const val OPER_USER = "01"

        const val LOG_TAG = "A90PRO_KEYPOS"
        const val LOG_ENABLE = false

        const val FILE_TDES = "claves-versiones.txt"
        const val FILE_AES256 = "claves-versiones.txt"

        const val MANUFACTURE_CODE = "94"
        const val timeout = 120

        const val KEY_ZCMK_TDES = "TDES KEY"
        const val KEY_ZCMK_AES256 = "AES256 KEY"
        const val KEY_TYPE_TR31 = "TR31 block"
        const val KEY_TYPE_CIPHERTEXT_TDES = "Ciphertext TDES"

        const val TRANSFER_TYPE_TDES = "Transfer TDES"
        const val TRANSFER_TYPE_TR31 = "Transfer TR31"

        const val LOAD_METHOD_TR31 = "Decrypt TR31"
        const val LOAD_METHOD_COMPONENTS = "Input Components"

        const val ZCMK_INDEX = 1
        const val CMI_INDEX = 2
        const val CI_INDEX = 2
        const val CMA_INDEX = 3
        const val CA_INDEX = 3
        const val CI_VER_INDEX = 20
        const val CTK_VER_INDEX = 21
        const val TEMP_KEY_INDEX = 99

        const val KEY_TYPE_CI = 10
        const val KEY_TYPE_CA = 11
        const val KEY_TYPE_CTK = 12
        const val KEY_TYPE_CI_VER = 13
        const val KEY_TYPE_CTK_VER = 14


        const val STATUS_SUCCESS = 0

        const val START_RECEIVE = 1
        const val START_VERIFY = 2
        const val START_SEND_DATA = 3
        const val START_PARSE_DATA = 4
        const val PROGRESS = 5
        const val STATUS_PRINT = 6
        const val PRINT_FINISH = 7
        const val WRITING_KEY = 8

        const val ERR_KEYFILE_NOT_EXISTS = -1
        const val ERR_KEYPATH = -2
        const val ERR_NO_KEY_MATCH = -3
        const val ERR_PORT_OPEN_FAIL = -4
        const val ERR_INIT_DEVICE = -5
        const val ERR_CMI_QUANTITY = -6
        const val ERR_CMA_QUANTITY = -7
        const val ERR_CMTK_QUANTITY = -8
        const val ERR_KEY_VERSION_QUANTITY = -9
        const val ERR_TIMEOUT = -10
        const val ERR_DATA_ERROR = -11
        const val ERR_GEN_CI = -12
        const val ERR_GEN_CA = -13
        const val ERR_GEN_CTK = -14
        const val ERR_GET_SN = -15
        const val ERR_SEND_DATA = -16
        const val ERR_DEL_KEY_SUCCESS = -17
        const val ERR_DEL_KEY_FAIL = -18
        const val ERR_CI_VER = -19
        const val ERR_CTK_VER = -20
        const val ERR_INIT_PORT = -21
        const val ERR_WRITE_RANDOM_KEY = -22
        const val ERR_GEN_TR31 = -23

    }

    enum class ZCMKType{
        KEY_AES256,
        KEY_TDES
    }

    enum class KeyBlockType{
        CMI,
        CMA,
        CMTK
    }
}