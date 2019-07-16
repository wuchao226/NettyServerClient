package com.wuc.server.utils

import android.content.Context
import android.net.wifi.WifiManager





/**
 * @author:     wuchao
 * @date:       2019-07-15 10:23
 * @desciption: 获取本机ip地址
 */
object IpGetUtils {


    /**
     * 获取当前ip地址
     *
     * @param context
     * @return
     */
    fun getLocalIpAddress(context: Context): String {
        return try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val i = wifiInfo.ipAddress
            int2ip(i)
        } catch (ex: Exception) {
            " 获取IP出错鸟!!!!请保证是WIFI,或者请重新打开网络!\n" + ex.message
        }

        // return null;
    }

    /**
     * 将ip的整数形式转换成ip形式
     *
     * @param ipInt
     * @return
     */
    fun int2ip(ipInt: Int): String {
        val sb = StringBuilder()
        sb.append(ipInt and 0xFF).append(".")
        sb.append(ipInt shr 8 and 0xFF).append(".")
        sb.append(ipInt shr 16 and 0xFF).append(".")
        sb.append(ipInt shr 24 and 0xFF)
        return sb.toString()
    }

}