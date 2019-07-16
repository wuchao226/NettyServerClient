package com.wuc.server.nettyserver

import io.netty.channel.Channel



/**
 * @author:     wuchao
 * @date:       2019-07-09 16:38
 * @desciption:
 */
interface NettyServerListener {

    companion object {
        const val STATUS_CONNECT_SUCCESS: Int = 1

        const val STATUS_CONNECT_CLOSED: Int = 0

        const val STATUS_CONNECT_ERROR: Int = 0
    }

    /**
     *
     * @param msg
     * @param channelId unique id
     */
    fun onMessageResponseServer(msg: Any?, channelId: String?)

    /**
     * 与客户端建立连接
     * @param channel
     */
    fun onChannelConnect(channel: Channel)

    /**
     * 与客户端断开连接
     * @param
     */
    fun onChannelDisConnect(channel: Channel)

    /**
     * server开启成功
     */
    fun onStartServer()

    /**
     * server关闭
     */
    fun onStopServer()

    fun onServiceStatusConnectChanged(statusCode: Int)
}