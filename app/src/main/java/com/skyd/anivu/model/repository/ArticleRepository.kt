package com.skyd.anivu.model.repository

import com.skyd.anivu.base.BaseRepository
import com.skyd.anivu.model.bean.ArticleBean
import com.skyd.anivu.model.db.dao.ArticleDao
import com.skyd.anivu.model.db.dao.FeedDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.zip
import javax.inject.Inject

class ArticleRepository @Inject constructor(
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    private val rssHelper: RssHelper,
) : BaseRepository() {
    fun requestArticleList(feedUrl: String): Flow<List<ArticleBean>> {
        return articleDao.getArticleList(feedUrl)
            .flowOn(Dispatchers.IO)
    }

    fun refreshArticleList(feedUrl: String): Flow<Unit> {
        return flow {
            val articleBeanList = rssHelper.queryRssXml(
                feed = feedDao.getFeed(feedUrl),
                latestLink = articleDao.queryLatestByFeedUrl(feedUrl)?.link
            )
            emit(articleDao.insertListIfNotExist(articleBeanList.map {
                if (it.feedUrl != feedUrl) it.copy(feedUrl = feedUrl)
                else it
            }))
        }.flowOn(Dispatchers.IO)
    }
}