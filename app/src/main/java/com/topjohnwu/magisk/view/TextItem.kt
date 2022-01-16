package com.brightsight.joker.view

import com.brightsight.joker.R
import com.brightsight.joker.databinding.ComparableRvItem

class TextItem(val text: Int) : ComparableRvItem<TextItem>() {
    override val layoutRes = R.layout.item_text

    override fun contentSameAs(other: TextItem) = text == other.text
    override fun itemSameAs(other: TextItem) = contentSameAs(other)
}
