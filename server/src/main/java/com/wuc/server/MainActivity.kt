package com.wuc.server

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cc.lison.pojo.EchoFile
import cc.lison.pojo.EchoMessage
import com.wuc.server.adapter.LogAdapter
import com.wuc.server.bean.ClientChanel
import com.wuc.server.bean.LogBean
import com.wuc.server.nettyserver.NettyServer
import com.wuc.server.nettyserver.NettyServerListener
import com.wuc.server.nsd.NSDServer
import com.wuc.server.utils.IpGetUtils
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.util.CharsetUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.net.InetAddress


/**
 * 无论两端哪边先结束应用再打开 Netty 都可以 可实现断线重连
 */
class MainActivity : AppCompatActivity(), View.OnClickListener, NettyServerListener {

    private val TAG = "MainActivityServer"

    private var mSendLogAdapter = LogAdapter()
    private var mReceLogAdapter = LogAdapter()

    /**
     * 注册 NSD 服务的名称 和 端口 这个可以设置默认固定址，用于客户端通过 NSD_SERVER_NAME 筛选得到服务端地址和端口
     */
    var NSD_SERVER_NAME = "AGSystem"

    var NSD_PORT = 8088

    var clientChanelArray: MutableList<ClientChanel> = mutableListOf() //储存客户端通道信息

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread(Runnable {
            val localHost = InetAddress.getLocalHost()
            //获得本机IP
            val ip = localHost.hostAddress
            //获得本机名称
            val name = localHost.hostName
            //Log.d(TAG, "本机IP： $ip" + "本机名称：$name")
            Log.i(TAG, "IP： ${IpGetUtils.getLocalIpAddress(this)}")
        }).start()
        startServer.setOnClickListener(this)
        clear_log.setOnClickListener(this)
        send_btn.setOnClickListener(this)
        image_btn.setOnClickListener(this)
        initData()
        registerNsdServer()
    }


    private fun initData() {
        send_list.layoutManager = LinearLayoutManager(this)
        send_list.adapter = mSendLogAdapter

        rece_list.layoutManager = LinearLayoutManager(this)
        rece_list.adapter = mReceLogAdapter

//        val textToBitmap = ImageTextUtils.drawTextToBitmap(this, R.mipmap.wudang, "图片和文字结合图片和文字结合图片和文字结合图片和文字结合图片和文字结合")
//        image_selected.setImageBitmap(textToBitmap)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.startServer -> {
                startServer()
                //registerNsdServer()
            }
            R.id.send_btn -> {
                if (!NettyServer.instance.isServerStart()) {
                    toast("未连接,请先连接")
                } else {
                    val msg = send_et.text.toString().trim()
                    if (TextUtils.isEmpty(msg)) {
                        return
                    }
                    val msgMessage = EchoMessage()
                    val byteArray = msg.toByteArray()
                    msgMessage.bytes = byteArray
                    NettyServer.instance.sendMsgToServer(msgMessage, ChannelFutureListener {
                        if (it.isSuccess) {
                            Log.d(TAG, "Write auth successful")
                            val s = String(msgMessage.bytes, CharsetUtil.UTF_8)
                            logSend(s)
                        } else {
                            Log.d(TAG, "Write auth error")
                        }
                    })
                }
            }
            R.id.clear_log -> {
                mReceLogAdapter.getDataList().clear()
                mSendLogAdapter.getDataList().clear()
                mReceLogAdapter.notifyDataSetChanged()
                mSendLogAdapter.notifyDataSetChanged()
            }
            R.id.image_btn -> {
            }
        }
    }

    private fun logSend(log: String) {
        val logBean = LogBean(System.currentTimeMillis(), log)
        mSendLogAdapter.getDataList().add(0, logBean)
        runOnUiThread {
            mSendLogAdapter.notifyDataSetChanged()
        }
    }

    private fun startServer() {
        if (!NettyServer.instance.isServerStart()) {
            NettyServer.instance.setListener(this@MainActivity)
            NettyServer.instance.start()
        } else {
            NettyServer.instance.disconnect()
        }
    }

    override fun onMessageResponseServer(msg: Any?, channelId: String?) {
        Log.e(TAG, "onMessageResponseServer：ChannelId:$channelId   msg：$msg")
        if (msg is EchoFile) {
            val bytes = msg.bytes
            val file_name = msg.file_name
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
            runOnUiThread {
                image_selected.setImageBitmap(bitmap)
            }
            //Log.d(TAG, "bytes : $bytes" + "file_name：$file_name")
        } else if (msg is EchoMessage) {
            val str = String(msg.bytes, CharsetUtil.UTF_8)
            logRece(str)
            //msg?.let { logRece(it) }
        }

    }

    private fun logRece(log: String) {
        val logBean = LogBean(System.currentTimeMillis(), log)
        mReceLogAdapter.getDataList().add(0, logBean)
        runOnUiThread {
            mReceLogAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onChannelConnect(channel: Channel) {
        val socketStr = channel.remoteAddress().toString()
        val clientChanel = ClientChanel(socketStr, channel, channel.id().asShortText())
        synchronized(clientChanelArray) {
            clientChanelArray.add(clientChanel)
            Log.i(TAG, clientChanel.clientIp + " 建立连接")
        }
        NettyServer.instance.setChannel(channel)
        runOnUiThread {
            receiveTv.text = "接收($channel)"
        }
    }

    override fun onChannelDisConnect(channel: Channel) {
        Log.e(TAG, "onChannelDisConnect:ChannelId" + channel.id().asShortText())
        for (i in 0 until clientChanelArray.size) {
            val clientChanel = clientChanelArray[i]
            if (clientChanel.shortId == channel.id().asShortText()) {
                synchronized(clientChanelArray) {
                    clientChanelArray.remove(clientChanel)
                    runOnUiThread {
                        Log.e(TAG, "disconncect " + clientChanel.clientIp + " 断开连接")
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onStartServer() {
        Log.e(TAG, "onStartServer")
        runOnUiThread {
            startServer.text = "stopServer"
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onStopServer() {
        Log.e(TAG, "onStopServer")
        runOnUiThread {
            startServer.text = "startServer"
        }
    }

    override fun onServiceStatusConnectChanged(statusCode: Int) {
        runOnUiThread {
            if (statusCode == NettyServerListener.STATUS_CONNECT_SUCCESS) {
                Log.e(TAG, "STATUS_CONNECT_SUCCESS:")
            } else {
                Log.e(TAG, "onServiceStatusConnectChanged:$statusCode")
                receiveTv.text = "接收"
            }
        }
    }

    /**
     * 服务器端注册一个可供NSD探测到的网络 Ip 地址，便于给展示叫号机连接此socket
     */
    private var nsdServerRunnable: Runnable = Runnable {
        val nsdServer = NSDServer()
        nsdServer.startNSDServer(this@MainActivity, NSD_SERVER_NAME, NSD_PORT)

        nsdServer.setRegisterState(object : NSDServer.IRegisterState {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                Log.i(TAG, "已注册服务onServiceRegistered: $serviceInfo")
                //已经注册可停止该服务
                //sdServer.stopNSDServer();
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.i(TAG, "已注册服务onRegistrationFailed: $serviceInfo")
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                Log.i(TAG, "已注册服务onServiceUnregistered: $serviceInfo")
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.i(TAG, "已注册服务onUnregistrationFailed: $serviceInfo")
            }
        })
    }

    private fun registerNsdServer() {
        Thread(nsdServerRunnable).start()
    }
}
