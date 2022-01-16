package com.brightsight.joker.core.download

import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import com.brightsight.joker.core.Info
import com.brightsight.joker.core.model.MagiskJson
import com.brightsight.joker.core.model.StubJson
import com.brightsight.joker.core.model.module.OnlineModule
import com.brightsight.joker.core.utils.MediaStoreUtils
import com.brightsight.joker.di.AppContext
import com.brightsight.joker.ktx.cachedFile
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

private fun cachedFile(name: String) = AppContext.cachedFile(name).apply { delete() }.toUri()

sealed class Subject : Parcelable {

    abstract val url: String
    abstract val file: Uri
    abstract val action: Action
    abstract val title: String

    @Parcelize
    class Module(
        val module: OnlineModule,
        override val action: Action
    ) : Subject() {
        override val url: String get() = module.zip_url
        override val title: String get() = module.downloadFilename

        @IgnoredOnParcel
        override val file by lazy {
            MediaStoreUtils.getFile(title).uri
        }
    }

    @Parcelize
    class Manager(
        private val json: MagiskJson = Info.remote.magisk,
        val stub: StubJson = Info.remote.stub
    ) : Subject() {
        override val action get() = Action.Download
        override val title: String get() = "Magisk-${json.version}(${json.versionCode})"
        override val url: String get() = json.link

        @IgnoredOnParcel
        override val file by lazy {
            cachedFile("manager.apk")
        }

        val externalFile get() = MediaStoreUtils.getFile("$title.apk").uri
    }
}

sealed class Action : Parcelable {
    @Parcelize
    object Flash : Action()

    @Parcelize
    object Download : Action()
}
