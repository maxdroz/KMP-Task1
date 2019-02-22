package maxim.drozd.maximdrozd_task1

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.profile_content.*
import kotlinx.android.synthetic.main.profile_layout.*

class ProfileActivity : AppCompatActivity() {
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

        callMobile.setOnClickListener {
            val number = Uri.parse("tel:+375447733782")
            val dial = Intent(Intent.ACTION_DIAL, number)
            startActivity(dial)
        }

        callHome.setOnClickListener {
            val number = Uri.parse("tel:80172656726")
            val dial = Intent(Intent.ACTION_DIAL, number)
            startActivity(dial)
        }

        goToGithub.setOnClickListener {
            val siteUrl = Uri.parse("http://www.github.com/Kamerton12")
            val site = Intent(Intent.ACTION_VIEW, siteUrl)
            startActivity(site)
        }

        goToVk.setOnClickListener {
            val siteUrl = Uri.parse("http://www.vk.com/maxdroz")
            val site = Intent(Intent.ACTION_VIEW, siteUrl)
            startActivity(site)
        }

        sendEmail1.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:s312@tut.by")
            startActivity(emailIntent)
        }

        sendEmail2.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:s312maxm@gmail.com")
            startActivity(emailIntent)
        }

        goToMap.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=Prospekt Dzerzhinskogo 5, Minsk"))
            startActivity(intent)
        }
    }
}