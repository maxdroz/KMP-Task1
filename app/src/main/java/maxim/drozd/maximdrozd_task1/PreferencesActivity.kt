package maxim.drozd.maximdrozd_task1

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
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
        if (s == "preference_dark_theme")
            activity?.recreate()
    }
}

class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dark = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("preference_dark_theme", false)
        val themeId = if (dark) R.style.DarkAppTheme else R.style.AppTheme
        setTheme(themeId)

        setContentView(R.layout.preference_holder)
        supportActionBar?.title = resources.getString(R.string.settings)
    }
}