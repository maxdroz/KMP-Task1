package maxim.drozd.maximdrozd_task1.welcome_pages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.yandex.metrica.YandexMetrica
import kotlinx.android.synthetic.main.activity_welcome_page.*
import maxim.drozd.maximdrozd_task1.R

class WelcomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dark = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("preference_dark_theme", false)
        val themeId = if (dark) R.style.DarkAppTheme else R.style.AppTheme
        setTheme(themeId)

        setContentView(R.layout.activity_welcome_page)
        welcome_view_pager.adapter = WelcomeActivityFragmentPageAdaptor(supportFragmentManager)
        tab_layout.setupWithViewPager(welcome_view_pager)

        YandexMetrica.reportEvent("Event: Welcome activity started")
    }
}
