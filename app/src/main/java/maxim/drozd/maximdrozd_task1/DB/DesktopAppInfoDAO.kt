package maxim.drozd.maximdrozd_task1.DB

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE

@Dao
interface DesktopAppInfoDAO{

    @Transaction
    fun updateAllApps(packageNames: List<String>){
        updateUsed(packageNames)
        deleteUnused()
        resetUsed()
    }


    @Query("UPDATE desktop SET used = 1 WHERE type = 1 AND value IN (:packageNames)")
    fun updateUsed(packageNames: List<String>)

    @Query("DELETE FROM desktop WHERE used = 0 AND type = 1")
    fun deleteUnused()

    @Query("UPDATE desktop SET used = 0")
    fun resetUsed()

    @Query("SELECT * FROM desktop WHERE pos = :pos")
    fun getAppByPos(pos: Position): DesktopAppInfo?

    @Query("UPDATE desktop SET imagePath = :path WHERE pos = :pos")
    fun setImagePath(pos: Position, path: String)

    @Insert(onConflict = REPLACE)
    fun insertAll(vararg infos: DesktopAppInfo)
}