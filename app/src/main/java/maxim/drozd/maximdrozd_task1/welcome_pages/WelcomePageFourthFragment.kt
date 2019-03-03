package maxim.drozd.maximdrozd_task1.welcome_pages

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_welcome_page4.*
import maxim.drozd.maximdrozd_task1.R
import maxim.drozd.maximdrozd_task1.launcher.LauncherActivity

class WelcomePageFourthFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome_page4, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        button4.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("preference_welcome", false).apply()
            activity?.finish()
        }

        fun click1(v: View?) {
            v?.tag
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("preference_dense", false).apply()
            radioButton.isChecked = true
            radioButton2.isChecked = false
            LauncherActivity.somethingChanged = true
        }

        fun click2(v: View?) {
            v?.tag
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("preference_dense", true).apply()
            radioButton.isChecked = false
            radioButton2.isChecked = true
            LauncherActivity.somethingChanged = true
        }

        clickable_first.setOnClickListener(::click1)
        clickable_second.setOnClickListener(::click2)
        radioButton.setOnClickListener(::click1)
        radioButton2.setOnClickListener(::click2)

        if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preference_dense", false))
            click1(null)
        else
            click2(null)
    }
}