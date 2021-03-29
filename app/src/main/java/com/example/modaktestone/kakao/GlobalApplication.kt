package com.example.modaktestone.kakao

import android.app.Application
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class GlobalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // 다른 초기화 코드들

        // Kakao SDK 초기화
        KakaoSdk.init(this, "e55c02914e0d51f60e971b486733ed22")
        startKoin{
            androidContext(this@GlobalApplication)
            modules(listOf())
        }
    }
}