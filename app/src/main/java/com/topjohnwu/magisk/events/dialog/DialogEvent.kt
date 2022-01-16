package com.brightsight.joker.events.dialog

import com.brightsight.joker.arch.ActivityExecutor
import com.brightsight.joker.arch.BaseUIActivity
import com.brightsight.joker.arch.ViewEvent
import com.brightsight.joker.view.MagiskDialog

abstract class DialogEvent : ViewEvent(), ActivityExecutor {

    protected lateinit var dialog: MagiskDialog

    override fun invoke(activity: BaseUIActivity<*, *>) {
        dialog = MagiskDialog(activity)
            .apply { setOwnerActivity(activity) }
            .apply(this::build).reveal()
    }

    abstract fun build(dialog: MagiskDialog)

}

typealias GenericDialogListener = () -> Unit
