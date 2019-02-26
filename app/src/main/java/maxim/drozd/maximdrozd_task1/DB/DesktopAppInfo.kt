package maxim.drozd.maximdrozd_task1.DB

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverter

/*
* type
* 0 - site
* 1 - application
* 2 - contact
*/
@Entity(tableName = "desktop")
data class DesktopAppInfo(
        @PrimaryKey(autoGenerate = false)
        var pos: Position,
        var type: Int,
        var value: String,
        var text: String
){
    var used = false
    var imagePath: String = ""
}

data class Position(var h: Int, var w: Int)

class PositionConverter{
    @TypeConverter
    fun fromPosition(p: Position): String{
        return "${p.h} ${p.w}"
    }

    @TypeConverter
    fun toPosition(p: String): Position{
        val pos = p.indexOf(' ')
        val h = p.substring(0, pos).toInt()
        val w = p.substring(pos +1 ).toInt()
        return Position(h, w)
    }
}