package com.artemchep.essence.domain.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.text.parseAsHtml
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.artemchep.essence.domain.viewmodel.base.BaseViewModel
import com.artemchep.essence.messageChannel
import com.artemchep.essence_common.BuildConfig
import com.artemchep.essence_common.R
import kotlinx.coroutines.launch

/**
 * @author Artem Chepurnoy
 */
class AboutViewModel(application: Application) : BaseViewModel(application) {

    val titleTextLive = object : MutableLiveData<CharSequence>() {
        init {
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

            value = context.getString(R.string.about_title, appName, versionName).parseAsHtml()
        }
    }

    val contentTextLive = object : MutableLiveData<CharSequence>() {
        init {
            val year = BuildConfig.MY_TIME_YEAR
            value = context.getString(R.string.about_content, year).parseAsHtml()
        }
    }

    fun navigateTo(event: NavigationEvent) {
        when (event) {
            NavigationEvent.BUILD_INFO -> launch {
                messageChannel.send(BuildConfig.MY_TIME)
            }
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    enum class NavigationEvent {
        BUILD_INFO
    }

}
