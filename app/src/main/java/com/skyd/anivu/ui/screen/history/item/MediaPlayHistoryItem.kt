package com.skyd.anivu.ui.screen.history.item

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Feed
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.EventListener
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.skyd.anivu.R
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.isLocal
import com.skyd.anivu.ext.toDateTimeString
import com.skyd.anivu.model.bean.history.MediaPlayHistoryWithArticle
import com.skyd.anivu.ui.activity.player.PlayActivity
import com.skyd.anivu.ui.component.PodAuraImage
import com.skyd.anivu.ui.component.rememberPodAuraImageLoader
import com.skyd.anivu.ui.local.LocalMediaShowThumbnail
import com.skyd.anivu.ui.local.LocalNavController
import com.skyd.anivu.ui.mpv.isFdFileExists
import com.skyd.anivu.ui.mpv.land.controller.bar.toDurationString
import com.skyd.anivu.ui.screen.read.openReadScreen
import java.io.File

@Composable
fun MediaPlayHistoryItem(
    data: MediaPlayHistoryWithArticle,
    onDelete: (MediaPlayHistoryWithArticle) -> Unit,
) {
    val context = LocalContext.current
    val articleWithEnclosure = data.article?.articleWithEnclosure
    val path = data.mediaPlayHistoryBean.path
    val fileName = rememberSaveable(data) {
        articleWithEnclosure?.article?.title ?: path.substringAfterLast("/")
    }
    val isLocal = Uri.parse(path).isLocal()
    val mediaExists = remember(path) {
        !isLocal || (path.startsWith("fd://") && isFdFileExists(path) || File(path).exists())
    }
    val articleThumbnail = articleWithEnclosure?.media?.image ?: data.article?.feed?.icon
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(0.1f))
            .clickable {
                if (!mediaExists) {
                    return@clickable
                }
                if (isLocal) {
                    PlayActivity.play(
                        activity = context.activity,
                        startFilePath = path,
                        files = listOf(path),
                    )
                } else {
                    PlayActivity.play(
                        activity = context.activity,
                        articleId = articleWithEnclosure?.article?.articleId,
                        url = path,
                    )
                }
            }
            .padding(vertical = 6.dp)
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var showThumbnail by remember(data) { mutableStateOf(true) }
        val showLocalFileThumbnail = LocalMediaShowThumbnail.current && isLocal
        val showArticleThumbnail = !isLocal && articleThumbnail != null
        if (showThumbnail && (showLocalFileThumbnail || showArticleThumbnail)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .size(50.dp),
                contentAlignment = Alignment.Center,
            ) {
                PodAuraImage(
                    modifier = Modifier.fillMaxSize(),
                    model = remember(path) {
                        ImageRequest.Builder(context)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .data(if (showLocalFileThumbnail) path else articleThumbnail)
                            .crossfade(true)
                            .build()
                    },
                    imageLoader = rememberPodAuraImageLoader(listener = object :
                        EventListener() {
                        override fun onError(request: ImageRequest, result: ErrorResult) {
                            showThumbnail = false
                        }
                    }),
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
        Column {
            Text(
                text = fileName,
                modifier = Modifier.padding(top = 6.dp, end = 16.dp),
                maxLines = 3,
                style = MaterialTheme.typography.titleSmall,
            )
            if (!mediaExists) {
                Text(
                    text = stringResource(R.string.history_screen_media_not_exists),
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(
                modifier = Modifier.padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val lastPlayPosition = data.mediaPlayHistoryBean.lastPlayPosition
                Text(
                    modifier = Modifier
                        .basicMarquee()
                        .widthIn(min = 40.dp),
                    text = stringResource(
                        R.string.history_screen_last_seen,
                        (lastPlayPosition / 1000).toDurationString(),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
                Spacer(
                    modifier = Modifier
                        .widthIn(min = 6.dp)
                        .weight(1f)
                )
                val lastTime = data.mediaPlayHistoryBean.lastTime
                if (lastTime > 0) {
                    Text(
                        modifier = Modifier
                            .basicMarquee()
                            .widthIn(min = 40.dp),
                        text = remember(lastTime) { lastTime.toDateTimeString(context = context) },
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                }
                ActionIconButton(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(id = R.string.delete),
                    onClick = { onDelete(data) },
                )
                if (articleWithEnclosure != null) {
                    val navController = LocalNavController.current
                    ActionIconButton(
                        imageVector = Icons.AutoMirrored.Outlined.Feed,
                        contentDescription = stringResource(id = R.string.read_screen_name),
                        onClick = {
                            openReadScreen(
                                navController = navController,
                                articleId = articleWithEnclosure.article.articleId,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    imageVector: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    Icon(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(5.5.dp),
        imageVector = imageVector,
        contentDescription = contentDescription,
    )
}

@Composable
fun MediaPlayItemPlaceholder() {
    val color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(0.1f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color)
                .size(50.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxSize()
                    .height(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color)
                )
                Icon(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .padding(6.dp),
                    imageVector = Icons.Outlined.Delete,
                    tint = color,
                    contentDescription = stringResource(id = R.string.delete),
                )
            }
        }
    }
}