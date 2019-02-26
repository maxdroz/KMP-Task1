package maxim.drozd.maximdrozd_task1.launcher

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import kotlinx.android.synthetic.main.desktop.*
import maxim.drozd.maximdrozd_task1.DB.AppDatabase
import maxim.drozd.maximdrozd_task1.DB.Position
import maxim.drozd.maximdrozd_task1.ImageLoaderService
import maxim.drozd.maximdrozd_task1.R
import maxim.drozd.maximdrozd_task1.SquareImage
import java.io.File
import java.util.*

class DesktopFragment: Fragment(){

    private val handlerThread = HandlerThread("thread2")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.desktop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!handlerThread.isAlive)
            handlerThread.start()

        val height = 6
        val width = 4
        val handler = Handler(handlerThread.looper)
        for(i in 0 until height){
            val row = TableRow(context)
            var param = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 0f)
            row.layoutParams = param

            for(j in 0 until width){
                val item = layoutInflater.inflate(R.layout.grid_layout_item, row, false)
                val rowParam = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
                if(j != 0 && j != width - 1){
                    val off = resources.getDimensionPixelOffset(R.dimen.offset)
                    rowParam.setMargins(off, 0,off, 0)
                }
                item.layoutParams = rowParam

                val image = item.findViewById<SquareImage>(R.id.square_image)
                val text = item.findViewById<TextView>(R.id.app_name)
                val r = Random()
                handler.post{
                    val desktopAppInfo = AppDatabase.getInstance(context!!).desktopAppInfo().getAppByPos(Position(i, j))
                    if(desktopAppInfo != null){
                        /*
                        * type
                        * 0 - site
                        * 1 - application
                        * 2 - contact
                        */

                        val img: Bitmap
                        if(desktopAppInfo.imagePath == ""){
                            img = when(desktopAppInfo.type){
                                0 -> {
                                    ImageLoaderService.loadBitmap("http://favicon.yandex.net/favicon/${desktopAppInfo.value}?size=32")?:
                                    BitmapFactory.decodeResource(resources, R.raw.default_web_icon)
                                }
                                1 -> {
                                    (LauncherActivity
                                            .newData
                                            ?.find {
                                                app -> desktopAppInfo.value == app.app.packageName
                                            }
                                            ?.drawable as? BitmapDrawable)
                                            ?.bitmap?:
                                    (context!!.packageManager.getApplicationIcon(desktopAppInfo.value)as BitmapDrawable).bitmap
                                }
                                else -> {
                                    //TODO
                                    Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888)
                                }
                            }
                            val f = File.createTempFile("icon", ".png")
                            img.compress(Bitmap.CompressFormat.PNG, 0, f.outputStream())
                            AppDatabase.getInstance(context!!).desktopAppInfo().setImagePath(Position(i, j), f.absolutePath)
                        }
                        else{
                            img = BitmapFactory.decodeFile(desktopAppInfo.imagePath)
                        }
                        image.background = BitmapDrawable(resources, img)
                        text.text = desktopAppInfo.text
                    }
                    item.setBackgroundColor(Color.rgb(r.nextInt(256), r.nextInt(256), r.nextInt(256)))
                    row.addView(item)
                }
            }
            table.addView(row)
            if(i != height - 1){
                val space = Space(context!!)
                param = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 1.0f)
                space.layoutParams = param
                space.setBackgroundColor(Color.rgb(0, 0, 0))
                table.addView(space)
            }
        }
    }
}