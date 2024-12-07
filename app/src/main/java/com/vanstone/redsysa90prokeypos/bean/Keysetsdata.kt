package com.vanstone.redsysa90prokeypos.bean

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class Info(
    val version: String,
    val customer: String,
    val package_name: String,
    val datetime: String,
    val model: String,
    val sn_range: String? // Use nullable type for 'null' value
)

@Serializable
data class Key(
    val index: Int,
    val crypto: String,
    val usage: String,
    val mode: String,
    val kcv: String,
    val ksnlen: Int,
    val ksn: String,
    val length: Int,
    val value: String,
    val iv: String,
    val keyBlock: Boolean
)

@Serializable
data class Keyset(
    val name: String,
    val key: List<Key>
)

@Serializable
data class KeysetsData(
    val info: Info,
    val keysets: List<Keyset>
)

