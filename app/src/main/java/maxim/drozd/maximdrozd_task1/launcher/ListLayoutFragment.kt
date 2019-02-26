package maxim.drozd.maximdrozd_task1.launcher

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.linear_launcher_fragment.*
import maxim.drozd.maximdrozd_task1.R

class ListLayoutFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.linear_launcher_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list_recycler.apply {

            var height = PreferenceManager.getDefaultSharedPreferences(context).getString("preference_popular_height", "2")!!
            if (height == "")
                height = "2"
            val popularHeight = height.toInt()
            val isPopularEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preference_popular", true)

            adapter = ListViewAdapter(context, popularHeight, isPopularEnabled, this@ListLayoutFragment)
            layoutManager = LinearLayoutManager(context)
            val off = resources.getDimensionPixelOffset(R.dimen.offset)
            addItemDecoration(GridLayoutFragment.GridViewDecorator(off))
        }
    }

    class ListViewAdapter(context: Context, val popularHeight: Int, val isPopularEnabled: Boolean, val fragment: ListLayoutFragment) : RecyclerView.Adapter<ListViewHolder>() {

        val pm = context.packageManager

        val handlerThread = HandlerThread("thread1")

        init {
            handlerThread.start()

            if (LauncherActivity.sortMethodChanged) {
                LauncherActivity.sortMethodChanged = false
                LauncherActivity.sort(context)
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (!isPopularEnabled) {
                return 0
            }

            return when (position) {
                0 -> 1
                popularHeight + 1 -> 2
                else -> 0
            }
        }
        override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): ListViewHolder {
            return when (viewType) {
                1, 2 -> ListViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.layout_header, p0, false))
                else -> ListViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.linear_layout_item, p0, false))
            }
        }

        override fun getItemCount(): Int = LauncherActivity.newData!!.size + if (isPopularEnabled) popularHeight + 2 else 0

        override fun onBindViewHolder(view: ListViewHolder, position: Int) {
         when (view.itemViewType) {
                1 -> view.itemView.findViewById<TextView>(R.id.header).text = view.itemView.context.getString(R.string.popular_apps)
                2 -> view.itemView.findViewById<TextView>(R.id.header).text = view.itemView.context.getString(R.string.all_apps)
             else -> {
                 var isPopular = false
                 var pos = position
                 if (isPopularEnabled) {
                     isPopular = position in 1..popularHeight
                     pos = if (isPopular)
                         LauncherActivity.popularApps!![position - 1]
                     else
                         position - popularHeight - 2
                 }

                 fragment.registerForContextMenu(view.itemView)

                 if (LauncherActivity.newData!![pos].drawable == null) {
                     val handler = Handler(handlerThread.looper)
                     view.itemView.findViewById<ImageView>(R.id.square_image).visibility = View.INVISIBLE
                     view.itemView.setOnClickListener { }
                     handler.post {
                         LauncherActivity.newData!![pos].drawable = LauncherActivity.newData!![pos].app.loadIcon(pm)
                         LauncherActivity.newData!![pos].name = LauncherActivity.newData!![pos].app.loadLabel(pm).toString()
                         Handler(Looper.getMainLooper()).post {
                             view.itemView.findViewById<ImageView>(R.id.square_image).visibility = View.VISIBLE
                             view.itemView.findViewById<ImageView>(R.id.square_image).setImageDrawable(LauncherActivity.newData!![pos].drawable)
                             view.itemView.findViewById<TextView>(R.id.app_name).text = LauncherActivity.newData!![pos].name
                             view.itemView.tag = pos
                             view.itemView.setOnClickListener {
                                 (fragment.activity as ClickListener).onClick(pos, isPopular)
                             }
                         }
                     }
                     return
                 }
                 view.itemView.findViewById<TextView>(R.id.app_name).text = LauncherActivity.newData!![pos].name
                 view.itemView.findViewById<ImageView>(R.id.square_image).setImageDrawable(LauncherActivity.newData!![pos].drawable)
                 view.itemView.tag = pos
                 view.itemView.setOnClickListener { (fragment.activity as ClickListener).onClick(pos, isPopular) }
             }
            }
        }
    }

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
