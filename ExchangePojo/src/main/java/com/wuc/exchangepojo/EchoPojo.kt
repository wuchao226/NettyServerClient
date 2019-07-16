package com.wuc.exchangepojo

import java.io.Serializable

/**
 * @author:     wuchao
 * @date:       2019-07-16 13:52
 * @desciption: 信息交换对象
 */
open class EchoPojo() :Serializable{
    /**
     * 总包数
     */
    var sumCountPackage: Int = 0

    /**
     * 当前包数
     */
    var countPackage: Int = 0

    /**
     * 交换信息数据字节
     */
    var bytes: ByteArray? = null

    /**
     * 发送人业务id
     */
    var send_uid: String? = null

    /**
     * 接收人业务id (0 接收目标为系统 其他为业务id)
     */
    var receive_uid: String? = null

    /**
     * 发送包时间
     */
    var send_time: Long = 0

    /**
     * 接收包时间
     */
    var receive_time: Long = 0

}