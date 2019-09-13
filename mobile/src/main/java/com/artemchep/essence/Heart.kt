package com.artemchep.essence

import android.app.Application
import com.artemchep.essence.sync.CfgDataClientAdapter

/**
 * @author Artem Chepurnoy
 */
class Heart : Application() {

    private val cfgSyncObserver = CfgDataClientAdapter(this)

    override fun onCreate() {
        super.onCreate()
        Cfg.init(this)
        Cfg.observe(cfgSyncObserver)
    }

}