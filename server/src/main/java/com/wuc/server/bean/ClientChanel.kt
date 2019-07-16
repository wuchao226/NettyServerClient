package com.wuc.server.bean

import io.netty.channel.Channel

/**
 * @author:     wuchao
 * @date:       2019-07-14 18:51
 * @desciption: 客户端信息
 */
data class ClientChanel(
    var clientIp: String,//客户端ip
    var channel: Channel,//与客户端建立的通道
    var shortId: String //通道的唯一标示
)