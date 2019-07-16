package com.wuc.server.nettyserver

import android.util.Log
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder


/**
 * @author:     wuchao
 * @date:       2019-07-10 14:53
 * @desciption: 服务端HttpServerInitializer（初始化连接的时候执行的回调），处理器Handler构成了一个链路
 */
class NettyServerInitializer(var mListener: NettyServerListener?) : ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel?) {
        Log.d("NettyServerInitializer", "initChannel ch:$ch")
        val pipeline = ch?.pipeline()
        pipeline?.addLast(ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null)))
        pipeline?.addLast(ObjectEncoder())
        pipeline?.addLast(LineBasedFrameDecoder(1024))
        //字符串解码
        //pipeline?.addLast(StringDecoder(CharsetUtil.UTF_8))
        //字符串编码
        //pipeline?.addLast(StringEncoder(CharsetUtil.UTF_8))
        pipeline?.addLast(NettyServerHandler(mListener))
    }
}