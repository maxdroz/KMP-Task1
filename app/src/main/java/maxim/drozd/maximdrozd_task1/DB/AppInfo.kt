package maxim.drozd.maximdrozd_task1.DB

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
// @Suppress
@Entity
data class AppInfo(
    @PrimaryKey(autoGenerate = false)
    var packageName: String = ""
) {
    var used: Boolean = false

    var timesLaunched: Int = 0

    var lastLaunched: Long = 0
}