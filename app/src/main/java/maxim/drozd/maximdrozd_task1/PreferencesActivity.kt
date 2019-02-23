package maxim.drozd.maximdrozd_task1

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import com.yandex.metrica.YandexMetrica
import maxim.drozd.maximdrozd_task1.launcher.LauncherActivity

class PreferencesFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    val listenerObj = SharedPreferences.OnSharedPreferenceChangeListener(::listener)

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(listenerObj)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listenerObj)
        super.onPause()
    }

    private fun listener(sharedPreferences: SharedPreferences, s: String) {
        if (s == "preference_sort" || s == "preference_popular_type")
            LauncherActivity.sortMethodChanged = true
        LauncherActivity.somethingChanged = true
        if (s == "preference_popular_height") {
            if (sharedPreferences.getString("preference_popular_height", "2")!!.toInt() > 5)
                sharedPreferences.edit().putString("preference_popular_height", "4").apply()
        }

        Thread(Runnable {
            when(s){
                "preference_popular" -> {
                    val json = if(sharedPreferences.getBoolean("preference_popular", true)){
                        """{"status":"on"}"""

                    }else{
                        """{"status":"off"}"""
                    }
                    YandexMetrica.reportEvent("Event: Popular window status changed", json)
                }
                "preference_dark_theme" -> {
                    val json = if(sharedPreferences.getBoolean("preference_dark_theme", false))
                        """{"theme":"dark"}"""
                    else
                        """{"theme":"light"}"""
                    YandexMetrica.reportEvent("Event: Theme changed", json)
                }
                "preference_dense" -> {
                    val json = if(sharedPreferences.getBoolean("preference_dense", false))
                        """{"density":"dense"}"""
                    else
                        """{"density":"default"}"""
                    YandexMetrica.reportEvent("Event: Density changed", json)
                }
                "preference_sort" -> {
                    val json = when(sharedPreferences.getString("preference_sort", "0")){
                        "1" -> """{"method":"by date"}"""
                        "2" -> """{"method":"by alphabet a-z"}"""
                        "3" -> """{"method":"by alphabet z-a"}"""
                        "5" -> """{"method":"by frequency"}"""
                        else -> """{"method":"no sort"}"""
                    }
                    YandexMetrica.reportEvent("Event: Sort method changed", json)
                }
                "preference_welcome" -> {
                    YandexMetrica.reportEvent("Event: Show welcome screen on next launch")
                }
                "preference_popular_type" -> {
                    val json = if(sharedPreferences.getBoolean("preference_popular_type", false))
                        """{"type":"last launch"}"""
                    else
                        """{"type":"frequency"}"""
                    YandexMetrica.reportEvent("Event: Sort type in popular area changed", json)
                }
                "preference_popular_height" -> {
                    val value = sharedPreferences.getString("preference_popular_height", "2")!!.toInt()
                    val json = """{"value":"$value"}"""
                    YandexMetrica.reportEvent("Event: Popular's height changed", json)
                }
            }
        }).start()
        if (s == "preference_dark_theme")
            activity?.recreate()
    }
}

class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dark = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("preference_dark_theme", false)
        val themeId = if (dark) R.style.DarkAppThemeNoActionBar else R.style.AppThemeNoActionBar
        setTheme(themeId)

        setContentView(R.layout.preference_holder)
        supportActionBar?.title = resources.getString(R.string.settings)
    }


    override fun onResume() {
        super.onResume()
        YandexMetrica.resumeSession(this)
    }


    override fun onPause() {
        YandexMetrica.pauseSession(this)
        super.onPause()
    }
}