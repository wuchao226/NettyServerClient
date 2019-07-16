package com.wuc.server.nettyserver

import android.util.Log
import cc.lison.pojo.EchoPojo
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel


/**
 * @author:     wuchao
 * @date:       2019-07-09 17:58
 * @desciption: 服务端
 */
class NettyServer {

    companion object {
        val instance: NettyServer by lazy { Holder.INSTANCE }
    }

    private object Holder {
        val INSTANCE = NettyServer()
    }

    private val TAG = "NettyServer"
    private val port = 1088
    private var channel: Channel? = null
    private lateinit var mListener: NettyServerListener
    private var isConnectStatus: Boolean = false
    /**
     * 服务是否启动
     */
    private var isServerStart: Boolean = false
    /**
     * NioEventLoopGroup 用来处理NIO操作的多线程事件循环器
     * NioEventLoopGroup实际上就是个线程池，NioEventLoopGroup在后台启动了n个IO线程（NioEventLoop）来处理Channel事件，
     * 每一个NioEventLoop负责处理m个Channel，NioEventLoopGroup从NioEventLoop数组里挨个取出NioEventLoop来处理Channel
     */
    //bossGroup是获取连接的，workerGroup是用来处理连接的，这二个线程组都是死循环
    private var bossGroup: EventLoopGroup? = null
    private var workerGroup: EventLoopGroup? = null


    fun start() {
        //使用Thread线程进行异步连接
        val mThread = object : Thread("NettyConnect.reConnect") {
            override fun run() {
                super.run()
                startServer()
            }
        }
        mThread.start()
    }

    private fun startServer() {
        try {
            //bossGroup是获取连接的，workerGroup是用来处理连接的，这二个线程组都是死循环
            bossGroup = NioEventLoopGroup(1)
            workerGroup = NioEventLoopGroup()
            //简化服务端启动的一个类
            val serverBootstrap = ServerBootstrap()
            //group有二个重载方法，一个是接收一个EventLoopGroup类型参数的方法，一个是接收二个EventLoopGroup类型的参数的方法
            serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                //.localAddress(InetSocketAddress(port))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(NettyServerInitializer(mListener))
            // Bind and start to accept incoming connections.
            // ChannelFuture的作用是用来保存Channel异步操作的结果。
            val channelFuture = serverBootstrap.bind(port).sync()
            Log.d(
                TAG,
                NettyServer::class.java.name + " started and listen on " + channelFuture.channel().localAddress()
            )
            isServerStart = true
            mListener.onStartServer()
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            /**CloseFuture异步方式关闭*/
            channelFuture.channel().closeFuture().sync()
        } catch (e: Exception) {
            Log.e(TAG, e.localizedMessage)
            e.printStackTrace()
        } finally {
            isServerStart = false
            mListener.onStopServer()
            //关闭，释放线程资源
            workerGroup?.shutdownGracefully()
            bossGroup?.shutdownGracefully()
        }
    }

    fun disconnect() {
        workerGroup?.shutdownGracefully()
        bossGroup?.shutdownGracefully()
    }

    fun setListener(listener: NettyServerListener) {
        this.mListener = listener
    }

    fun setConnectStatus(connectStatus: Boolean) {
        this.isConnectStatus = connectStatus
    }

    fun getConnectStatus(): Boolean {
        return isConnectStatus
    }

    fun isServerStart(): Boolean {
        return isServerStart
    }

    /**
     * 切换通道
     * 设置服务端，与哪个客户端通信
     * @param channel
     */
    fun setChannel(channel: Channel) {
        this.channel = channel
    }

    fun sendMsgToServer(data: String, listener: ChannelFutureListener): Boolean {
        val flag = channel != null && isConnectStatus && channel?.isActive!!
        if (flag) {
            channel?.writeAndFlush(data + System.getProperty("line.separator"))?.addListener(listener)
        }
        return flag
    }

    fun sendMsgToServer(file: EchoPojo, listener: ChannelFutureListener): Boolean {
        val flag = channel != null && isConnectStatus && channel?.isActive!!
        if (flag) {
            channel?.writeAndFlush(file)?.addListener(listener)
        }
        return flag
    }
}