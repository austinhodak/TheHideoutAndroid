package com.austinhodak.thehideout

import androidx.recyclerview.widget.DiffUtil

interface RecyclerItem {
    val layoutId: Int

    val variableId: Int
    val dataToBind: Any

    val id: String

    override fun equals(other:Any?): Boolean

    companion object {

        fun <M: RecyclerItem> diffCallback()
                = object: DiffUtil.ItemCallback<M>() {

            override fun areItemsTheSame(od:M, nw:M)
                    = od === nw || od.id == nw.id

            override fun areContentsTheSame(od:M, nw:M)
                    = od == nw
        }
    }
}