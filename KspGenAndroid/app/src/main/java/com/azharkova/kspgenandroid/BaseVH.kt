package com.azharkova.kspgenandroid

import android.content.Context
import android.content.res.Resources
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseVH<T>(val viewBinding: ViewBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {

    open fun bindItem(item: T) {}

    open var tag: Int = 0

    val context: Context
        get() = viewBinding.root.context

}

abstract class BaseVBAdapter<K : BaseVH<T>, T>(
    var onItemClickListener: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<K>() {

    var items: ArrayList<T> = arrayListOf()


    fun getItem(position: Int): T {
        return items[position]
    }


    override fun getItemCount(): Int = items.count()

    open fun setupItems(items: List<T>) {
        this.items = arrayListOf()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    open fun update(items: List<T>, callback: (Boolean) -> Unit) {}

    abstract fun takeViewHolder(parent: ViewGroup): BaseVH<T>

    override fun onBindViewHolder(holder: K, position: Int) {
        bindHolder(holder, position)
    }

    open fun bindHolder(holder: BaseVH<T>, position: Int) {
        val item = this.items[position]
        holder.bindItem(item)
        holder.tag = position
        onItemClickListener?.let { listener ->
            holder.itemView.setOnClickListener {
                listener.invoke(position)
            }
        }
    }
}