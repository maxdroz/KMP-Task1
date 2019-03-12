package maxim.drozd.maximdrozd_task1.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.R.id
import android.content.ContentValues


@Entity
data class AppInfo(
    @PrimaryKey(autoGenerate = false)
    var packageName: String = ""
) {
    var used: Boolean = false

    var timesLaunched: Int = 0

    var lastLaunched: Long = 0
    companion object {
        fun fromContentValues(values: ContentValues?): AppInfo {
            val appInfo = AppInfo()
            if(values == null)
                return appInfo
            if (values.containsKey("timesLaunched")) {
                appInfo.timesLaunched = values.getAsInteger("timesLaunched")
            }
            if (values.containsKey("lastLaunched")) {
                appInfo.lastLaunched = values.getAsLong("lastLaunched")
            }
            return appInfo
        }

    }

}