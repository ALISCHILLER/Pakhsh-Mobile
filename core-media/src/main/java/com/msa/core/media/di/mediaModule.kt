package com.zar.core.media.di

import com.zar.core.media.camera.CameraHelper
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val mediaModule = module {
    single { CameraHelper(androidContext()) }
}