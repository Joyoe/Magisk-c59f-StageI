package com.brightsight.joker.ui.theme

import com.brightsight.joker.arch.BaseViewModel
import com.brightsight.joker.events.RecreateEvent
import com.brightsight.joker.events.dialog.DarkThemeDialog
import com.brightsight.joker.view.TappableHeadlineItem

class ThemeViewModel : BaseViewModel(), TappableHeadlineItem.Listener {

    val themeHeadline = TappableHeadlineItem.ThemeMode

    override fun onItemPressed(item: TappableHeadlineItem) = when (item) {
        is TappableHeadlineItem.ThemeMode -> darkModePressed()
        else -> Unit
    }

    fun saveTheme(theme: Theme) {
        theme.select()
        RecreateEvent().publish()
    }

    private fun darkModePressed() = DarkThemeDialog().publish()

}
