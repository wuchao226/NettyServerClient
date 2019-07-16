package com.wuc.client.nettyclient

import android.util.Log
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.serialization.ClassResolvers
import io.netty.handler.codec.serialization.ObjectDecoder
import io.netty.handler.codec.serialization.ObjectEncoder
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit


/**
 * @author:     wuchao
 * @date:       2019-07-10 14:53
 * @desciption: 服务端HttpServerInitializer（初始化连接的时候执行的回调），处理器Handler构成了一个链路
 */
class NettyClientInitializer(var mListener: NettyClientListener?, var index: Int) :
    ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel?) {
        Log.d("NettyServerInitializer", "initChannel ch:$ch")
        val pipeline = ch?.pipeline()
        pipeline?.addLast(ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null)))
        pipeline?.addLast(ObjectEncoder())
        //黏包处理
        pipeline?.addLast(LineBasedFrameDecoder(1024))
        //字符串解码
        //pipeline?.addLast(StringDecoder(CharsetUtil.UTF_8))
        //字符串编码
        //pipeline?.addLast(StringEncoder(CharsetUtil.UTF_8))
        //5s未发送数据，回调userEventTriggered
        pipeline?.addLast(
            "ping",
            IdleStateHandler(0, 5, 0, TimeUnit.SECONDS)
        )
        pipeline?.addLast(NettyClientHandler(mListener, index))
    }
}