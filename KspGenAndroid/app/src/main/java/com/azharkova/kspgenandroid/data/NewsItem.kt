package com.azharkova.kspgenandroid.data

import com.azharkova.annotations.*
import com.azharkova.kspgenandroid.R
import com.azharkova.kspgenandroid.databinding.ItemLayoutBinding


data class NewsItem(
    val author: String?,
    val title: String?, val description: String?,
    val url: String?, val urlToImage: String?,
    val publishedAt: String?,
    val content: String?
)

fun NewsItem.toModel():NewsItemModel {
    return NewsItemModel(title,description,urlToImage, publishedAt, content)
}

@MapModel
data class NewsItemModel(
    @MapText(R.id.title_news) val title: String?, val description: String?,
    @MapImage(R.id.image_news) val urlToImage: String?,
    @MapText(R.id.date_news) val publishedAt: String?,
    val content: String?
)

@MapView(model = NewsItemModel::class)
class NewsItemView(@BindLayout val itemViewBinding: ItemLayoutBinding)



