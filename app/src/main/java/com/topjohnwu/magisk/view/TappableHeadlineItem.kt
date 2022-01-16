package com.brightsight.joker.view

import com.brightsight.joker.R
import com.brightsight.joker.databinding.ComparableRvItem

sealed class TappableHeadlineItem : ComparableRvItem<TappableHeadlineItem>() {

    abstract val title: Int
    abstract val icon: Int

    override val layoutRes = R.layout.item_tappable_headline

    override fun itemSameAs(other: TappableHeadlineItem) =
        this === other

    override fun contentSameAs(other: TappableHeadlineItem) =
        title == other.title && icon == other.icon

    // --- listener

    interface Listener {

        fun onItemPressed(item: TappableHeadlineItem)

    }

    // --- objects

    object Hide : TappableHeadlineItem() {
        override val title = R.string.jokerhide
        override val icon = R.drawable.ic_hide_md2
    }

    object ThemeMode : TappableHeadlineItem() {
        override val title = R.string.settings_dark_mode_title
        override val icon = R.drawable.ic_day_night
    }

}
