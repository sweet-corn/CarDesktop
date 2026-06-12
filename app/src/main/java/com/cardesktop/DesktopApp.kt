package com.cardesktop

import android.app.Application
import com.cardesktop.service.MusicController
import com.cardesktop.service.MusicMetadataService
import com.cardesktop.service.VehicleService

class DesktopApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        MusicController.init(this)
        MusicMetadataService.init(this)
        VehicleService.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        MusicMetadataService.stop()
        VehicleService.stop()
    }

    companion object {
        lateinit var instance: DesktopApp
            private set
    }
}