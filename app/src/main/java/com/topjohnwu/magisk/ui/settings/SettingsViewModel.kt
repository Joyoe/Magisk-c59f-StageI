package com.brightsight.joker.ui.settings

import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.viewModelScope
import com.brightsight.joker.BR
import com.brightsight.joker.BuildConfig
import com.brightsight.joker.R
import com.brightsight.joker.arch.BaseViewModel
import com.brightsight.joker.arch.adapterOf
import com.brightsight.joker.arch.diffListOf
import com.brightsight.joker.arch.itemBindingOf
import com.brightsight.joker.core.Const
import com.brightsight.joker.core.Info
import com.brightsight.joker.core.isRunningAsStub
import com.brightsight.joker.core.tasks.HideAPK
import com.brightsight.joker.data.database.RepoDao
import com.brightsight.joker.di.AppContext
import com.brightsight.joker.events.AddHomeIconEvent
import com.brightsight.joker.events.RecreateEvent
import com.brightsight.joker.events.dialog.BiometricEvent
import com.brightsight.joker.ktx.activity
import com.brightsight.joker.utils.Utils
import com.brightsight.superuser.Shell
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repositoryDao: RepoDao
) : BaseViewModel(), BaseSettingsItem.Callback {

    val adapter = adapterOf<BaseSettingsItem>()
    val itemBinding = itemBindingOf<BaseSettingsItem> { it.bindExtra(BR.callback, this) }
    val items = diffListOf(createItems())

    init {
        viewModelScope.launch {
            Language.loadLanguages(this)
        }
    }

    private fun createItems(): List<BaseSettingsItem> {
        val context = AppContext
        val hidden = context.packageName != BuildConfig.APPLICATION_ID

        // Customization
        val list = mutableListOf(
            Customization,
            Theme, Language
        )
        if (isRunningAsStub && ShortcutManagerCompat.isRequestPinShortcutSupported(context))
            list.add(AddShortcut)

        // Manager
        list.addAll(listOf(
            AppSettings,
            UpdateChannel, UpdateChannelUrl, DoHToggle, UpdateChecker, DownloadPath
        ))
        if (Info.env.isActive) {
            list.add(ClearRepoCache)
            if (Const.USER_ID == 0) {
                if (hidden)
                    list.add(Restore)
                else if (Info.isConnected.get())
                    list.add(Hide)
            }
        }

        // Magisk
        if (Info.env.isActive) {
            list.addAll(listOf(
                Magisk,
                JokerHide, SystemlessHosts
            ))
        }

        // Superuser
        if (Utils.showSuperUser()) {
            list.addAll(listOf(
                Superuser,
                Tapjack, Biometrics, AccessMode, MultiuserMode, MountNamespaceMode,
                AutomaticResponse, RequestTimeout, SUNotification
            ))
            if (Build.VERSION.SDK_INT < 23) {
                // Biometric is only available on 6.0+
                list.remove(Biometrics)
            }
            if (Build.VERSION.SDK_INT < 26) {
                // Re-authenticate is not feasible on 8.0+
                list.add(Reauthenticate)
            }
        }

        return list
    }

    override fun onItemPressed(view: View, item: BaseSettingsItem, callback: () -> Unit) = when (item) {
        is DownloadPath -> withExternalRW(callback)
        is Biometrics -> authenticate(callback)
        is Theme -> SettingsFragmentDirections.actionSettingsFragmentToThemeFragment().navigate()
        is ClearRepoCache -> clearRepoCache()
        is SystemlessHosts -> createHosts()
        is Restore -> HideAPK.restore(view.activity)
        is AddShortcut -> AddHomeIconEvent().publish()
        else -> callback()
    }

    override fun onItemChanged(view: View, item: BaseSettingsItem) {
        when (item) {
            is Language -> RecreateEvent().publish()
            is UpdateChannel -> openUrlIfNecessary(view)
            is Hide -> viewModelScope.launch { HideAPK.hide(view.activity, item.value) }
            else -> Unit
        }
    }

    private fun openUrlIfNecessary(view: View) {
        UpdateChannelUrl.refresh()
        if (UpdateChannelUrl.isEnabled && UpdateChannelUrl.value.isBlank()) {
            UpdateChannelUrl.onPressed(view, this)
        }
    }

    private fun authenticate(callback: () -> Unit) {
        BiometricEvent {
            // allow the change on success
            onSuccess { callback() }
        }.publish()
    }

    private fun clearRepoCache() {
        viewModelScope.launch {
            repositoryDao.clear()
            Utils.toast(R.string.repo_cache_cleared, Toast.LENGTH_SHORT)
        }
    }

    private fun createHosts() {
        Shell.jojo("add_hosts_module").submit {
            Utils.toast(R.string.settings_hosts_toast, Toast.LENGTH_SHORT)
        }
    }
}
