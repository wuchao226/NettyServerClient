package com.wuc.server.nettyserver

import android.util.Log
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor

/**
 * @author:     wuchao
 * @date:       2019-07-09 16:49
 * @desciption:
 */
@ChannelHandler.Sharable
class NettyServerHandler(var mListener: NettyServerListener?) : SimpleChannelInboundHandler<Any>() {

    private val TAG = "NettyServerHandler"
    //保留所有与服务器建立连接的channel对象
    private val channelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)


    /**
     * 在数据被接收的时候调用。
     */
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Any?) {
        Log.d(TAG, "channelRead0 : $msg")
//        if (msg.equals("Heartbeat")) {
//            Log.d(TAG, "Heartbeat")
//            return //客户端发送来的心跳数据
//        }
        /*if (msg is EchoFile) {
            val bytes = msg.bytes
            val file_name = msg.file_name
            Log.d(TAG, "channelRead0-> bytes : $bytes" + "file_name：$file_name")
        }*/
        mListener?.onMessageResponseServer(msg, ctx?.channel()?.id()?.asShortText())
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
        /*if (msg is EchoFile) {
            val bytes = msg.bytes
            val file_name = msg.file_name
            Log.d(TAG, "channelRead-> bytes : $bytes" + "file_name：$file_name")
        }*/
    }

    /**
     * 表示服务端与客户端连接建立
     */
    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        super.handlerAdded(ctx)
        val channel = ctx?.channel()  //其实相当于一个connection

        /**
         * 调用channelGroup的writeAndFlush其实就相当于channelGroup中的每个channel都writeAndFlush
         *
         * 先去广播，再将自己加入到channelGroup中
         */
        channelGroup.writeAndFlush(" 【服务器】 -" + channel?.remoteAddress() + " 加入\n")
        channelGroup.add(channel)
    }

    /**
     * 客户端断开连接
     */
    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        super.handlerRemoved(ctx)
        val channel = ctx?.channel()
        channelGroup.writeAndFlush(" 【服务器】 -" + channel?.remoteAddress() + " 离开\n")

        //验证一下每次客户端断开连接，连接自动地从channelGroup中删除调。
        println(channelGroup.size)
        //当客户端和服务端断开连接的时候，下面的那段代码netty会自动调用，所以不需要人为的去调用它
        //channelGroup.remove(channel);
    }

    /**
     * 在连接被建立并且准备进行通信时被调用。
     */
    override fun channelActive(ctx: ChannelHandlerContext?) {
        Log.d(TAG, "channel active")
        super.channelActive(ctx)
        val channel = ctx?.channel()
        println(channel?.remoteAddress().toString() + " 上线了")
        mListener?.onChannelConnect(channel!!)
        NettyServer.instance.setConnectStatus(true)
        mListener?.onServiceStatusConnectChanged(NettyServerListener.STATUS_CONNECT_SUCCESS)
    }

    /**
     * 连接断开
     */
    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        Log.e(TAG, "channelInactive")
        val channel = ctx?.channel()
        println(channel?.remoteAddress().toString() + " 下线了")
        mListener?.onChannelDisConnect(channel!!)
        NettyServer.instance.setConnectStatus(false)
        mListener?.onServiceStatusConnectChanged(NettyServerListener.STATUS_CONNECT_CLOSED)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        super.channelReadComplete(ctx)
        ctx?.fireChannelReadComplete()
        Log.d(TAG, "channelReadComplete")
    }

    /**
     * exceptionCaught()事件处理方法是当出现Throwable对象才会被调用，
     * 即当Netty由于IO错误或者处理器在处理事件时抛出的异常时。
     * 在大部分情况下，捕获的异常应该被记录下来并且把关联的channel给关闭掉。
     */
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
        ctx?.close()
        Log.e(TAG, "Unexpected exception from downstream : " + cause?.message)
    }
}