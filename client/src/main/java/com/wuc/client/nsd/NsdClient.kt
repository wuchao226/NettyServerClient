package com.wuc.client.nsd

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log


/**
 * @author:     wuchao
 * @date:       2019-07-10 16:26
 * @desciption:
 */
class NsdClient(var mContext: Context, var mServiceName: String, var mIServerFound: IServerFound) {

    val TAG = "NsdClient"

    /**
     * NSD_SERVICE_NAME和NSD_SERVER_TYPE需要与服务器端完全一致
     */
    private val NSD_SERVER_TYPE = "_http._tcp."
    /**
     * 搜寻监听器
     */
    private var mDiscoveryListener: NsdManager.DiscoveryListener? = null
    /**
     *  解析监听器
     */
    private var mResolverListener: NsdManager.ResolveListener? = null
    private var mNsdManager: NsdManager? = null

    /**
     * 用来存储解析后的网络对象列表，包含完整数据
     */
    private var mNsdServiceInfoList: MutableList<NsdServiceInfo> = mutableListOf()

    /**
     * 未解析前搜索到的
     */
    private var mNsdServiceInfoListBefore: MutableList<NsdServiceInfo> = mutableListOf()


    private val MSG_RESOLVER = 1002

    private val MSG_NULL = 1003

    fun startNSDClient() {
        mNsdManager = mContext.getSystemService(Context.NSD_SERVICE) as NsdManager
        //注册NSD服务网络的监听，发现NSD网络后会在对应的方法回调
        initializeDiscoveryListener()
       //注册解析NSD网络的监听 ,解析NSD数据后回调
        initializeResolveListener()
        discoveryNSDServer()
    }
    //发现周边的NSD相关网络
    private fun discoveryNSDServer() {
        //三个参数
        //第一个参数要和NSD服务器端定的ServerType一样，
        //第二个参数是固定的
        //第三个参数是扫描监听器
        mNsdManager?.discoverServices(NSD_SERVER_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener)

    }
    //对得到的NDSServiceInfo进行解析
    private fun resoleServer(serviceInfo: NsdServiceInfo){
        //第一个参数是扫描得到的对象，第二个参数是解析监听对象
        mNsdManager?.resolveService(serviceInfo, mResolverListener);
    }

    /**
     * 扫描未被解析前的 NsdServiceInfo
     * 用于服务发现的回调调用接口
     */
    private fun initializeDiscoveryListener() {
        mDiscoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.i(TAG, "onStartDiscoveryFailed--> $serviceType:$errorCode")
                mNsdManager?.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.i(TAG, "onStopDiscoveryFailed--> $serviceType:$errorCode")
                mNsdManager?.stopServiceDiscovery(this)
            }

            override fun onDiscoveryStarted(serviceType: String) {
                Log.i(TAG, "onDiscoveryStarted--> $serviceType")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "onDiscoveryStopped--> $serviceType")
            }

            /**
             * serviceInfo里面只有NSD服务器的主机名，要解析后才能得到该主机名的其他数据信息
             */
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.i(TAG, "onServiceFound Info: --> $serviceInfo")
                //根据咱服务器的定义名称，指定解析该 NsdServiceInfo
                if (serviceInfo.serviceName == mServiceName) {
                    resoleServer(serviceInfo)
                    //mNsdManager?.resolveService(serviceInfo, mResolverListener)
                } else {
                    mHandler.sendEmptyMessage(MSG_NULL)
                }

                mNsdServiceInfoListBefore.add(serviceInfo)

            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.i(TAG, "onServiceLost--> $serviceInfo")
            }
        }
    }

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_RESOLVER -> {
                    //回调到主线 进行解析結果的回調
                    val serviceInfo = msg.obj as NsdServiceInfo
                    mIServerFound.onServerFound(serviceInfo, serviceInfo.port)
                    Log.e(TAG, " 指定onServiceFound（$mServiceName)： Service Info: --> $serviceInfo")
                }
                MSG_NULL -> mIServerFound.onServerFail()
            }
        }
    }

    /**
     * 解析未 调用未被解析的 NsdServiceInfo
     */
    private fun initializeResolveListener() {
        mResolverListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                val port = serviceInfo.port
                val host = serviceInfo.host
                val serviceName = serviceInfo.serviceName
                val hostAddress = serviceInfo.host.hostAddress
                Log.i(TAG, "onResolveFailed 已解析: host:$hostAddress:$port ----- serviceName: $serviceName")
            }

            /**
             * 这里就是我们要的最终数据信息
             */
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val port = serviceInfo.port
                val host = serviceInfo.host
                val serviceName = serviceInfo.serviceName
                val hostAddress = serviceInfo.host.hostAddress
                Log.i(TAG, "onServiceResolved 已解析: host:$hostAddress:$port ----- serviceName: $serviceName")

                mNsdServiceInfoList.add(serviceInfo)

                //解析的结果 通过Handler发送到主线程
                val msg = Message.obtain()
                msg.what = MSG_RESOLVER
                msg.obj = serviceInfo
                mHandler.sendMessageDelayed(msg, 500)
            }
        }
    }

    fun stopNSDServer() {
        mNsdManager?.stopServiceDiscovery(mDiscoveryListener)
    }

    interface IServerFound {

        //                void onServerFound(InetAddress host, int port);

        /**
         * 回调 指定解析的结果
         */
        fun onServerFound(serviceInfo: NsdServiceInfo, port: Int)

        //        void onServerFoundList(ArrayList<NsdServiceInfo> NsdServiceInfoList);

        /**
         * 无合适 回调失败
         */
        fun onServerFail()
    }
}