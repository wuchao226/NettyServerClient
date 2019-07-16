package com.wuc.client

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cc.lison.pojo.EchoFile
import cc.lison.pojo.EchoMessage
import com.wuc.client.adapter.LogAdapter
import com.wuc.client.bean.LogBean
import com.wuc.client.nettyclient.NettyClient
import com.wuc.client.nettyclient.NettyClientListener
import com.wuc.client.nsd.NsdClient
import com.wuc.client.utils.BitmapUtils
import io.netty.channel.ChannelFutureListener
import io.netty.util.CharsetUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener, NettyClientListener {

    private val TAG = "MainActivityClient"

    val SERVER_NAME = "AGSystem"
    private var mSendLogAdapter = LogAdapter()
    private var mReceLogAdapter = LogAdapter()
    /**
     * Netty 客户端连接处理
     */
    private var mNettyClient: NettyClient? = null
    /**
     * Nsd 客户端搜索
     */
    private var mNsdClient: NsdClient? = null
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        connect.setOnClickListener(this)
        send_btn.setOnClickListener(this)
        clear_log.setOnClickListener(this)
        image_btn.setOnClickListener(this)
        disconnect.setOnClickListener(this)
        send_image_btn.setOnClickListener(this)
        initData()
        mNettyClient = NettyClient(Const.HOST, Const.TCP_PORT, 0)
        //searchNsdServer()
    }

    /**
     * 通過 Nsd 搜索註冊過的服務端名称 解析后拿到 IP 和端口 ，進行 NettySocket 的連接
     */
    private fun searchNsdServer() {
        mNsdClient = NsdClient(this@MainActivity, SERVER_NAME, object : NsdClient.IServerFound {
            override fun onServerFound(serviceInfo: NsdServiceInfo, port: Int) {
                val hostAddress = serviceInfo.host.hostAddress

                //tvConnect.setText("NSD查询到指定服务器信息：\n$info")
                Log.d(TAG, "NSD查询到指定服务器信息：\n$serviceInfo")
                Log.d(TAG, "NSD查询到指定服务器信息hostAddress：\n$hostAddress")

                //获取到指定的地址，进行Netty的连接
                connectNettyServer(hostAddress, port)

                if (serviceInfo.serviceName == SERVER_NAME) {
                    //扫面到对应后停止nsd扫描
                    mNsdClient?.stopNSDServer()
                }
            }

            override fun onServerFail() {
                Log.d(TAG, "NSD查询到指定服务器信息：失败")
            }
        })

        mNsdClient?.startNSDClient()
    }

    /**
     * 连接Netty 服务端
     *
     * @param host 服务端地址
     * @param port 服务端端口 默认两端约定一致
     */
    private fun connectNettyServer(host: String, port: Int) {

        mNettyClient = NettyClient(host, port, 0)

        Log.i(TAG, "connectNettyServer")
        if (!mNettyClient!!.getConnectStatus()) {
            mNettyClient?.setListener(object : NettyClientListener {
                override fun onMessageResponseClient(msg: Any, index: Int) {
                    Log.i(TAG, "onMessageResponse:$msg")
                    /**
                     * 接收服务端发送过来的 json数据解析
                     */
                    // TODO: 2018/6/1  do something
                    // QueueShowBean    queueShowBean = JSONObject.parseObject((String) msg, QueueShowBean.class);

                    //需要在主线程中刷新
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, msg.toString() + "", Toast.LENGTH_SHORT).show()

                        //tvNetty.setText("Client received:$msg")
                    }


                }

                override fun onClientStatusConnectChanged(statusCode: Int, index: Int) {
                    /**
                     * 回调执行还在子线程中
                     */
                    runOnUiThread {
                        if (statusCode == NettyClientListener.STATUS_CONNECT_SUCCESS) {
                            Log.e(TAG, "STATUS_CONNECT_SUCCESS:")
                            //vNettyStatus.setSelected(true)
                        } else {
                            Log.e(TAG, "onServiceStatusConnectChanged:$statusCode")
                            //vNettyStatus.setSelected(false)
                        }
                    }
                }
            })

            mNettyClient?.connect()//连接服务器
        }
    }


    private fun initData() {
        send_list.layoutManager = LinearLayoutManager(this)
        send_list.adapter = mSendLogAdapter

        rece_list.layoutManager = LinearLayoutManager(this)
        rece_list.adapter = mReceLogAdapter
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.connect -> {
                //searchNsdServer()
                connect()
            }
            R.id.disconnect -> {
                mNettyClient?.disconnect()
            }
            R.id.send_btn -> {
                if (!mNettyClient!!.getConnectStatus()) {
                    toast("未连接,请先连接")
                } else {
                    val msg = send_et.text.toString().trim()
                    if (TextUtils.isEmpty(msg)) {
                        return
                    }
                    val msgMessage = EchoMessage()
                    val byteArray = msg.toByteArray()
                    msgMessage.bytes = byteArray
                    mNettyClient!!.sendMsgToServer(msgMessage, ChannelFutureListener {
                        if (it.isSuccess) {
                            Log.d(TAG, "EchoMessage->Write auth successful")
                            val s = String(msgMessage.bytes, CharsetUtil.UTF_8)
                            logSend(s)
                        } else {
                            Log.d(TAG, "EchoMessage->Write auth error")
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
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, 1)
            }
            R.id.send_image_btn -> {
                if (!mNettyClient!!.getConnectStatus()) {
                    toast("未连接,请先连接")
                } else {
                    sendImageToServer()
                }
            }
        }
    }

    private fun connect() {
        Log.d(TAG, "connect")
        if (!mNettyClient!!.getConnectStatus()) {
            mNettyClient!!.setListener(this@MainActivity)
            mNettyClient!!.connect()//连接服务器
        } else {
            mNettyClient!!.disconnect()
        }
    }

    override fun onMessageResponseClient(msg: Any, index: Int) {
        Log.e(TAG, "onMessageResponse:$msg")
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
            logRece("$index:$str")
            //msg?.let { logRece(it) }
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onClientStatusConnectChanged(statusCode: Int, index: Int) {
        runOnUiThread {
            if (statusCode == NettyClientListener.STATUS_CONNECT_SUCCESS) {
                Log.e(TAG, "STATUS_CONNECT_SUCCESS:")
                connect.text = "DisConnect:$index"
            } else {
                Log.e(TAG, "onServiceStatusConnectChanged:$statusCode")
                connect.text = "Connect:$index"
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

    private fun logRece(log: String) {
        val logBean = LogBean(System.currentTimeMillis(), log)
        mReceLogAdapter.getDataList().add(0, logBean)
        runOnUiThread {
            mReceLogAdapter.notifyDataSetChanged()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            uri = data?.data
            sendImageToServer()
        }
    }

    private fun sendImageToServer() {
        val contentResolver = contentResolver
        if (uri != null) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                //val bitmap = BitmapFactory.decodeStream(inputStream)
                val bitmap = BitmapUtils.getBitmapFormUri(this, uri)
                image_selected.setImageBitmap(bitmap)
                val baos = BitmapUtils.baos
                val byteArray = baos!!.toByteArray()
                Log.i(TAG, "文件总长度:" + byteArray.size)
                val msgFile = EchoFile()
                msgFile.bytes = byteArray
                msgFile.file_name = Build.MANUFACTURER + "-" + UUID.randomUUID() + ".jpg"
                mNettyClient?.sendMsgToServer(msgFile, ChannelFutureListener {
                    if (it.isSuccess) {
                        Log.d(TAG, "msgFile Write auth successful")
                        //logSend(msg)
                    } else {
                        Log.d(TAG, "msgFile Write auth error")
                    }
                })
            } catch (e: Exception) {
            } finally {
            }
        } else {
            toast("图片获取失败")
        }
    }
}
