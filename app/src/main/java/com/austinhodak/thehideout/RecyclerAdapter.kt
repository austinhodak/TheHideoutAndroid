package com.austinhodak.thehideout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter<M : RecyclerItem>(
    diffCallback: DiffUtil.ItemCallback<M>,
    @LayoutRes val placeholderId: Int?,
    private val clickListener: ((RecyclerAdapter<M>, Int, M) -> Unit)? = null
) : ListAdapter<M, RecyclerAdapter<M>.ViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            viewType,
            parent,
            false
        )
    ).also { viewHolder ->
        if (viewType != placeholderId && clickListener != null)
            viewHolder.itemView.setOnClickListener {
                val position = viewHolder.absoluteAdapterPosition
                val item = getItem(position) ?: return@setOnClickListener

                clickListener.invoke(this@RecyclerAdapter, position, item)
            }
    }

    override fun getItemViewType(position: Int) =
        requireNotNull(getItem(position)?.layoutId ?: placeholderId) {
            "item at $position is null"
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.run(holder.binding::bind)
    }

    /* ViewHolder */
    inner class ViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)
}

private fun ViewDataBinding.bind(item: RecyclerItem) {
    setVariable(item.variableId, item.dataToBind)
    executePendingBindings()
}