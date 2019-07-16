package com.wuc.client.nettyclient

import android.os.SystemClock
import android.util.Log
import cc.lison.pojo.EchoPojo
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel


/**
 * @author:     wuchao
 * @date:       2019-07-09 18:50
 * @desciption:
 */
class NettyClient(var host: String, var tcp_port: Int, var index: Int) {


    private val TAG = "NettyClient"

    private var listener: NettyClientListener? = null

    private var channel: Channel? = null

    private var isConnect = false
    private var isNeedReconnect = true
    private var isConnecting = false

    private var reconnectIntervalTime: Long = 5000
    private var CONNECT_TIMEOUT_MILLIS = 5000
    /**
     * 重连次数
     */
    private var reconnectNum = Integer.MAX_VALUE

    private var group: EventLoopGroup? = null

    fun connect() {
        if (isConnect) {
            return
        }
        val clientThread = object : Thread("client-Netty") {
            override fun run() {
                super.run()
                isNeedReconnect = true
                reconnectNum = Integer.MAX_VALUE
                connectServer()
            }
        }
        clientThread.start()
    }

    private fun connectServer() {
        synchronized(NettyClient::class.java) {
            var channelFuture: ChannelFuture? = null
            if (!isConnect) {
                isConnect = true
                group = NioEventLoopGroup()
                val bootstrap = Bootstrap().group(group)
                    //通过NoDelay禁用Nagle,使消息立即发出去，不用等待到一定的数据量才发出去
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    //保持长连接状态
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel::class.java)
                    .handler(NettyClientInitializer(listener,index))
                try {
                    //发起异步连接操作
                    channelFuture = bootstrap.connect(host, tcp_port)
                        .addListener(object : ChannelFutureListener {
                            override fun operationComplete(channelFuture: ChannelFuture?) {
                                if (channelFuture?.isSuccess!!) {
                                    Log.e(TAG, "连接成功")
                                    isConnect = true
                                    channel = channelFuture.channel()
                                } else {
                                    Log.e(TAG, "连接失败")
                                    isConnect = false
                                }
                                isConnecting = false
                            }
                        }).sync()
                    // Wait until the connection is closed.
                    channelFuture.channel().closeFuture().sync()
                    Log.e(TAG, " 断开连接")
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isConnect = false
                    listener?.onClientStatusConnectChanged(NettyClientListener.STATUS_CONNECT_CLOSED, index)
                    if (null != channelFuture) {
                        if (channelFuture.channel() != null && channelFuture.channel().isOpen) {
                            channelFuture.channel().close()
                        }
                    }
                    group?.shutdownGracefully()
                    reconnect()
                }
            }
        }
    }

    private fun reconnect() {
        Log.e(TAG, "reconnect")
        if (isNeedReconnect && reconnectNum > 0 && !isConnect) {
            reconnectNum--
            SystemClock.sleep(reconnectIntervalTime)
            if (isNeedReconnect && reconnectNum > 0 && !isConnect) {
                Log.e(TAG, "重新连接")
                connectServer()
            }
        }
    }

    fun disconnect() {
        Log.e(TAG, "disconnect")
        isNeedReconnect = false
        group?.shutdownGracefully()
    }


    /**
     * 设置重连次数
     * @param reconnectNum
     */
    fun setReconnectNum(reconnectNum: Int) {
        this.reconnectNum = reconnectNum
    }

    /**
     * 设置重连间隔
     * @param reconnectIntervalTime
     */
    fun setReconnectIntervalTime(reconnectIntervalTime: Long) {
        this.reconnectIntervalTime = reconnectIntervalTime
    }

    /**
     * 获取TCP连接状态
     * @return
     */
    fun getConnectStatus(): Boolean {
        return isConnect
    }

    fun isConnecting(): Boolean {
        return isConnecting
    }

    fun setConnectStatus(status: Boolean) {
        this.isConnect = status
    }

    fun setListener(listener: NettyClientListener) {
        this.listener = listener
    }

    /**
     * 同步发送
     * @param data
     * @return
     */
    fun sendMsgToServer(data: String): Boolean {
        val flag = channel != null && isConnect
        if (flag) {
            val channelFuture =
                channel?.writeAndFlush(data + System.getProperty("line.separator"))?.awaitUninterruptibly()
            return channelFuture?.isSuccess!!
        }
        return false
    }


    fun sendMsgToServer(data: ByteArray, listener: ChannelFutureListener): Boolean {
        val flag = channel != null && isConnect
        if (flag) {
            val buf = Unpooled.copiedBuffer(data)
            channel?.writeAndFlush(buf)?.addListener(listener)
        }
        return flag
    }

    fun sendMsgToServer(file: EchoPojo, listener: ChannelFutureListener): Boolean {
        val flag = channel != null && isConnect
        if (flag) {
            channel?.writeAndFlush(file)?.addListener(listener)
        }
        return flag
    }

    fun sendMsgToServer(any: Any, listener: ChannelFutureListener): Boolean {
        val flag = channel != null && isConnect
        if (flag) {
            //			ByteBuf buf = Unpooled.copiedBuffer(data);
            //            ByteBuf byteBuf = Unpooled.copiedBuffer(data + System.getProperty("line.separator"), //2
            //                    CharsetUtil.UTF_8);
            channel?.writeAndFlush(any)?.addListener(listener)

        }
        return flag
    }

    fun sendMsgToServer(data: String, listener: ChannelFutureListener): Boolean {
        val flag = channel != null && isConnect
        if (flag) {
            //			ByteBuf buf = Unpooled.copiedBuffer(data);
            //            ByteBuf byteBuf = Unpooled.copiedBuffer(data + System.getProperty("line.separator"), //2
            //                    CharsetUtil.UTF_8);
            channel?.writeAndFlush(data + System.getProperty("line.separator"))?.addListener(listener)
        }
        return flag
    }


}