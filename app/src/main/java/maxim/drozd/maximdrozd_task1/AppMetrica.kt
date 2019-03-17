package maxim.drozd.maximdrozd_task1

import android.app.Application
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import com.microsoft.appcenter.distribute.Distribute
import com.microsoft.appcenter.crashes.Crashes
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.AppCenter
import android.R
import com.crashlytics.android.Crashlytics
import com.yandex.metrica.push.YandexMetricaPush
import io.fabric.sdk.android.Fabric





class AppMetrica: Application() {

    val API_key = "a8d7fa5d-4774-4fbc-8f50-c2b91a612626"

    override fun onCreate() {
        super.onCreate()

        Fabric.with(this, Crashlytics())

        AppCenter.start(this, "537ba06c-9f74-4109-bfa7-18ca47970105", Analytics::class.java, Crashes::class.java, Distribute::class.java)


        val config = YandexMetricaConfig.newConfigBuilder(API_key).build()
        // Инициализация AppMetrica SDK.
        YandexMetrica.activate(applicationContext, config)

        YandexMetricaPush.init(applicationContext)

        // Отслеживание активности пользователей.
        YandexMetrica.enableActivityAutoTracking(this)
    }

}