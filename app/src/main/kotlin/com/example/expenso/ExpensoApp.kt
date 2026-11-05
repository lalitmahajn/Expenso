package com.example.expenso

import android.app.Application
import com.example.expenso.di.AppContainer
import com.example.expenso.di.DefaultAppContainer

class ExpensoApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
