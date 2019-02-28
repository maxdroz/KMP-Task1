package maxim.drozd.maximdrozd_task1.launcher

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.TextView
import kotlinx.android.synthetic.main.grid_launcher_fragment.*
import maxim.drozd.maximdrozd_task1.R
import maxim.drozd.maximdrozd_task1.SquareImage

class GridLayoutFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.grid_launcher_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        grid_recycler.apply {
            val spanCount: Int
            val orientation = resources.configuration.orientation
            val isNormalLayout = !PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preference_dense", false)

            spanCount = if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (isNormalLayout)
                    6
                else
                    7
            } else {
                if (isNormalLayout)
                    4
                else
                    5
            }

            var height = PreferenceManager.getDefaultSharedPreferences(context).getString("preference_popular_height", "2")!!
            if (height == "")
                height = "2"
            val popularHeight = height.toInt()
            val isPopularEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preference_popular", true)

            val manager = GridLayoutManager(context, spanCount)
            if (isPopularEnabled) {
                manager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return when (position) {
                            0, popularHeight * spanCount + 1 -> spanCount
                            else -> 1
                        }
                    }
                }
            }
            layoutManager = manager
            adapter = GridViewAdapter(context, spanCount, popularHeight, isPopularEnabled, this@GridLayoutFragment)

            val off = resources.getDimensionPixelOffset(R.dimen.offset)
            addItemDecoration(GridViewDecorator(off))
        }
    }

    class GridViewAdapter(context: Context, private val spanCount: Int, private var popularHeight: Int, private val isPopularEnabled: Boolean, val fragment: GridLayoutFragment) : RecyclerView.Adapter<GridViewHolder>() {

        private val pm = context.packageManager

        private val handlerThread = HandlerThread("thread1")

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
                popularHeight * spanCount + 1 -> 2
                else -> 0
            }
        }

        override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): GridViewHolder {
            return when (viewType) {
                1, 2 -> GridViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.layout_header, p0, false))
                else -> GridViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.grid_layout_item, p0, false))
            }
        }

        override fun getItemCount(): Int = LauncherActivity.newData!!.size + if (isPopularEnabled) popularHeight * spanCount + 2 else 0

        override fun onBindViewHolder(view: GridViewHolder, position: Int) {
            when (view.itemViewType) {
                1 -> view.itemView.findViewById<TextView>(R.id.header).text = view.itemView.context.getString(R.string.popular_apps)
                2 -> view.itemView.findViewById<TextView>(R.id.header).text = view.itemView.context.getString(R.string.all_apps)
                else -> {
                    var isPopular = false
                    var pos = position
                    if (isPopularEnabled) {
                        isPopular = position > 0 && position <= popularHeight * spanCount
                        pos = if (isPopular) {
                            LauncherActivity.popularApps!![position - 1]
                        } else
                            position - popularHeight * spanCount - 2
                    }

                    fragment.registerForContextMenu(view.itemView)

                    if (LauncherActivity.newData!![pos].drawable == null) {
                        val handler = Handler(handlerThread.looper)
                        view.itemView.findViewById<SquareImage>(R.id.square_image).visibility = INVISIBLE
                        view.itemView.setOnClickListener { }
                        handler.post {
                            LauncherActivity.newData!![pos].drawable = LauncherActivity.newData!![pos].app.loadIcon(pm)
                            LauncherActivity.newData!![pos].name = LauncherActivity.newData!![pos].app.loadLabel(pm).toString()
                            Handler(Looper.getMainLooper()).post {
                                view.itemView.findViewById<SquareImage>(R.id.square_image).visibility = VISIBLE
                                view.itemView.findViewById<SquareImage>(R.id.square_image).setImageDrawable(LauncherActivity.newData!![pos].drawable)
                                view.itemView.findViewById<TextView>(R.id.app_name).text = LauncherActivity.newData!![pos].name
                                view.itemView.tag = pos
                                view.itemView.setOnClickListener {
                                    (fragment.activity as ClickListener).onClick(pos, isPopular, view.itemView)
                                }
                            }
                        }
                        return
                    }
                    view.itemView.findViewById<TextView>(R.id.app_name).text = LauncherActivity.newData!![pos].name
                    view.itemView.findViewById<SquareImage>(R.id.square_image).setImageDrawable(LauncherActivity.newData!![pos].drawable)
                    view.itemView.tag = pos
                    view.itemView.setOnClickListener { (fragment.activity as ClickListener).onClick(pos, isPopular, view.itemView) }
                }
            }
        }
    }

    class GridViewDecorator(val off: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.set(off, off, off, off)
        }
    }

    class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
