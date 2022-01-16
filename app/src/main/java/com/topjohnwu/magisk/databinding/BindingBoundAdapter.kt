package com.brightsight.joker.databinding

import androidx.databinding.ViewDataBinding
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter

open class BindingBoundAdapter : BindingRecyclerViewAdapter<RvItem>() {

    override fun onBindBinding(binding: ViewDataBinding, variableId: Int, layoutRes: Int, position: Int, item: RvItem) {
        super.onBindBinding(binding, variableId, layoutRes, position, item)

        item.onBindingBound(binding)
    }
}