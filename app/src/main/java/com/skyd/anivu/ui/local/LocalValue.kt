package com.skyd.anivu.ui.local

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import com.skyd.anivu.model.preference.IgnoreUpdateVersionPreference
import com.skyd.anivu.model.preference.appearance.DarkModePreference
import com.skyd.anivu.model.preference.appearance.DateStylePreference
import com.skyd.anivu.model.preference.appearance.NavigationBarLabelPreference
import com.skyd.anivu.model.preference.appearance.TextFieldStylePreference
import com.skyd.anivu.model.preference.appearance.ThemePreference
import com.skyd.anivu.model.preference.appearance.feed.FeedGroupExpandPreference
import com.skyd.anivu.model.preference.autodelete.AutoDeleteArticleBeforePreference
import com.skyd.anivu.model.preference.autodelete.AutoDeleteArticleFrequencyPreference
import com.skyd.anivu.model.preference.autodelete.UseAutoDeletePreference
import com.skyd.anivu.model.preference.behavior.PickImageMethodPreference
import com.skyd.anivu.model.preference.behavior.article.ArticleSwipeLeftActionPreference
import com.skyd.anivu.model.preference.behavior.article.ArticleTapActionPreference
import com.skyd.anivu.model.preference.behavior.article.DeduplicateTitleInDescPreference
import com.skyd.anivu.model.preference.behavior.feed.HideEmptyDefaultPreference
import com.skyd.anivu.model.preference.player.HardwareDecodePreference
import com.skyd.anivu.model.preference.player.PlayerDoubleTapPreference
import com.skyd.anivu.model.preference.player.PlayerShow85sButtonPreference
import com.skyd.anivu.model.preference.player.PlayerShowScreenshotButtonPreference

val LocalNavController = compositionLocalOf<NavHostController> {
    error("LocalNavController not initialized!")
}

val LocalWindowSizeClass = compositionLocalOf<WindowSizeClass> {
    error("LocalWindowSizeClass not initialized!")
}

// Appearance
val LocalTheme = compositionLocalOf { ThemePreference.default }
val LocalDarkMode = compositionLocalOf { DarkModePreference.default }
val LocalFeedGroupExpand = compositionLocalOf { FeedGroupExpandPreference.default }
val LocalTextFieldStyle = compositionLocalOf { TextFieldStylePreference.default }
val LocalDateStyle = compositionLocalOf { DateStylePreference.default }
val LocalNavigationBarLabel = compositionLocalOf { NavigationBarLabelPreference.default }

// Update
val LocalIgnoreUpdateVersion = compositionLocalOf { IgnoreUpdateVersionPreference.default }

// Behavior
val LocalDeduplicateTitleInDesc = compositionLocalOf { DeduplicateTitleInDescPreference.default }
val LocalArticleTapAction = compositionLocalOf { ArticleTapActionPreference.default }
val LocalArticleSwipeLeftAction = compositionLocalOf { ArticleSwipeLeftActionPreference.default }
val LocalHideEmptyDefault = compositionLocalOf { HideEmptyDefaultPreference.default }
val LocalPickImageMethod = compositionLocalOf { PickImageMethodPreference.default }

// Player
val LocalPlayerDoubleTap = compositionLocalOf { PlayerDoubleTapPreference.default }
val LocalPlayerShow85sButton = compositionLocalOf { PlayerShow85sButtonPreference.default }
val LocalPlayerShowScreenshotButton =
    compositionLocalOf { PlayerShowScreenshotButtonPreference.default }
val LocalHardwareDecode = compositionLocalOf { HardwareDecodePreference.default }

// Data
val LocalUseAutoDelete = compositionLocalOf { UseAutoDeletePreference.default }
val LocalAutoDeleteArticleFrequency =
    compositionLocalOf { AutoDeleteArticleFrequencyPreference.default }
val LocalAutoDeleteArticleBefore = compositionLocalOf { AutoDeleteArticleBeforePreference.default }