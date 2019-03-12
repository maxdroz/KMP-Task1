package maxim.drozd.maximdrozd_task1

import android.content.Context
import android.content.Intent.getIntent
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.preference.Preference
import android.util.AttributeSet
import android.widget.Toast
import maxim.drozd.maximdrozd_task1.launcher.LauncherActivity

class BroadcastPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs), Preference.OnPreferenceClickListener {

    init {
        this.onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if(!LauncherActivity.updatingBackground){
            Toast.makeText(context, R.string.wait, Toast.LENGTH_LONG).show()
            context.sendBroadcast(intent)
        } else {
            Toast.makeText(context, R.string.already_updating, Toast.LENGTH_LONG).show()
        }
        return true
    }
}