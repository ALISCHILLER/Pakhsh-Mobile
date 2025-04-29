package com.msa.core.config



/**
 * Interface برای تنظیمات اپلیکیشن.
 */
interface AppConfig {
    val sharedPreferencesName: String
    val databaseName: String
}


//نحوه استفاده

//import com.example.core.config.AppConfig
//
//object App1Config : AppConfig {
//    override val sharedPreferencesName: String = "app1_prefs"
//    override val databaseName: String = "app1_database"
//}


//class MainApplication : Application() {
//
//    override fun onCreate() {
//        super.onCreate()
//
//        startKoin {
//            androidContext(this@MainApplication)
//            modules(
//                storageModule, // ثبت ماژول ذخیره‌سازی
//                module {
//                    single<AppConfig> { App1Config }
//                }
//            )
//        }
//    }
//}