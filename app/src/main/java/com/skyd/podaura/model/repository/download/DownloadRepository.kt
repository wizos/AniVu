package com.skyd.podaura.model.repository.download

import com.skyd.podaura.appContext
import com.skyd.podaura.config.Const
import com.skyd.podaura.config.TORRENT_RESUME_DATA_DIR
import com.skyd.podaura.ext.sampleWithoutFirst
import com.skyd.podaura.model.bean.download.DownloadInfoBean
import com.skyd.podaura.model.bean.download.bt.BtDownloadInfoBean
import com.skyd.podaura.model.repository.BaseRepository
import com.skyd.podaura.model.repository.download.bt.BtDownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File

class DownloadRepository : BaseRepository() {
    suspend fun requestBtDownloadTasksList(): Flow<List<BtDownloadInfoBean>> = combine(
        BtDownloadManager.getDownloadInfoList().distinctUntilChanged(),
        BtDownloadManager.peerInfoMapFlow.sampleWithoutFirst(1000),
        BtDownloadManager.torrentStatusMapFlow.sampleWithoutFirst(1000),
    ) { list, peerInfoMap, uploadPayloadRateMap ->
        list.map { downloadInfoBean ->
            downloadInfoBean.copy().apply {
                peerInfoList = peerInfoMap.getOrDefault(downloadRequestId, emptyList()).toList()
                val torrentStatus = uploadPayloadRateMap[downloadRequestId]
                if (torrentStatus != null) {
                    uploadPayloadRate = torrentStatus.uploadPayloadRate()
                    downloadPayloadRate = torrentStatus.downloadPayloadRate()
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    fun requestDownloadTasksList(): Flow<List<DownloadInfoBean>> {
        return DownloadManager.getInstance(appContext).downloadInfoListFlow
    }

    fun deleteBtDownloadTaskInfo(
        link: String,
    ): Flow<Unit> = flow {
        if (BtDownloadManager.getDownloadState(link)?.downloadComplete() != true) {
            BtDownloadManager.getTorrentFilesByLink(link).forEach {
                File(it.path).deleteRecursively()
            }
        }
        val requestUuid = BtDownloadManager.getDownloadInfo(link)?.downloadRequestId
        if (!requestUuid.isNullOrBlank()) {
            File(Const.TORRENT_RESUME_DATA_DIR, requestUuid).deleteRecursively()
        }
        // 这些最后删除，防止上面会使用
        BtDownloadManager.deleteDownloadInfo(link)
        BtDownloadManager.deleteSessionParams(link)
        BtDownloadManager.removeDownloadLinkUuidMap(link)
        emit(Unit)
    }.flowOn(Dispatchers.IO)
}