package com.wuc.server.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log


/**
 * @author:     wuchao
 * @date:       2019-07-10 15:25
 * @desciption:
 */
class NSDServer {
    val TAG = "NSDServer"
    private var mNsdManager: NsdManager? = null
    private var mRegistrationListener: NsdManager.RegistrationListener? = null
    private var mServerName: String? = null


    private val mServerType = "_http._tcp."  // 服务器type，要客户端扫描服务器的一致
    //监听注册，并开始NSDServer注册
    fun startNSDServer(context: Context, serviceName: String, port: Int) {
        initializeRegistrationListener()
        registerService(context, serviceName, port)
    }
    //实例化注册监听器
    private fun initializeRegistrationListener() {
        mRegistrationListener = object : NsdManager.RegistrationListener {
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Log.i(TAG, "onUnregistrationFailed serviceInfo: $serviceInfo ,errorCode:$errorCode")
                serviceInfo?.let { registerState?.onUnregistrationFailed(it, errorCode) }
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo?) {
                Log.i(TAG, "onServiceUnregistered serviceInfo: $serviceInfo")
                serviceInfo?.let { registerState?.onServiceUnregistered(it) }
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
                Log.e(TAG, "NsdServiceInfo onRegistrationFailed")
                serviceInfo?.let { registerState?.onRegistrationFailed(it, errorCode) }
            }

            override fun onServiceRegistered(serviceInfo: NsdServiceInfo?) {
                mServerName = serviceInfo?.serviceName
                Log.i(TAG, "onServiceRegistered: " + serviceInfo.toString())
                serviceInfo?.let { registerState?.onServiceRegistered(it) }
            }

        }
    }
    //注册NSD服务器端
    private fun registerService(context: Context, serviceName: String, port: Int) {
        mNsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
        val serviceInfo = NsdServiceInfo()
        serviceInfo.serviceName = serviceName
        serviceInfo.port = port
        serviceInfo.serviceType = mServerType//客户端发现服务器是需要对应的这个Type字符串
        mNsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener)
    }
    //取消注册NSD服务器端
    fun stopNSDServer() {
        mNsdManager?.unregisterService(mRegistrationListener)
    }

    //NSD服务注册监听接口
    interface IRegisterState {
        //注册NSD成功
        fun onServiceRegistered(serviceInfo: NsdServiceInfo)

        //注册NSD失败
        fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int)

        //取消NSD注册成功
        fun onServiceUnregistered(serviceInfo: NsdServiceInfo)

        //取消NSD注册失败
        fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int)

    }

    //NSD服务接口对象
    private var registerState: IRegisterState? = null


    //设置NSD服务接口对象
    fun setRegisterState(registerState: IRegisterState) {
        this.registerState = registerState
    }
}