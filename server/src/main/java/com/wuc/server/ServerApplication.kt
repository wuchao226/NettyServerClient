package com.wuc.server

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import kotlin.properties.Delegates

/**
 * @author:     wuchao
 * @date:       2019-07-09 22:19
 * @desciption:
 */
class ServerApplication : Application() {
    /**
     * 全局伴生对象
     */
    companion object {
        var context: ServerApplication by Delegates.notNull()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}