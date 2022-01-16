package com.brightsight.joker.events.dialog

import com.brightsight.joker.R
import com.brightsight.joker.view.MagiskDialog

class SuperuserRevokeDialog(
    builder: Builder.() -> Unit
) : DialogEvent() {

    private val callbacks = Builder().apply(builder)

    override fun build(dialog: MagiskDialog) {
        dialog.applyTitle(R.string.su_revoke_title)
            .applyMessage(R.string.su_revoke_msg, callbacks.appName)
            .applyButton(MagiskDialog.ButtonType.POSITIVE) {
                titleRes = android.R.string.ok
                onClick { callbacks.listenerOnSuccess() }
            }
            .applyButton(MagiskDialog.ButtonType.NEGATIVE) {
                titleRes = android.R.string.cancel
            }
    }

    inner class Builder internal constructor() {
        var appName: String = ""

        internal var listenerOnSuccess: GenericDialogListener = {}

        fun onSuccess(listener: GenericDialogListener) {
            listenerOnSuccess = listener
        }
    }
}
