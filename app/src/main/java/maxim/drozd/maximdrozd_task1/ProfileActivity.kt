package maxim.drozd.maximdrozd_task1

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.push.YandexMetricaPush
import kotlinx.android.synthetic.main.profile_content.*
import kotlinx.android.synthetic.main.profile_layout.*
import java.lang.Exception

class ProfileActivity : AppCompatActivity() {

    companion object {
        const val FILTER_RECIEVER = "maxim.drozd.maximdrozd_task1.silentPushFilter"
        const val PREFERENCES_TEXT = "maxim.drozd.maximdrozd_task1.preference_edit"
    }


    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("Shad", "in main secondary receiver")
            val payload = intent?.getStringExtra(YandexMetricaPush.EXTRA_PAYLOAD)
            silentPushText.text = payload
            Log.i("Shad", payload)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dark = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("preference_dark_theme", false)
        val themeId = if (dark) R.style.DarkAppThemeNoActionBar else R.style.AppThemeNoActionBarProfile
        setTheme(themeId)

        setContentView(R.layout.profile_layout)

        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val name = resources.getString(R.string.name)
        supportActionBar?.title = name

        Thread(Runnable {
            silentPushText.text = PreferenceManager.getDefaultSharedPreferences(this).getString(PREFERENCES_TEXT, "Here will be text")
        }).start()

        callMobile.setOnClickListener {
            YandexMetrica.reportEvent("Event: Mobile phone clicked")
            val number = Uri.parse("tel:+375447733782")
            val dial = Intent(Intent.ACTION_DIAL, number)
            startActivity(dial)
        }

        callHome.setOnClickListener {
            YandexMetrica.reportEvent("Event: Home phone clicked")
            val number = Uri.parse("tel:80172656726")
            val dial = Intent(Intent.ACTION_DIAL, number)
            startActivity(dial)
        }

        goToGithub.setOnClickListener {
            YandexMetrica.reportEvent("Event: Github link opened")
            val siteUrl = Uri.parse("http://www.github.com/Kamerton12")
            val site = Intent(Intent.ACTION_VIEW, siteUrl)
            startActivity(site)
        }

        goToVk.setOnClickListener {
            YandexMetrica.reportEvent("Event: Vk link opened")
            val siteUrl = Uri.parse("http://www.vk.com/maxdroz")
            val site = Intent(Intent.ACTION_VIEW, siteUrl)
            startActivity(site)
        }

        sendEmail1.setOnClickListener {
            YandexMetrica.reportEvent("Event: Main mail address opened")
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:s312@tut.by")
            startActivity(emailIntent)
        }

        sendEmail2.setOnClickListener {
            YandexMetrica.reportEvent("Event: Main mail address opened")
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:s312maxm@gmail.com")
            startActivity(emailIntent)
        }

        goToMap.setOnClickListener {
            YandexMetrica.reportEvent("Event: Map opened")
            val intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=Prospekt Dzerzhinskogo 5, Minsk"))
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        YandexMetrica.resumeSession(this)

        val filter = IntentFilter(FILTER_RECIEVER)
        try{
            registerReceiver(receiver, filter)
        } catch (e: Exception){
        }
    }


    override fun onPause() {
        try {
            unregisterReceiver(receiver)
        } catch (e: Exception){
        }
        YandexMetrica.pauseSession(this)
        super.onPause()
    }
}