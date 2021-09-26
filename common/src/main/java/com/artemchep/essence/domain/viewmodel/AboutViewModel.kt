package com.artemchep.essence.domain.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.text.parseAsHtml
import com.artemchep.essence.domain.viewmodel.base.BaseViewModel
import com.artemchep.essence.messageChannel
import com.artemchep.mw_common.BuildConfig
import com.artemchep.mw_common.R
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @author Artem Chepurnoy
 */
class AboutViewModel(application: Application) : BaseViewModel(application) {

    val titleTextLive = kotlin.run {
        val appName = context.getString(R.string.app_name)
        val versionName = try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = info.versionName

            // Make the info part of version name a bit smaller
            if (versionName.indexOf('-') >= 0) {
                versionName.replaceFirst("-".toRegex(), "<small>-") + "</small>"
            } else versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "N/A"
        }

        val v = context.getString(R.string.about_title, appName, versionName).parseAsHtml()
        MutableStateFlow(v)
    }

    val contentTextLive = kotlin.run {
        val year = BuildConfig.MY_TIME_YEAR
        val v = context.getString(R.string.about_content, year).parseAsHtml()
        MutableStateFlow(v)
    }

    fun navigateTo(event: NavigationEvent) {
        when (event) {
            NavigationEvent.BUILD_INFO -> messageChannel.trySend(BuildConfig.MY_TIME)
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    enum class NavigationEvent {
        BUILD_INFO
    }

}
