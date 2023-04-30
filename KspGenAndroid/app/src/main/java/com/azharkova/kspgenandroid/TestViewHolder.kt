package com.azharkova.kspgenandroid

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.recyclerview.widget.RecyclerView
import com.azharkova.annotations.*
import com.azharkova.kspgenandroid.databinding.ItemTestLayoutBinding

@BindVH(Int::class)
class TestViewHolder(@BindLayout val itemViewBinding: ItemTestLayoutBinding, @BindItemClick(R.id.text) val item: ()->Unit = { } ) :
RecyclerView.ViewHolder(itemViewBinding.root) {


    @BindSetup
    fun setupData(data: Int) {
    itemViewBinding.text.text = data.toString()
    }
}

@Adapter(holders = [TestViewHolder::class], Int::class)
abstract class TestAdapter
