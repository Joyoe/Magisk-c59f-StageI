package com.brightsight.joker.events.dialog

import androidx.lifecycle.lifecycleScope
import com.brightsight.joker.R
import com.brightsight.joker.core.base.BaseActivity
import com.brightsight.joker.core.tasks.MagiskInstaller
import com.brightsight.joker.view.MagiskDialog
import kotlinx.coroutines.launch

class EnvFixDialog : DialogEvent() {

    override fun build(dialog: MagiskDialog) = dialog
        .applyTitle(R.string.env_fix_title)
        .applyMessage(R.string.env_fix_msg)
        .applyButton(MagiskDialog.ButtonType.POSITIVE) {
            titleRes = android.R.string.ok
            preventDismiss = true
            onClick {
                dialog.applyTitle(R.string.setup_title)
                    .applyMessage(R.string.setup_msg)
                    .resetButtons()
                    .cancellable(false)
                (dialog.ownerActivity as BaseActivity).lifecycleScope.launch {
                    MagiskInstaller.FixEnv {
                        dialog.dismiss()
                    }.exec()
                }
            }
        }
        .applyButton(MagiskDialog.ButtonType.NEGATIVE) {
            titleRes = android.R.string.cancel
        }
        .let { }

    companion object {
        const val DISMISS = "com.brightsight.joker.ENV_DONE"
    }
}
