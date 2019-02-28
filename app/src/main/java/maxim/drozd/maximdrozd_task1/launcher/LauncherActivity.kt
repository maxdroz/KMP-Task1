package maxim.drozd.maximdrozd_task1.launcher

import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.content.pm.ApplicationInfo
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_launcher.*
import kotlinx.android.synthetic.main.app_bar_launcher.*
import maxim.drozd.maximdrozd_task1.DB.AppDatabase
import maxim.drozd.maximdrozd_task1.DB.AppInfo
import maxim.drozd.maximdrozd_task1.welcome_pages.WelcomePageActivity
import android.view.ContextMenu
import android.view.Menu
import android.net.Uri
import android.os.PersistableBundle
import android.provider.ContactsContract
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewPager
import android.util.Log
import android.support.v7.widget.RecyclerView
import com.yandex.metrica.YandexMetrica
import kotlinx.android.synthetic.main.content_launcher.*
import maxim.drozd.maximdrozd_task1.*
import maxim.drozd.maximdrozd_task1.DB.DesktopAppInfo
import maxim.drozd.maximdrozd_task1.DB.Position
import java.lang.Exception
import java.util.*

interface ClickListener {
    fun onClick(position: Int, fromPopular: Boolean, view: View)
}

class LauncherActivity : AppCompatActivity(), ClickListener {

    private val backgroundMonitor = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when(action){
                UPDATE_BACKGROUND -> {
                    Log.i("ShadJobs", "inBroadcastReciever")
                    val sp = PreferenceManager.getDefaultSharedPreferences(context)
                    val key = when (launcher_view_pager.currentItem) {
                        0 -> sp.getString("file1_path", "")
                        1 -> sp.getString("file2_path", "")
                        else -> sp.getString("file3_path", "")
                    }
                    if (key != "") {
                        val bmp = BitmapFactory.decodeFile(key)
                        this@LauncherActivity.drawer_layout.background = BitmapDrawable(resources, bmp)
                    }
                }
                UPDATE_BACKGROUND_ONCE -> {
                    Log.i("ShadJobs", "inBroadcastRecieverOnce")
                    val jobber = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                    jobber.schedule(JobInfo.Builder(2,
                            ComponentName(applicationContext, ImageLoaderService::class.java))
                            .setOverrideDeadline(0L)
                            .build())
                }
            }
        }
    }

    private val monitor = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.i("Shad", "recieve")
            Thread(Runnable {
                val action = intent?.action
                val data = intent?.data.toString().substring(8)
                Log.i("Shad", "before: ${newData?.size}")
                when (action) {
                    Intent.ACTION_PACKAGE_REMOVED -> {
                        AppDatabase.getInstance(this@LauncherActivity).appInfo().deleteApp(data)

                        newData?.removeAll { appInfo ->
                            appInfo.app.packageName.toString() == data
                        }
                    }

                    Intent.ACTION_PACKAGE_ADDED -> {
                        AppDatabase.getInstance(this@LauncherActivity).appInfo().insertAllApps(listOf(AppInfo(data)))
                        newData?.add(CustomAppInfo(this@LauncherActivity.packageManager.getApplicationInfo(data, 0), null, null))
                    }
                }

                sort(context!!)

                Log.i("Shad", "after: ${newData?.size}")
                Log.i("Shad", action + this@LauncherActivity.supportFragmentManager.fragments.size)
               this@LauncherActivity.runOnUiThread {
                    notifyDataSet()
                }
            }).start()
        }
    }

    private fun notifyDataSet() {
        supportFragmentManager
                .findFragmentByTag(LauncherFragmentPageAdaptor.makeFragmentName(launcher_view_pager.id, 1))
                ?.view
                ?.findViewById<RecyclerView>(R.id.grid_recycler)
                ?.adapter
                ?.notifyDataSetChanged()

        supportFragmentManager
                .findFragmentByTag(LauncherFragmentPageAdaptor.makeFragmentName(launcher_view_pager.id, 0))
                ?.view
                ?.findViewById<RecyclerView>(R.id.list_recycler)
                ?.adapter
                ?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dark = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("preference_dark_theme", false)
        val themeId = if (dark) R.style.DarkAppThemeNoActionBar else R.style.AppThemeNoActionBar
        setTheme(themeId)

        setContentView(R.layout.activity_launcher)

        Log.i("Shad", "--------------------------------")

        launcher_view_pager.adapter = LauncherFragmentPageAdaptor(supportFragmentManager)
        launcher_view_pager.currentItem = 1
        launcher_view_pager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                nav_view.menu.getItem(2 - position).isChecked = true
                if(position == 2){
                    app_bar_layout.visibility = View.INVISIBLE
                    val params = behaviour.layoutParams as CoordinatorLayout.LayoutParams
                    params.behavior = null
                    behaviour.requestLayout()
                }
                else{
                    app_bar_layout.visibility = View.VISIBLE
                    val params = behaviour.layoutParams as CoordinatorLayout.LayoutParams
                    params.behavior = AppBarLayout.ScrollingViewBehavior()
                    behaviour.requestLayout()
                    val fragm = supportFragmentManager.findFragmentByTag(LauncherFragmentPageAdaptor.makeFragmentName(launcher_view_pager.id, 2)) as DesktopFragment
                    if(fragm.active != null){
                        fragm.active = null
                        fragm.activeView = null
                        fragm.update()
                    }
                }

            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })

        val (offset, period) = getTimeOffsetAndPeriod(this)

        val bundle = PersistableBundle()
        bundle.putLong("offset", offset)
        bundle.putLong("period", period)

        val jobber = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobber.schedule(JobInfo.Builder(0,
                ComponentName(applicationContext, JobOffsetService::class.java))
                .setOverrideDeadline(0L)
                .setExtras(bundle)
                .build())
