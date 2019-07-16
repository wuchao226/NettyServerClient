package com.wuc.client.nettyclient

import android.util.Log
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

/**
 * @author:     wuchao
 * @date:       2019-07-09 18:36
 * @desciption:
 */
class NettyClientHandler(var mListener: NettyClientListener?, var index: Int) : SimpleChannelInboundHandler<Any>() {

    private val TAG = "NettyClientHandler"

    /**
     * 客户端收到消息
     */
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Any?) {
        Log.d(TAG, "channelRead0 : $msg")
        /*if (msg.equals("Heartbeat")) {
            Log.d(TAG, "Heartbeat")
            return //客户端发送来的心跳数据
        }*/
        if (msg != null) {
            mListener?.onMessageResponseClient(msg, index)
        }
    }

    /**
     * channelRead()方法是在数据被接收的时候调用。
     */
    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        super.channelRead(ctx, msg)
//        val buf = msg as ByteBuf
//        val req = ByteArray(buf.readableBytes())
//        buf.readBytes(req)
//        val body = String(req, Charset.forName("UTF-8"))
        Log.d(TAG, "verify : $msg")
    }

    /**
     * 连接成功
     */
    override fun channelActive(ctx: ChannelHandlerContext?) {
        super.channelActive(ctx)
        Log.d(TAG, "channel active")
        mListener?.onClientStatusConnectChanged(NettyClientListener.STATUS_CONNECT_SUCCESS, index)
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        Log.e(TAG, "channelInactive")
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
        super.userEventTriggered(ctx, evt)
        if (evt is IdleStateEvent) {
            //发送心跳
            if (evt.state() === IdleState.WRITER_IDLE) {
                ctx?.channel()?.writeAndFlush("Heartbeat" + System.getProperty("line.separator"))
            }
        }
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        Log.e(TAG, "exceptionCaught")
        mListener?.onClientStatusConnectChanged(NettyClientListener.STATUS_CONNECT_ERROR, index)
        cause?.printStackTrace()
        ctx?.close()
    }
}