package maxim.drozd.maximdrozd_task1

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v7.preference.PreferenceManager
import android.util.Log
import com.yandex.metrica.push.YandexMetricaPush



class SilentPushReceiver: BroadcastReceiver(){
    @SuppressLint("ApplySharedPref")
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("Shad", "in main silent receiver")
        val payload = intent?.getStringExtra(YandexMetricaPush.EXTRA_PAYLOAD)
        Thread(Runnable {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(ProfileActivity.PREFERENCES_TEXT, payload).commit()
            val intent2 = Intent(ProfileActivity.FILTER_RECIEVER)
            intent2.putExtra(YandexMetricaPush.EXTRA_PAYLOAD, payload)
            context?.sendBroadcast(intent2)
        }).start()
        Log.i("Shad", payload)
    }


}