package com.brightsight.joker.di

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.room.Room
import com.brightsight.joker.core.Const
import com.brightsight.joker.core.magiskdb.PolicyDao
import com.brightsight.joker.core.magiskdb.SettingsDao
import com.brightsight.joker.core.magiskdb.StringDao
import com.brightsight.joker.core.tasks.RepoUpdater
import com.brightsight.joker.data.database.RepoDatabase
import com.brightsight.joker.data.database.SuLogDatabase
import com.brightsight.joker.data.repository.LogRepository
import com.brightsight.joker.data.repository.NetworkService
import com.brightsight.joker.ktx.deviceProtectedContext
import com.brightsight.joker.ui.home.HomeViewModel
import com.brightsight.joker.ui.install.InstallViewModel
import com.brightsight.joker.ui.log.LogViewModel
import com.brightsight.joker.ui.module.ModuleViewModel
import com.brightsight.joker.ui.settings.SettingsViewModel
import com.brightsight.joker.ui.superuser.SuperuserViewModel
import com.brightsight.joker.ui.surequest.SuRequestViewModel

val AppContext: Context inline get() = ServiceLocator.context

@SuppressLint("StaticFieldLeak")
object ServiceLocator {

    lateinit var context: Context
    val deContext by lazy { context.deviceProtectedContext }
    val timeoutPrefs by lazy { deContext.getSharedPreferences("su_timeout", 0) }

    // Database
    val policyDB = PolicyDao()
    val settingsDB = SettingsDao()
    val stringDB = StringDao()
    val repoDB by lazy { createRepoDatabase(context).repoDao() }
    val sulogDB by lazy { createSuLogDatabase(deContext).suLogDao() }
    val repoUpdater by lazy { RepoUpdater(networkService, repoDB) }
    val logRepo by lazy { LogRepository(sulogDB) }

    // Networking
    val okhttp by lazy { createOkHttpClient(context) }
    val retrofit by lazy { createRetrofit(okhttp) }
    val markwon by lazy { createMarkwon(context, okhttp) }
    val networkService by lazy {
        NetworkService(
            createApiService(retrofit, Const.Url.GITHUB_PAGE_URL),
            createApiService(retrofit, Const.Url.GITHUB_RAW_URL),
            createApiService(retrofit, Const.Url.JS_DELIVR_URL),
            createApiService(retrofit, Const.Url.GITHUB_API_URL)
        )
    }

    object VMFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(clz: Class<T>): T {
            return when (clz) {
                HomeViewModel::class.java -> HomeViewModel(networkService)
                LogViewModel::class.java -> LogViewModel(logRepo)
                ModuleViewModel::class.java -> ModuleViewModel(repoDB, repoUpdater)
                SettingsViewModel::class.java -> SettingsViewModel(repoDB)
                SuperuserViewModel::class.java -> SuperuserViewModel(policyDB)
                InstallViewModel::class.java -> InstallViewModel(networkService)
                SuRequestViewModel::class.java -> SuRequestViewModel(policyDB, timeoutPrefs)
                else -> clz.newInstance()
            } as T
        }
    }
}

inline fun <reified VM : ViewModel> ViewModelStoreOwner.viewModel() =
    lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, ServiceLocator.VMFactory).get(VM::class.java)
    }

private fun createRepoDatabase(context: Context) =
    Room.databaseBuilder(context, RepoDatabase::class.java, "repo.db")
        .fallbackToDestructiveMigration()
        .build()

private fun createSuLogDatabase(context: Context) =
    Room.databaseBuilder(context, SuLogDatabase::class.java, "sulogs.db")
        .fallbackToDestructiveMigration()
        .build()
