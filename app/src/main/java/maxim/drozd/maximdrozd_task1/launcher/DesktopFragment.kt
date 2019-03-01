package maxim.drozd.maximdrozd_task1.launcher

import android.app.Activity
import android.content.ClipData
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.opengl.Visibility
import android.os.*
import android.preference.PreferenceManager
import android.provider.ContactsContract
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.ImageViewCompat
import android.util.Log
import android.view.*
import android.widget.*
import kotlinx.android.synthetic.main.desktop.*
import maxim.drozd.maximdrozd_task1.*
import maxim.drozd.maximdrozd_task1.DB.AppDatabase
import maxim.drozd.maximdrozd_task1.DB.DesktopAppInfo
import maxim.drozd.maximdrozd_task1.DB.Position
import maxim.drozd.maximdrozd_task1.DB.PositionConverter
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.StringBuilder
import java.net.URI
import java.net.URL
import java.util.*

class DesktopFragment: Fragment(){

    private val handlerThread = HandlerThread("thread2")

    var height = 5
    var width = 4

    var contactPos: Position? = null

    var active: Position? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.desktop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(!handlerThread.isAlive)
            handlerThread.start()

        //useless dummy draglistener to fix a bug
        empty.setOnDragListener{ _: View, dragEvent: DragEvent ->

            val action = dragEvent.action
            val dropView = dragEvent.localState as View
            val dropImage = dropView.findViewById<ImageView>(R.id.square_image)
            val dropText = dropView.findViewById<TextView>(R.id.app_name)

            val image = view.findViewById<ImageView>(R.id.square_image)
            val text = view.findViewById<TextView>(R.id.app_name)

            when(action){
                DragEvent.ACTION_DRAG_STARTED -> {
                    return@setOnDragListener true
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    image.visibility = View.VISIBLE
                    image.setImageDrawable(dropImage.drawable)
                    text.text = dropText.text
                    return@setOnDragListener true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    image.visibility = View.INVISIBLE
                    image.setImageResource(R.drawable.default_web_icon)
                    text.text = ""
                    view.setBackgroundResource(R.drawable.app_selector)
                    return@setOnDragListener true
                }
                DragEvent.ACTION_DROP -> {
                    view.setBackgroundResource(R.drawable.app_selector)
                    active = Position(0, 0)
                    Thread(Runnable {
                        AppDatabase.getInstance(context!!).desktopAppInfo().migrate(PositionConverter().toPosition(dropView.tag as String), Position(0, 0))
                        update()
                    }).start()
                    return@setOnDragListener true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    dropView.post {
                        dropView.visibility = View.VISIBLE
                    }
                    return@setOnDragListener true
                }
            }

            return@setOnDragListener false
        }

