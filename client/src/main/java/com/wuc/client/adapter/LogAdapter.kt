package com.wuc.client.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.wuc.client.R
import com.wuc.client.bean.LogBean


/**
 * @author:     wuchao
 * @date:       2019-07-09 23:03
 * @desciption:
 */
class LogAdapter : RecyclerView.Adapter<LogAdapter.ItemHolder>() {

    var mDataList: MutableList<LogBean> = mutableListOf()

    fun getDataList(): MutableList<LogBean> {
        return mDataList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false))
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val bean = mDataList[position]
        holder.mTime.text = bean.getTime()
        holder.mLog.text = bean.log
        holder.itemView.setOnLongClickListener { v ->
            val cmb = v.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val log = mDataList[holder.adapterPosition]
            val msg = log.getTime() + " " + log.log
            cmb.primaryClip = ClipData.newPlainText(null, msg)
            Toast.makeText(v.context, "已复制到剪贴板", Toast.LENGTH_LONG).show()
            true
        }
    }


    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTime: TextView = itemView.findViewById(R.id.time) as TextView
        var mLog: TextView = itemView.findViewById(R.id.logtext) as TextView

    }
}