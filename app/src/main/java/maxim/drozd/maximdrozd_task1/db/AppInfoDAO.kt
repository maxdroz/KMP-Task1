package maxim.drozd.maximdrozd_task1.db

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.IGNORE
import android.database.Cursor

@Dao
interface AppInfoDAO {

    @Query("SELECT timesLaunched from AppInfo WHERE packageName = :packageName")
    fun getTimesLaunchedApp(packageName: String): List<Int>

    @Query("UPDATE AppInfo SET timesLaunched = timesLaunched + 1 WHERE packageName = :packageName")
    fun updateLaunchCount(packageName: String)

    @Transaction
    fun updateAllAps(appInfos: List<AppInfo>) {
        insertAllApps(appInfos)
        updateUsed(appInfos.map { app -> app.packageName })
        deleteUnused()
        resetUsed()
    }

    @Transaction
    fun updateLaunch(packageName: String, time: Long) {
        updateLaunchCount(packageName)
        updateLaunchTime(time, packageName)
    }

    @Query("SELECT packageName, timesLaunched, MAX(lastLaunched) FROM AppInfo")
    fun getLastLaunchedApp(): Cursor

    @Query("SELECT * FROM AppInfo WHERE timesLaunched != 0")
    fun getLaunchedApps(): Cursor

    @Query("UPDATE AppInfo SET used = 1 WHERE packageName IN (:packageNames)")
    fun updateUsed(packageNames: List<String>)

    @Query("DELETE from AppInfo WHERE used = 0")
    fun deleteUnused()

    @Query("UPDATE AppInfo SET used = 0")
    fun resetUsed()

    @Insert(onConflict = IGNORE)
    fun insertAllApps(appInfos: List<AppInfo>)

    @Query("DELETE from AppInfo WHERE packageName = :packageName")
    fun deleteApp(packageName: String)

    @Query("UPDATE AppInfo SET lastLaunched = :time WHERE packageName = :packageName")
    fun updateLaunchTime(time: Long, packageName: String)

    @Query("SELECT lastLaunched FROM AppInfo WHERE packageName = :packageName")
    fun getLaunchedTime(packageName: String): Long

    @Update
    fun updateAppInfo(appInfo: AppInfo): Int
}