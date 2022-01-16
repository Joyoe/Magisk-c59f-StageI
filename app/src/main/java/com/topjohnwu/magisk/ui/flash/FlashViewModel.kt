package com.brightsight.joker.ui.flash

import android.view.MenuItem
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.brightsight.joker.BR
import com.brightsight.joker.R
import com.brightsight.joker.arch.BaseViewModel
import com.brightsight.joker.arch.diffListOf
import com.brightsight.joker.arch.itemBindingOf
import com.brightsight.joker.core.Const
import com.brightsight.joker.core.Info
import com.brightsight.joker.core.tasks.FlashZip
import com.brightsight.joker.core.tasks.MagiskInstaller
import com.brightsight.joker.core.utils.MediaStoreUtils
import com.brightsight.joker.core.utils.MediaStoreUtils.outputStream
import com.brightsight.joker.databinding.RvBindingAdapter
import com.brightsight.joker.events.SnackbarEvent
import com.brightsight.joker.ktx.*
import com.brightsight.joker.utils.set
import com.brightsight.joker.view.Notifications
import com.brightsight.superuser.CallbackList
import com.brightsight.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FlashViewModel : BaseViewModel() {

    @get:Bindable
    var showReboot = Shell.rootAccess()
        set(value) = set(value, field, { field = it }, BR.showReboot)

    private val _subtitle = MutableLiveData(R.string.flashing)
    val subtitle get() = _subtitle as LiveData<Int>

    val adapter = RvBindingAdapter<ConsoleItem>()
    val items = diffListOf<ConsoleItem>()
    val itemBinding = itemBindingOf<ConsoleItem>()
    lateinit var args: FlashFragmentArgs

    private val logItems = mutableListOf<String>().synchronized()
    private val outItems = object : CallbackList<String>() {
        override fun onAddElement(e: String?) {
            e ?: return
            items.add(ConsoleItem(e))
            logItems.add(e)
        }
    }

    fun startFlashing() {
        val (action, uri, id) = args
        if (id != -1)
            Notifications.mgr.cancel(id)

        viewModelScope.launch {
            val result = when (action) {
                Const.Value.FLASH_ZIP -> {
                    FlashZip(uri!!, outItems, logItems).exec()
                }
                Const.Value.UNINSTALL -> {
                    showReboot = false
                    MagiskInstaller.Uninstall(outItems, logItems).exec()
                }
                Const.Value.FLASH_MAGISK -> {
                    if (Info.isEmulator)
                        MagiskInstaller.Emulator(outItems, logItems).exec()
                    else
                        MagiskInstaller.Direct(outItems, logItems).exec()
                }
                Const.Value.FLASH_INACTIVE_SLOT -> {
                    MagiskInstaller.SecondSlot(outItems, logItems).exec()
                }
                Const.Value.PATCH_FILE -> {
                    uri ?: return@launch
                    showReboot = false
                    MagiskInstaller.Patch(uri, outItems, logItems).exec()
                }
                else -> {
                    back()
                    return@launch
                }
            }
            onResult(result)
        }
    }

    private fun onResult(success: Boolean) {
        state = if (success) State.LOADED else State.LOADING_FAILED
        when {
            success -> _subtitle.postValue(R.string.done)
            else -> _subtitle.postValue(R.string.failure)
        }
    }

    fun onMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> savePressed()
        }
        return true
    }

    private fun savePressed() = withExternalRW {
        viewModelScope.launch(Dispatchers.IO) {
            val name = "magisk_install_log_%s.log".format(now.toTime(timeFormatStandard))
            val file = MediaStoreUtils.getFile(name, true)
            file.uri.outputStream().bufferedWriter().use { writer ->
                logItems.forEach {
                    writer.write(it)
                    writer.newLine()
                }
            }
            SnackbarEvent(file.toString()).publish()
        }
    }

    fun restartPressed() = reboot()
}
