package com.skyd.anivu.ui.fragment.media.list

import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.skyd.anivu.R
import com.skyd.anivu.base.mvi.getDispatcher
import com.skyd.anivu.ext.activity
import com.skyd.anivu.ext.plus
import com.skyd.anivu.ext.showSnackbarWithLaunchedEffect
import com.skyd.anivu.ext.toUri
import com.skyd.anivu.model.bean.MediaGroupBean
import com.skyd.anivu.model.bean.VideoBean
import com.skyd.anivu.ui.activity.PlayActivity
import com.skyd.anivu.ui.component.AnimatedPlaceholder
import com.skyd.anivu.ui.fragment.media.CreateGroupDialog
import com.skyd.anivu.ui.fragment.media.sub.SUB_MEDIA_SCREEN_PATH_KEY
import com.skyd.anivu.ui.local.LocalNavController

class GroupInfo(
    val group: MediaGroupBean,
    val version: Long,   // For update, if version changed, MediaList will refresh media list
    val onCreateGroup: (MediaGroupBean) -> Unit,
    val onMoveFileToGroup: (VideoBean, MediaGroupBean) -> Unit,
)

@Composable
internal fun MediaList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    path: String,
    groupInfo: GroupInfo? = null,
    viewModel: MediaListViewModel = hiltViewModel(key = path + groupInfo?.group)
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val navController = LocalNavController.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->

        val uiState by viewModel.viewState.collectAsStateWithLifecycle()
        val uiEvent by viewModel.singleEvent.collectAsStateWithLifecycle(initialValue = null)

        val dispatch = viewModel.getDispatcher(
            path,
            groupInfo?.version,
            startWith = MediaListIntent.Init(
                path = path,
                group = groupInfo?.group,
                version = groupInfo?.version,
            )
        )
        val state = rememberPullRefreshState(
            refreshing = uiState.listState.loading,
            onRefresh = {
                dispatch(
                    MediaListIntent.Refresh(
                        path = path,
                        group = groupInfo?.group
                    )
                )
            },
        )
        Box(modifier = Modifier.pullRefresh(state)) {
            when (val listState = uiState.listState) {
                is ListState.Failed,
                is ListState.Init -> Unit

                is ListState.Success -> {
                    if (listState.list.isEmpty()) {
                        EmptyPlaceholder()
                    } else {
                        MediaList(
                            modifier = modifier,
                            list = listState.list,
                            groups = uiState.groups,
                            groupInfo = groupInfo,
                            onPlay = {
                                PlayActivity.play(
                                    context.activity,
                                    it.file.toUri(context)
                                )
                            },
                            onOpenDir = {
                                navController.navigate(
                                    R.id.action_to_sub_media_fragment,
                                    Bundle().apply {
                                        putString(SUB_MEDIA_SCREEN_PATH_KEY, it.file.path)
                                    }
                                )
                            },
                            onRemove = { dispatch(MediaListIntent.DeleteFile(it.file)) },
                            contentPadding = innerPadding + contentPadding,
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = uiState.listState.loading,
                state = state,
                modifier = Modifier
                    .padding(contentPadding)
                    .align(Alignment.TopCenter),
            )
        }

        when (val event = uiEvent) {
            is MediaListEvent.DeleteFileResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = event.msg, key1 = event
                )

            is MediaListEvent.MediaListResultEvent.Failed ->
                snackbarHostState.showSnackbarWithLaunchedEffect(
                    message = event.msg, key1 = event
                )

            null -> Unit
        }
    }
}

@Composable
private fun EmptyPlaceholder() {
    AnimatedPlaceholder(
        modifier = Modifier
            .width(220.dp)
            .padding(vertical = 20.dp),
        resId = R.raw.lottie_empty_1,
        tip = stringResource(id = R.string.empty_tip_1),
    )
}

@Composable
private fun MediaList(
    modifier: Modifier = Modifier,
    list: List<VideoBean>,
    groups: List<MediaGroupBean>,
    groupInfo: GroupInfo?,
    onPlay: (VideoBean) -> Unit,
    onOpenDir: (VideoBean) -> Unit,
    onRemove: (VideoBean) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var openEditMediaDialog by rememberSaveable {
        mutableStateOf<VideoBean?>(null, policy = referentialEqualityPolicy())
    }
    var openCreateGroupDialog by rememberSaveable { mutableStateOf(false) }
    var createGroupDialogGroup by rememberSaveable { mutableStateOf("") }

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding + PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        columns = GridCells.Fixed(1),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(list) { item ->
            Media1Item(
                data = item,
                onPlay = onPlay,
                onOpenDir = onOpenDir,
                onRemove = onRemove,
                onLongClick = if (groupInfo == null) null else {
                    { openEditMediaDialog = it }
                },
            )
        }
    }
    if (openEditMediaDialog != null) {
        val videoBean = openEditMediaDialog!!
        EditMediaSheet(
            onDismissRequest = { openEditMediaDialog = null },
            file = videoBean.file,
            currentGroup = groupInfo!!.group,
            groups = groups,
            onDelete = {
                onRemove(videoBean)
                openEditMediaDialog = null
            },
            onGroupChange = {
                groupInfo.onMoveFileToGroup(videoBean, it)
                openEditMediaDialog = null
            },
            openCreateGroupDialog = {
                openCreateGroupDialog = true
                createGroupDialogGroup = ""
            },
        )
    }

    CreateGroupDialog(
        visible = openCreateGroupDialog,
        value = createGroupDialogGroup,
        onValueChange = { text -> createGroupDialogGroup = text },
        onCreateGroup = {
            groupInfo?.onCreateGroup?.invoke(it)
            openCreateGroupDialog = false
            createGroupDialogGroup = ""
        },
        onDismissRequest = {
            openCreateGroupDialog = false
            createGroupDialogGroup = ""
        }
    )
}