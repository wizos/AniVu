package com.skyd.anivu.ui.adapter.variety

import androidx.recyclerview.widget.GridLayoutManager
import com.skyd.anivu.appContext
import com.skyd.anivu.ext.screenIsLand
import com.skyd.anivu.model.bean.FeedBean

class AniSpanSize(
    private val adapter: VarietyAdapter,
    private val enableLandScape: Boolean = true
) : GridLayoutManager.SpanSizeLookup() {
    companion object {
        const val MAX_SPAN_SIZE = 60
    }

    override fun getSpanSize(position: Int): Int {
        return if (enableLandScape && appContext.screenIsLand) {
            when (adapter.dataList[position]) {
                is FeedBean -> MAX_SPAN_SIZE
                else -> MAX_SPAN_SIZE
            }
        } else {
            when (adapter.dataList[position]) {
                is FeedBean -> MAX_SPAN_SIZE
                else -> MAX_SPAN_SIZE
            }
        }
    }
}