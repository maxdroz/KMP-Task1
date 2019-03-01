package maxim.drozd.maximdrozd_task1

import android.content.Context
import android.content.Intent.getIntent
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.preference.Preference
import android.util.AttributeSet
import android.widget.Toast

class BroadcastPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs), Preference.OnPreferenceClickListener {

    init {
        this.onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        Toast.makeText(context, context.getString(R.string.wait), Toast.LENGTH_LONG).show()
        context.sendBroadcast(intent)
        return true
    }
}