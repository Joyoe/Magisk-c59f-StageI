package com.brightsight.joker.events.dialog

import android.app.ProgressDialog
import android.widget.Toast
import com.brightsight.joker.R
import com.brightsight.joker.arch.BaseUIActivity
import com.brightsight.joker.ui.flash.FlashFragment
import com.brightsight.joker.utils.Utils
import com.brightsight.joker.view.MagiskDialog
import com.brightsight.superuser.Shell

class UninstallDialog : DialogEvent() {

    override fun build(dialog: MagiskDialog) {
        dialog.applyTitle(R.string.uninstall_magisk_title)
            .applyMessage(R.string.uninstall_magisk_msg)
            .applyButton(MagiskDialog.ButtonType.POSITIVE) {
                titleRes = R.string.restore_img
                onClick { restore() }
            }
            .applyButton(MagiskDialog.ButtonType.NEGATIVE) {
                titleRes = R.string.complete_uninstall
                onClick { completeUninstall() }
            }
    }

    @Suppress("DEPRECATION")
    private fun restore() {
        val dialog = ProgressDialog(dialog.context).apply {
            setMessage(dialog.context.getString(R.string.restore_img_msg))
            show()
        }

        Shell.jojo("restore_imgs").submit { result ->
            dialog.dismiss()
            if (result.isSuccess) {
                Utils.toast(R.string.restore_done, Toast.LENGTH_SHORT)
            } else {
                Utils.toast(R.string.restore_fail, Toast.LENGTH_LONG)
            }
        }
    }

    private fun completeUninstall() {
        (dialog.ownerActivity as? BaseUIActivity<*, *>)
                ?.navigation?.navigate(FlashFragment.uninstall())
    }

}
