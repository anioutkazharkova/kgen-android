package com.azharkova.kspgenandroid

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azharkova.annotations.ListScreen
import com.azharkova.annotations.VmResult
import com.azharkova.kspgenandroid.data.NewsItem
import com.azharkova.kspgenandroid.data.NewsItemModel
import com.azharkova.kspgenandroid.data.NewsItemView
import com.azharkova.kspgenandroid.data.toModel
import com.azharkova.test_kmm.data.NewsList
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun NewsListItemView(item: NewsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp,8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        GlideImage(model = item.urlToImage?.orEmpty(), contentDescription = "NewItem",
            modifier = Modifier.width(120.dp).height(120.dp).padding(0.dp,0.dp,8.dp, 0.dp))
        Column {
            Text(text = item.title.orEmpty(), style = MaterialTheme.typography.h6)
            Text(text = item.content.orEmpty(), style = MaterialTheme.typography.subtitle1, maxLines = 3)
            Text(text = item.publishedAt.orEmpty(), style = MaterialTheme.typography.caption)
        }
    }
}

@Composable
fun NewsListView(items: List<NewsItem>) {
    LazyColumn(
        contentPadding = // 1.
        PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(items) {
            NewsListItemView(it)
        }
    }

}

@Composable
fun NewsListScreen(newsList: NewsList) {
    NewsListView(newsList?.articles.orEmpty())
}

@ListScreen(NewsListVM::class, NewsItemView::class)
class NewsListScreenView



class NewsListVM: ViewModel(),IVM {
    @VmResult
    var data = MutableStateFlow<List<NewsItemModel>?>(null)

    val service by lazy {
        NewsService()
    }

    override fun loadData() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                service.loadNews()
            }
            data.tryEmit(result.articles?.map {
                it.toModel()
            }.orEmpty())
        }
    }
}

interface IVM {
    fun loadData()
}