//        jobber.

        val firstStart = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("preference_welcome", true)

        if (firstStart && newData == null) {
            startActivity(Intent(this, WelcomePageActivity::class.java))
            return
        }else if (newData == null) {
            nav_view.menu.getItem(0).isChecked = true
            Thread(Runnable {
                sort(this)
            }).start()
        }

        val path = PreferenceManager.getDefaultSharedPreferences(this).getString("file1_path", "")
        if(path != ""){
            Thread(Runnable {
                val bmp = BitmapDrawable(resources, BitmapFactory.decodeFile(path))
                runOnUiThread{
                    this@LauncherActivity.drawer_layout.background = bmp
                }
            }).start()
        }

        setSupportActionBar(main_toolbar)

        main_toolbar_layout.isTitleEnabled = false

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, main_toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.title = getString(R.string.launcher)

        nav_view.setNavigationItemSelectedListener(::onNavigationItemSelected)

        nav_view.getHeaderView(0).findViewById<View>(R.id.imageViewHeader).setOnClickListener {
            YandexMetrica.reportEvent("Event: Profile opened")
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()

        var intentFilter2 = IntentFilter(UPDATE_BACKGROUND_ONCE)
        intentFilter2.addAction(UPDATE_BACKGROUND)
        registerReceiver(backgroundMonitor, intentFilter2)

        intentFilter2 = IntentFilter(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter2.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter2.addDataScheme("package")
        registerReceiver(monitor, intentFilter2)

    }

    override fun onResume() {
        super.onResume()
        YandexMetrica.resumeSession(this)

        nav_view.menu.getItem(launcher_view_pager.currentItem).isChecked = true

        if (somethingChanged) {
            somethingChanged = false
            recreate()
        }
    }

    override fun onPause() {
        YandexMetrica.pauseSession(this)
        super.onPause()
    }

    override fun onDestroy() {
        unregisterReceiver(backgroundMonitor)
        unregisterReceiver(monitor)
        super.onDestroy()
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_grid -> {
                Thread(Runnable {
                    YandexMetrica.reportEvent("Event: Grid layout clicked")
                    val path = PreferenceManager.getDefaultSharedPreferences(this).getString("file1_path", "")
                    if(path != ""){
                        val bmp = BitmapDrawable(resources, BitmapFactory.decodeFile(path))
                        runOnUiThread{
                            this@LauncherActivity.drawer_layout.background = bmp
                        }
                    }
                }).start()
                launcher_view_pager.currentItem = 1
            }
            R.id.nav_linear -> {
                Thread(Runnable {
                    YandexMetrica.reportEvent("Event: Linear layout clicked")
//                    if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("preference_background_diff", true))
                    val path = PreferenceManager.getDefaultSharedPreferences(this).getString("file2_path", "")
                    if(path != ""){
                        val bmp = BitmapDrawable(resources, BitmapFactory.decodeFile(path))
                        runOnUiThread{
                            this@LauncherActivity.drawer_layout.background = bmp
                        }
                    }
                }).start()
                launcher_view_pager.currentItem = 0
            }
            R.id.nav_desktop ->{
                YandexMetrica.reportEvent("Event: Desktop clicked")
                launcher_view_pager.currentItem = 2
            }
            R.id.nav_settings -> {
                YandexMetrica.reportEvent("Event: Settings clicked")
                startActivity(Intent(this, PreferencesActivity::class.java))
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onClick(position: Int, fromPopular: Boolean, view: View) {
        Thread(Runnable {
            AppDatabase.getInstance(this).appInfo().updateLaunch(newData!![position].app.packageName, System.currentTimeMillis())
            sort(this)
            runOnUiThread {
                notifyDataSet()
            }
            val json = """{"fromPopular":"$fromPopular"}"""
            YandexMetrica.reportEvent("Event: App launched", json)
        }).start()
        try {
            startActivity(Intent(packageManager.getLaunchIntentForPackage(newData!![position].app.packageName)))
        }
        catch (e: Exception){
            Snackbar.make(view, getString(R.string.unable_to_start), Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle(newData!![v!!.tag as Int].name)
        menu?.add(v?.tag as Int, Menu.NONE, 0, getString(R.string.delete_menu))
        val count = AppDatabase.getInstance(this).appInfo().getTimesLaunchedApp(newData!![v!!.tag as Int].app.packageName)[0]
        menu?.add(v.tag as Int, Menu.NONE, 1, getString(R.string.frq_menu) + ": $count")
        menu?.add(v.tag as Int, Menu.NONE, 2, getString(R.string.about_menu))
        menu?.add(v.tag as Int, Menu.NONE, 3, getString(R.string.add_to_dsk_menu))
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val pn = "package:" + newData!![item.groupId].app.packageName
        val packageUri = Uri.parse(pn)
        when (item.order) {
            0 -> {
                YandexMetrica.reportEvent("Event: App uninstalled")
                val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
                startActivity(uninstallIntent)
            }

            2 -> {
                YandexMetrica.reportEvent("Event: App info opened")
                val aboutIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
                startActivity(aboutIntent)
            }

            3 -> {
                YandexMetrica.reportEvent("Event: App added to Desktop")
                Thread(Runnable {
                    val positions = AppDatabase.getInstance(this).desktopAppInfo().getAllTakenPositions()
                    val fragm = supportFragmentManager.findFragmentByTag(LauncherFragmentPageAdaptor.makeFragmentName(launcher_view_pager.id, 2)) as DesktopFragment
                    val h = fragm.height
                    val w = fragm.width
                    var ah = -1
                    var aw = -1
                    outer@ for(i in 0 until h){
                        for(j in 0 until w){
                            if(Position(i, j) !in positions){
                                ah = i
                                aw = j
                                break@outer
                            }
                        }
                    }
                    if(ah == -1 || aw == -1){
                        Log.i("Shad", "no space")
                        Snackbar.make(launcher_view_pager, getString(R.string.no_space_desktop), Snackbar.LENGTH_SHORT).show()
                        return@Runnable
                    }
                    val name: String = newData!![item.groupId].name!!
                    val packageName: String = newData!![item.groupId].app.packageName
                    AppDatabase.getInstance(this).desktopAppInfo().insertAll(DesktopAppInfo(Position(ah, aw),1, packageName, name))
                    fragm.update()
                }).start()
            }

            else -> {
            }
        }
        return true
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
            tooLate = true
    }

    companion object {
        var tooLate = false
        var somethingChanged = false
        var sortMethodChanged = true
        var popularApps: MutableList<Int>? = null
        var newData: MutableList<CustomAppInfo>? = null
        class CustomAppInfo(var app: ApplicationInfo, var drawable: Drawable?, var name: String?)

        const val UPDATE_BACKGROUND = "maxim.drozd.maximdrozd_task1.upadte_backgeround"
        const val UPDATE_BACKGROUND_ONCE = "maxim.drozd.maximdrozd_task1.upadte_backgeround_once"

        @Synchronized
        fun sort(context: Context) {
            Log.i("Shad", "sort!")

            val pm = context.packageManager
            if(newData == null) {
                val pmData = context.packageManager.getInstalledApplications(0)
                        //.filter { appInfo -> appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                newData = pmData
                        .map { app -> CustomAppInfo(app, null, null) }
                        .toMutableList()
                AppDatabase.getInstance(context).appInfo().updateAllAps(pmData.map { app -> AppInfo(app.packageName) })
                AppDatabase.getInstance(context).desktopAppInfo().updateAllApps(pmData.map { app -> app.packageName })
            }

            when (PreferenceManager.getDefaultSharedPreferences(context).getString("preference_sort", "0")) {
                "1" -> {
                    newData?.sortByDescending { app -> pm.getPackageInfo(app.app.packageName, 0).firstInstallTime }
                }
                "2" -> {
                    newData?.sortBy { app -> app.app.loadLabel(pm).toString() }
                }
                "3" -> {
                    newData?.sortByDescending { app -> app.app.loadLabel(pm).toString() }
                }
                "4" -> {
                    val dao = AppDatabase.getInstance(context).appInfo()
                    newData?.sortByDescending { app -> dao.getTimesLaunchedApp(app.app.packageName)[0] }
                }
                else -> {}
            }
            if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("preference_popular_type", false)) {
                val dao = AppDatabase.getInstance(context).appInfo()
                val popApp = mutableListOf<Pair<CustomAppInfo, Int>>()
                newData?.forEachIndexed { index, app -> popApp.add(Pair(app, index)) }
                popularApps = popApp
                        .sortedByDescending { app -> dao.getLaunchedTime(app.first.app.packageName) }
                        .map { app -> app.second }
                        .toMutableList()
            } else {
                val dao = AppDatabase.getInstance(context).appInfo()
                val popApp = mutableListOf<Pair<CustomAppInfo, Int>>()
                newData?.forEachIndexed { index, app -> popApp.add(Pair(app, index)) }
                popularApps = popApp
                        .sortedByDescending { app -> dao.getTimesLaunchedApp(app.first.app.packageName)[0] }
                        .map { app -> app.second }
                        .toMutableList()
            }
            Log.i("Shad", popularApps?.size.toString())
        }

        fun getTimeOffsetAndPeriod(context: Context): Pair<Long, Long>{
            val now = Calendar.getInstance()

            val currHour = now.get(Calendar.HOUR_OF_DAY)
            val currMinuets = now.get(Calendar.MINUTE)

            val minLeft: Int
            val hoursLeft: Int

            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val pref = sp.getString("preference_background_freq", "900000")
            val period = pref!!.toLong()
            when(pref){
                "900000" -> {
                    minLeft = 15 - (currMinuets % 15)
                    hoursLeft = 0
                }

                "3600000" -> {
                    minLeft = 60 - currMinuets
                    hoursLeft = 0
                }

                "28800000" -> {
                    minLeft = 60 - currMinuets
                    hoursLeft = 8 - (currHour % 8)
                }

                "86400000" -> {
                    minLeft = 60 - currMinuets
                    hoursLeft = 24 - currHour
                }
                else ->{
                    minLeft = Int.MAX_VALUE
                    hoursLeft = Int.MAX_VALUE
                }
            }
            Log.i("ShadJobs", "$hoursLeft $minLeft")
            return Pair(minLeft.toLong() * 60000L + hoursLeft.toLong() * 3600000L, period)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            Thread(Runnable {
                val contactUri = data?.data
                val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val cursor = this.contentResolver.query(contactUri!!, projection,
                        null, null, null)

                if (cursor != null && cursor.moveToFirst()) {
                    val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val number = cursor.getString(numberIndex).filterNot { char -> char == ' ' || char == '-' }

                    val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val name = cursor.getString(nameIndex)

                    val fragm = supportFragmentManager.findFragmentByTag(LauncherFragmentPageAdaptor.makeFragmentName(launcher_view_pager.id, 2)) as DesktopFragment
                    fragm.getContactInfo(name, number)
                }
                cursor?.close()
            }).start()
        }
    }




}
