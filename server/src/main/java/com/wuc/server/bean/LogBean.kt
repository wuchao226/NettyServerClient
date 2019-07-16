package com.wuc.server.bean

import android.annotation.SuppressLint
import java.text.SimpleDateFormat

/**
 * @author:     wuchao
 * @date:       2019-07-09 23:15
 * @desciption:
 */
class LogBean(var time: Long, var log: String) {

    @SuppressLint("SimpleDateFormat")
    fun getTime():String{
        val format = SimpleDateFormat("HH:mm:ss")
        return format.format(time)
    }


//    @SuppressLint("SimpleDateFormat")
//    fun LogBean(time: Long, log: String) {
//        val format = SimpleDateFormat("HH:mm:ss")
//        mTime = format.format(time)
//        mLog = log
//    }
}