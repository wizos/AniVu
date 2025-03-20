package com.skyd.anivu.ui.screen.settings.data.importexport.importopml

import com.skyd.anivu.base.mvi.MviSingleEvent
import com.skyd.anivu.model.repository.importexport.IImportRepository.ImportOpmlResult

sealed interface ImportOpmlEvent : MviSingleEvent {
    sealed interface ImportOpmlResultEvent : ImportOpmlEvent {
        data class Success(val result: ImportOpmlResult) : ImportOpmlResultEvent
        data class Failed(val msg: String) : ImportOpmlResultEvent
    }
}