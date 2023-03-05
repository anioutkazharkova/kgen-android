package com.azharkova.kspgenandroid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azharkova.annotations.Adapter
import com.azharkova.annotations.BindLayout
import com.azharkova.annotations.BindSetup
import com.azharkova.annotations.BindVH
import com.azharkova.kspgenandroid.databinding.ItemTestLayoutBinding

@BindVH(Int::class)
class TestViewHolder(@BindLayout val itemViewBinding: ItemTestLayoutBinding) :
RecyclerView.ViewHolder(itemViewBinding.root) {

    @BindSetup
    fun setupData(data: Int) {
    itemViewBinding.text.text = data.toString()
    }
}

@Adapter(holders = [TestViewHolder::class], Int::class)
abstract class TestAdapter

/*
class TestAdapterImpl : RecyclerView.Adapter<TestViewHolder>() {
    val items: MutableList<Int> = mutableListOf()

    fun setupItems(items: List<Int>) {
        this.items.addAll(items)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        return TestViewHolder(ItemTestLayoutBinding.inflate( LayoutInflater.from(
            parent.context
        ), parent, false))
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.tag = position
        holder.bindItem(items.get(position))
    }

    override fun getItemCount(): Int = items.count()

}*/