package com.artemchep.essence

import android.app.Application

/**
 * @author Artem Chepurnoy
 */
class Heart : Application() {

    override fun onCreate() {
        super.onCreate()
        Cfg.init(this)
    }

}