package com.azharkova.kspgenandroid

import com.azharkova.annotations.GoTo
import com.azharkova.annotations.Navigator

@Navigator
interface SampleNavigator {

    @GoTo(NewsItemActivity::class)
    fun toNewsItem(item: NewsItem)
}


data class NewsItem(val id: Int)