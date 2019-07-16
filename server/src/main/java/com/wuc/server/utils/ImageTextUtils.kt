package com.wuc.server.utils

import android.content.Context
import android.graphics.*
import com.wuc.server.ServerApplication.Companion.context
import kotlin.math.ceil


/**
 * @author:     wuchao
 * @date:       2019-07-15 14:12
 * @desciption: 图片加上文字
 */
object ImageTextUtils {

    /**传递进来的源图片*/
    private var mBitmapSource: Bitmap? = null
    /**图片的配文*/
    private var mText: String? = null
    /**图片加上配文后生成的新图片*/
    private var mNewBitmap: Bitmap? = null
    /**配文的颜色*/
    private var mTextColor: Int = Color.BLACK
    /**配文的字体大小*/
    private var mTextSize: Float = 36f
    /**图片的宽度*/
    private var mBitmapWidth: Int = 0
    /**图片的高度*/
    private var mBitmapHeight: Int = 0
    /**画图片的画笔*/
    private var mBitmapPaint: Paint? = null
    /**画文字的画笔*/
    private var mTextPaint: Paint? = null
    /**配文与图片间的距离*/
    private var mPadding: Float = dp2px(10).toFloat()
    /**配文行与行之间的距离*/
    private var mLinePadding: Float = dp2px(15).toFloat()

    init {
        mBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    fun drawTextToBitmap(context: Context, imgId: Int, text: String): Bitmap? {
        val bitmapSource = BitmapFactory.decodeResource(context.resources, imgId)
        mBitmapWidth = bitmapSource.width
        mBitmapHeight = bitmapSource.height
        //一行可以显示文字的个数
        val lineTextCount = ((mBitmapWidth - 20) / mTextSize).toInt()
        //一共要把文字分为几行
        val line = ceil(text.length.toDouble() / lineTextCount.toDouble()).toInt()
        //新创建一个新图片比源图片多出一部分，后续用来与文字叠加用
        //宽度就是图片宽度，高度是图片高度+配文与图片间的间距+文字大小*文字行数+文字间的行间距*文字行数；
        val totalHeight = mBitmapHeight + mPadding + mTextSize * line + mLinePadding * line
        mNewBitmap = Bitmap.createBitmap(
            mBitmapWidth,
            totalHeight.toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(mNewBitmap!!)
        //把图片画上来
        canvas.drawBitmap(bitmapSource, 0f, 0f, mBitmapPaint)
        //在图片下边画一个白色矩形块用来放文字，防止文字是透明背景，在有些情况下保存到本地后看不出来
        mTextPaint?.color = Color.WHITE
        canvas.drawRect(
            0f, mBitmapHeight.toFloat(), mBitmapWidth.toFloat(),
            totalHeight, mTextPaint!!
        )

        //把文字画上来
        mTextPaint?.color = mTextColor
        mTextPaint?.textSize = mTextSize

        val boundsRect = Rect()
        //开启循环直到画完所有行的文字
        for (i in 0 until line) {
            val str: String = if (i == line - 1) {
                text.substring(i * lineTextCount, text.length)
            } else {//不是最后一行
                text.substring(i * lineTextCount, (i + 1) * lineTextCount)
            }
            //如果是最后一行，则结束位置就是文字的长度，别下标越界哦
            //获取文字的字宽高以便把文字与图片中心对齐
            mTextPaint?.getTextBounds(str, 0, str.length, boundsRect)
            //画文字的时候高度需要注意文字大小以及文字行间距
            canvas.drawText(
                str,
                (mBitmapWidth / 2 - boundsRect.width() / 2).toFloat(),
                mBitmapHeight + mPadding + i * mLinePadding + boundsRect.height() / 2,
                mTextPaint!!
            )
        }
        canvas.save()
        canvas.restore()
        return mNewBitmap
    }


    private fun dp2px(value: Int): Int {
        val v = context.resources.displayMetrics.density
        return (v * value + 0.5f).toInt()
    }

    private fun sp2px(value: Int): Int {
        val v = context.resources.displayMetrics.scaledDensity
        return (v * value + 0.5f).toInt()
    }

}