package com.example.focusable

import android.app.Application
import com.example.focusable.data.di.dataModule
import com.example.focusable.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FocusableApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FocusableApplication)
            modules(dataModule, appModule)
        }
    }
}
