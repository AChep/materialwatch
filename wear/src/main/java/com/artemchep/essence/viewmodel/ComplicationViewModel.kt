package com.artemchep.essence.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.artemchep.essence.domain.models.Event
import com.artemchep.essence.domain.viewmodel.base.BaseViewModel
import com.artemchep.essence.live.ScreenComplicationsLiveData
import com.artemchep.essence.ui.model.ConfigItem

/**
 * @author Artem Chepurnoy
 */
class ComplicationViewModel(application: Application) : BaseViewModel(application) {

    val screenLiveData = ScreenComplicationsLiveData(context)

    val showProviderForComplicationEvent = MutableLiveData<Event<Int>>()

    fun updateComplications() {
        screenLiveData.updateComplications()
    }

    fun showProviderForComplication(item: ConfigItem) {
        val event = Event(item.id)
        showProviderForComplicationEvent.postValue(event)
    }

}
