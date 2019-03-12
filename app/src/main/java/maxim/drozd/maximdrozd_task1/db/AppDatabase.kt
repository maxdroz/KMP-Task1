package maxim.drozd.maximdrozd_task1.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context

@Database(entities = [AppInfo::class, DesktopAppInfo::class], version = 1)
@TypeConverters(PositionConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var APPDATABASE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (APPDATABASE == null) {
                synchronized(AppDatabase::class) {
                    APPDATABASE = Room
                            .databaseBuilder(context.applicationContext, AppDatabase::class.java, "main.db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build()
                }
            }
            return APPDATABASE!!
        }
    }
    abstract fun appInfo(): AppInfoDAO
    abstract fun desktopAppInfo(): DesktopAppInfoDAO
}