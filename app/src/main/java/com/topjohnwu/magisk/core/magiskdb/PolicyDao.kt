package com.brightsight.joker.core.magiskdb

import android.content.pm.PackageManager
import com.brightsight.joker.core.Const
import com.brightsight.joker.core.model.su.SuPolicy
import com.brightsight.joker.core.model.su.toMap
import com.brightsight.joker.core.model.su.toPolicy
import com.brightsight.joker.di.AppContext
import com.brightsight.joker.ktx.now
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit


class PolicyDao : BaseDao() {

    override val table: String = Table.POLICY

    suspend fun deleteOutdated() = buildQuery<Delete> {
        condition {
            greaterThan("until", "0")
            and {
                lessThan("until", TimeUnit.MILLISECONDS.toSeconds(now).toString())
            }
            or {
                lessThan("until", "0")
            }
        }
    }.commit()

    suspend fun delete(uid: Int) = buildQuery<Delete> {
        condition {
            equals("uid", uid)
        }
    }.commit()

    suspend fun fetch(uid: Int) = buildQuery<Select> {
        condition {
            equals("uid", uid)
        }
    }.query().first().toPolicyOrNull()

    suspend fun update(policy: SuPolicy) = buildQuery<Replace> {
        values(policy.toMap())
    }.commit()

    suspend fun <R: Any> fetchAll(mapper: (SuPolicy) -> R) = buildQuery<Select> {
        condition {
            equals("uid/100000", Const.USER_ID)
        }
    }.query {
        it.toPolicyOrNull()?.let(mapper)
    }

    private fun Map<String, String>.toPolicyOrNull(): SuPolicy? {
        return runCatching { toPolicy(AppContext.packageManager) }.getOrElse {
            Timber.e(it)
            if (it is PackageManager.NameNotFoundException) {
                val uid = getOrElse("uid") { null } ?: return null
                GlobalScope.launch {
                    delete(uid.toInt())
                }
            }
            null
        }
    }

}
