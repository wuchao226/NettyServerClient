package com.wuc.client.nettyclient


/**
 * @author:     wuchao
 * @date:       2019-07-09 18:35
 * @desciption:
 */
interface NettyClientListener {

    companion object {
        const val STATUS_CONNECT_SUCCESS: Int = 1

        const val STATUS_CONNECT_CLOSED: Int = 0

        const val STATUS_CONNECT_ERROR: Int = 0
    }

    /**
     * 当接收到系统消息
     */
    fun onMessageResponseClient(msg: Any, index: Int)

    /**
     * 当服务状态发生变化时触发
     */
    fun onClientStatusConnectChanged(statusCode: Int, index: Int)
}