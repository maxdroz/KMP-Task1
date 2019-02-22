package maxim.drozd.maximdrozd_task1.welcome_pages

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_welcome_page3.*
import maxim.drozd.maximdrozd_task1.R
import maxim.drozd.maximdrozd_task1.launcher.LauncherActivity

class WelcomePageThirdFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val dark = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preference_dark_theme", false)
        val themeId = if (dark) R.style.DarkAppThemeNoActionBar else R.style.AppThemeNoActionBar

        val contextThemeWrapper = ContextThemeWrapper(activity, themeId)

        val localInflater = inflater.cloneInContext(contextThemeWrapper)

        return localInflater.inflate(R.layout.fragment_welcome_page3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        button3.setOnClickListener {
            val pager = activity?.findViewById<ViewPager>(R.id.welcome_view_pager)
            pager?.setCurrentItem(pager.currentItem + 1, true)
        }

        radioButton.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("preference_dark_theme", false).apply()
            LauncherActivity.somethingChanged = true
            activity?.recreate()
       }

        radioButton2.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("preference_dark_theme", true).apply()
            LauncherActivity.somethingChanged = true
            activity?.recreate()
        }

        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preference_dark_theme", false))
            radioButton.isChecked = true
        else
            radioButton2.isChecked = true
    }
}