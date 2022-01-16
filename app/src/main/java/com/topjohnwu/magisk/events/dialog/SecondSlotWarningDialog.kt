package com.brightsight.joker.events.dialog

import com.brightsight.joker.R
import com.brightsight.joker.view.MagiskDialog

class SecondSlotWarningDialog : DialogEvent() {

    override fun build(dialog: MagiskDialog) {
        dialog.applyTitle(android.R.string.dialog_alert_title)
            .applyMessage(R.string.install_inactive_slot_msg)
            .applyButton(MagiskDialog.ButtonType.POSITIVE) {
                titleRes = android.R.string.ok
            }
            .cancellable(true)
    }
}
