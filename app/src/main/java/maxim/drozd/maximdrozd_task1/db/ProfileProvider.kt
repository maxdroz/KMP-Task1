package maxim.drozd.maximdrozd_task1.db

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri

class ProfileProvider: ContentProvider(){

    companion object {

        private const val TABLE_NAME = "ProfileInfo"
        private const val AUTHORITY = "maxim.drozd.maximdrozd_task2"

        private const val CODE_READ = 1

        private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH)

        init{
            URI_MATCHER.addURI(AUTHORITY, "$TABLE_NAME/profile", CODE_READ)
        }
    }

    private val cursor = MatrixCursor(arrayOf("_id", "mobile_number", "home_number", "github", "vk", "main_email", "second_email", "work_address"))

    init {
        cursor.addRow(arrayOf(0L, "(44) 77-33-782", "(17) 265-67-26", "Kamerton12", "vk.com/maxdroz", "s312@tut.by", "s312maxm@gmail.com", "prospect Dzerzhinskogo 5, Minsk"))
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw IllegalArgumentException("Illegal operation URI: $uri")
    }

    override fun query(uri: Uri,
                       projection: Array<String>?,
                       selection: String?,
                       selectionArgs: Array<String>?,
                       sortOrder: String?): Cursor? = cursor

    override fun onCreate(): Boolean = true

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw IllegalArgumentException("Illegal operation URI: $uri")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw IllegalArgumentException("Illegal operation URI: $uri")
    }

    override fun getType(uri: Uri): String? {
        throw IllegalArgumentException("Illegal operation URI: $uri")
    }

}