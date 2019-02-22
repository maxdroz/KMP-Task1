package maxim.drozd.maximdrozd_task1.DB

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = [AppInfo::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var APPDATABASE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (APPDATABASE == null) {
                synchronized(AppDatabase::class) {
                    APPDATABASE = Room
                            .databaseBuilder(context.applicationContext, AppDatabase::class.java, "main.db")
                            .allowMainThreadQueries()
                            .build()
                }
            }
            return APPDATABASE!!
        }
    }
    public abstract fun appInfo(): AppInfoDAO
}