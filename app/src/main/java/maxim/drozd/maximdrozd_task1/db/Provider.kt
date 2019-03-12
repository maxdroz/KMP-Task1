package maxim.drozd.maximdrozd_task1.db

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.content.UriMatcher


class Provider: ContentProvider(){

    companion object {
        const val TABLE_NAME = "AppInfo"
        const val AUTHORITY = "maxim.drozd.maximdrozd_task1"

        private const val CODE_LAST_LAUNCHED_APP = 1
        private const val CODE_LAUNCHED_APPS = 2
        private const val CODE_UPDATE_APP = 3

        private val URI = Uri.parse("content://$AUTHORITY/$TABLE_NAME")
        private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH)

        init {
            URI_MATCHER.addURI(AUTHORITY, "$TABLE_NAME/last_launched", CODE_LAST_LAUNCHED_APP)
            URI_MATCHER.addURI(AUTHORITY, "$TABLE_NAME/launched", CODE_LAUNCHED_APPS)
            URI_MATCHER.addURI(AUTHORITY, "$TABLE_NAME/update/*", CODE_UPDATE_APP)
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw IllegalArgumentException("Illegal operation URI: $uri")
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val code = URI_MATCHER.match(uri)
        if(code == CODE_LAST_LAUNCHED_APP || code == CODE_LAUNCHED_APPS) {
            if(context == null)
                return null
            val appInfo = AppDatabase.getInstance(context!!).appInfo()
            val cursor = when (code) {
                CODE_LAST_LAUNCHED_APP -> appInfo.getLastLaunchedApp()
                else -> appInfo.getLaunchedApps()
            }
            cursor.setNotificationUri(context!!.contentResolver, uri)
            return cursor
        } else if(code == CODE_UPDATE_APP) {
            throw IllegalArgumentException("Cannot get item by id: $uri")
        } else {
            throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun onCreate(): Boolean = true

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        when(URI_MATCHER.match(uri)){
            CODE_LAST_LAUNCHED_APP, CODE_LAUNCHED_APPS -> {
                throw IllegalArgumentException("Cannot update this fields: $uri")
            }
            CODE_UPDATE_APP -> {
                if(context == null)
                    return 0
                val appInfo = AppInfo.fromContentValues(values)
                val packageName = uri.toString().substring(uri.toString().lastIndexOf('/') + 1)
                appInfo.packageName = packageName
                val count = AppDatabase.getInstance(context!!).appInfo().updateAppInfo(appInfo)
                context!!.contentResolver.notifyChange(uri, null)
                return count
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw IllegalArgumentException("Illegal operation URI: $uri")
    }

    override fun getType(uri: Uri): String? {
        throw IllegalArgumentException("Illegal operation URI: $uri")
    }


}