        val orientation = resources.configuration.orientation

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            height = 3
            width = 7
        } else {
            height = 5
            width = 4
        }


            val handler = Handler(handlerThread.looper)
        for(i in 0 until height){
            val row = TableRow(context)
            var param = TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT, 0f)
            row.layoutParams = param

            for(j in 0 until width){
                val item = layoutInflater.inflate(R.layout.table_layout_item, row, false)
                val rowParam = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
                if(j != 0){
                    val off = resources.getDimensionPixelOffset(R.dimen.offset)
                    rowParam.setMargins(off, 0, 0, 0)
                }
                item.layoutParams = rowParam

                val image = item.findViewById<SquareImage>(R.id.square_image)
                val text = item.findViewById<TextView>(R.id.app_name)
                handler.post{
                    val desktopAppInfo = AppDatabase.getInstance(context!!).desktopAppInfo().getAppByPos(Position(i, j))
                    if(desktopAppInfo != null){
                        val img = getImage(desktopAppInfo)
                        Handler(Looper.getMainLooper()).post {
                            image.setImageBitmap(img)
                            text.text = desktopAppInfo.text
                            bindView(item, desktopAppInfo)
                        }
                    }
                    else{
                        Handler(Looper.getMainLooper()).post {
                            image.setImageResource(R.drawable.default_web_icon)
                            image.visibility = View.INVISIBLE
                            bindEmpty(item, i, j)
                        }
                    }
                }
                row.addView(item)
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



    private fun bindView(view: View, info: DesktopAppInfo){
        view.setOnDragListener(null)
        view.tag = PositionConverter().fromPosition(info.pos)
        view.setOnLongClickListener {
            active = info.pos
            val data = ClipData.newPlainText("", "")
            val shadowBuilder = View.DragShadowBuilder(view)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(data, shadowBuilder, view, 0)
            } else {
                @Suppress("DEPRECATION")
                view.startDrag(data, shadowBuilder, view, 0)
            }

            view.visibility = View.INVISIBLE

            Log.i("Shad", "active set")
            Snackbar.make(view, getString(R.string.delete_icon), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.yes)){
                        Thread(Runnable {
                            File(info.imagePath).delete()
                            AppDatabase.getInstance(context!!).desktopAppInfo().deletePos(active!!)
                            update()
                        }).start()
                    }.show()
            return@setOnLongClickListener true
        }

        view.setOnClickListener {
            when(info.type){
                0 -> {
                    val siteUrl = Uri.parse(info.value)
                    val site = Intent(Intent.ACTION_VIEW, siteUrl)
                    startActivity(site)
                }
                1 -> {
                    try {
                        startActivity(context!!.packageManager.getLaunchIntentForPackage(info.value))
                    }
                    catch (e: Exception){
                        Snackbar.make(view, getString(R.string.unable_to_start), Snackbar.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    val number = Uri.parse("tel:${info.value}")
                    val dial = Intent(Intent.ACTION_CALL, number)
                    startActivity(dial)
                }
            }
        }
    }



    private fun bindEmpty(view: View, h: Int, w: Int){
        view.tag = null
        view.setOnDragListener{ _: View, dragEvent: DragEvent ->

            val action = dragEvent.action
            val dropView = dragEvent.localState as View
            val dropImage = dropView.findViewById<ImageView>(R.id.square_image)
            val dropText = dropView.findViewById<TextView>(R.id.app_name)

            val image = view.findViewById<ImageView>(R.id.square_image)
            val text = view.findViewById<TextView>(R.id.app_name)

            when(action){
                DragEvent.ACTION_DRAG_STARTED -> {
                    return@setOnDragListener true
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    image.visibility = View.VISIBLE
                    image.setImageDrawable(dropImage.drawable)
                    text.text = dropText.text
                    return@setOnDragListener true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    image.visibility = View.INVISIBLE
                    image.setImageResource(R.drawable.default_web_icon)
                    text.text = ""
                    view.setBackgroundResource(R.drawable.app_selector)
                    return@setOnDragListener true
                }
                DragEvent.ACTION_DROP -> {
                    view.setBackgroundResource(R.drawable.app_selector)
                    active = Position(h, w)
                    Thread(Runnable {
                        AppDatabase.getInstance(context!!).desktopAppInfo().migrate(PositionConverter().toPosition(dropView.tag as String), Position(h, w))
                        update()
                    }).start()
                    return@setOnDragListener true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    dropView.post {
                        dropView.visibility = View.VISIBLE
                    }
                    return@setOnDragListener true
                }
            }

            return@setOnDragListener false
        }

        view.setBackgroundResource(R.drawable.app_selector)
        view.setOnLongClickListener {
            contactPos = Position(h, w)
            val df = DialogFragmentMain.create(h, w)
            df.show(fragmentManager, "dialog_choose")
            return@setOnLongClickListener true
        }
    }

    private fun getImage(info: DesktopAppInfo): Bitmap{

        /*
        * type
        * 0 - site
        * 1 - application
        * 2 - contact
        */

        val img: Bitmap
        if(info.imagePath == "" || !File(info.imagePath).exists()){
            img = when(info.type){
                0 -> {
                    val st = BufferedReader(InputStreamReader(URL("http://favicon.yandex.net/favicon/${info.value}?size=32&json=1").openConnection().getInputStream())).readLine()
                    Log.i("Shad", st)
                    val img2 = if(st.contains("\"image\":\"")){
                        Log.i("Shad", "main")
                        val tmp = ImageLoaderService.loadBitmap("http://favicon.yandex.net/favicon/${info.value}?size=32")
                        if(tmp != null){
                            if(tmp.width != tmp.height){
                                Bitmap.createBitmap(tmp, 0, tmp.height - tmp.width, tmp.width, tmp.width)
                            }
                            else
                                tmp
                        }
                        else
                            null
                    }else{
                        Log.i("Shad", "else")
                        BitmapFactory.decodeResource(resources, R.drawable.default_web_icon)
                    }

                    img2 ?: BitmapFactory.decodeResource(resources, R.drawable.default_web_icon)

                }
                1 -> {
                    (LauncherActivity
                            .newData
                            ?.find {
                                app -> info.value == app.app.packageName
                            }
                            ?.drawable as? BitmapDrawable)
                            ?.bitmap?:
                    (context!!.packageManager.getApplicationIcon(info.value)as BitmapDrawable).bitmap
                }
                else -> {
                    BitmapFactory.decodeResource(resources, R.drawable.default_contact_icon)
                }
            }
            val f = File.createTempFile("icon", ".png")
            img.compress(Bitmap.CompressFormat.PNG, 0, f.outputStream())
            AppDatabase.getInstance(context!!).desktopAppInfo().setImagePath(info.pos, f.absolutePath)
        }
        else{
            img = BitmapFactory.decodeFile(info.imagePath)
        }
        return img
    }

    fun getContactInfo(name: String, data: String){
        Log.i("Shad", "${contactPos?.h} ${contactPos?.w}")
       if(contactPos != null){
           AppDatabase.getInstance(context!!).desktopAppInfo().insertAll(DesktopAppInfo(contactPos!!, 2, data, name))
           update()
        }
    }

    fun update(){
        table ?: return
        for(i in 0 until height * 2){
            val row = table.getChildAt(i) as? LinearLayout
            row ?: continue
            for(j in 0 until width){
                val v =  row.getChildAt(j)
                val image = v.findViewById<SquareImage>(R.id.square_image)
                val text = v.findViewById<TextView>(R.id.app_name)
                val desktopAppInfo = AppDatabase.getInstance(context!!).desktopAppInfo().getAppByPos(Position(i / 2, j))

                if(desktopAppInfo != null) {
                    val bmp = getImage(desktopAppInfo)
                    Handler(Looper.getMainLooper()).post {
                        v.setBackgroundResource(R.drawable.app_selector)
                        image.visibility = View.VISIBLE
                        image.setImageBitmap(bmp)
                        text.text = desktopAppInfo.text
                        bindView(v, desktopAppInfo)
                    }
                }else{
                    Handler(Looper.getMainLooper()).post {
                        image.visibility = View.INVISIBLE
                        text.text = ""
                        bindEmpty(v, i / 2, j)
                    }
                }

            }
        }
    }

}