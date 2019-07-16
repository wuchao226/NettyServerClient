package com.wuc.client

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

/**
 * @author:     wuchao
 * @date:       2019-07-09 22:17
 * @desciption:
 */
class ClientApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
    }
}