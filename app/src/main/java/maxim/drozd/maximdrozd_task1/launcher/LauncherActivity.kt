package maxim.drozd.maximdrozd_task1.launcher

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_launcher.*
import kotlinx.android.synthetic.main.app_bar_launcher.*
import maxim.drozd.maximdrozd_task1.DB.AppDatabase
import maxim.drozd.maximdrozd_task1.DB.AppInfo
import maxim.drozd.maximdrozd_task1.PreferencesActivity
import maxim.drozd.maximdrozd_task1.ProfileActivity
import maxim.drozd.maximdrozd_task1.R
import maxim.drozd.maximdrozd_task1.welcome_pages.WelcomePageActivity
import android.view.ContextMenu
import android.view.Menu
import android.net.Uri
import android.util.Log
import android.content.IntentFilter
import android.os.Build
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.content_launcher.*
import java.lang.StringBuilder

interface ClickListener {
    fun onClick(position: Int)
}

class LauncherActivity : AppCompatActivity(), ClickListener {

    private val monitor = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
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
                if (this@LauncherActivity.supportFragmentManager.fragments.size > 0) {
                    this@LauncherActivity.runOnUiThread {
                        notifyDataSet()
                    }
                }
            }).start()
        }
    }

    private fun notifyDataSet() {
        this@LauncherActivity
                .supportFragmentManager
                .fragments[0]
                .view
                ?.findViewById<RecyclerView>(R.id.grid_recycler)
                ?.adapter
                ?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dark = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("preference_dark_theme", false)
        val themeId = if (dark) R.style.DarkAppThemeNoActionBar else R.style.AppThemeNoActionBar
        setTheme(themeId)

        setContentView(R.layout.activity_launcher)

        setSupportActionBar(main_toolbar)

        main_toolbar_layout.isTitleEnabled = false

        Log.i("Shad", "--------------------------------")

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addDataScheme("package")
        registerReceiver(monitor, intentFilter)

        val firstStart = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("preference_welcome", true)

        if (newData == null && firstStart) {
            Log.i("Shad", "1")
            startActivity(Intent(this, WelcomePageActivity::class.java))
            return
        } else if(newData == null){
            Log.i("Shad", "2")
            nav_view.menu.getItem(0).isChecked = true
            Thread(Runnable {
                sort(this)
                runOnUiThread {
                    if (!tooLate && supportFragmentManager.fragments.size == 0)
                        supportFragmentManager.beginTransaction().add(R.id.fragment_launcher, GridLayoutFragment()).commit()
                }
            }).start()
        }
        Log.i("Shad", "3")
        if (supportFragmentManager.fragments.size == 0)
            supportFragmentManager.beginTransaction().add(R.id.fragment_launcher, GridLayoutFragment()).commit()

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, main_toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val sortType = StringBuilder(resources.getStringArray(R.array.sorts)[PreferenceManager.getDefaultSharedPreferences(this).getString("preference_sort", "0")!!.toInt()])
        sortType[0] = sortType[0].toLowerCase()

        supportActionBar?.title = getString(R.string.sort_type) + " " + sortType

        nav_view.setNavigationItemSelectedListener(::onNavigationItemSelected)

        nav_view.getHeaderView(0).findViewById<View>(R.id.imageViewHeader).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.REQUEST_DELETE_PACKAGES, Manifest.permission.REQUEST_INSTALL_PACKAGES, Manifest.permission.CALL_PHONE), 1)


    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (supportFragmentManager?.findFragmentById(R.id.fragment_launcher) != null) {
            if (supportFragmentManager.findFragmentById(R.id.fragment_launcher)!!::class.java == GridLayoutFragment::class.java)
                nav_view.menu.getItem(0).isChecked = true
            else
                nav_view.menu.getItem(1).isChecked = true
        }

        if (somethingChanged) {
            somethingChanged = false
            recreate()
        }
    }

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_grid -> {
                replaceFragment(GridLayoutFragment())
            }
            R.id.nav_linear -> {
                replaceFragment(ListLayoutFragment())
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, PreferencesActivity::class.java))
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun replaceFragment(fr: Fragment) {
        val sup = supportFragmentManager.beginTransaction()
        sup.replace(R.id.fragment_launcher, fr)
        sup.commit()
    }

    override fun onClick(position: Int) {
        Thread(Runnable {
            AppDatabase.getInstance(this).appInfo().updateLaunch(newData!![position].app.packageName, System.currentTimeMillis())

            sort(this)
            runOnUiThread {
                notifyDataSet()
            }
        }).start()
        try{
            startActivity(Intent(packageManager.getLaunchIntentForPackage(newData!![position].app.packageName)))
        } catch (e: Exception) {
            Snackbar.make(fragment_launcher, "Unable to start app", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu?.setHeaderTitle(newData!![v!!.tag as Int].name)
        menu?.add(v?.tag as Int, Menu.NONE, 0, getString(R.string.delete_menu))
        val count = AppDatabase.getInstance(this).appInfo().getTimesLaunchedApp(newData!![v!!.tag as Int].app.packageName)[0]
        menu?.add(v.tag as Int, Menu.NONE, 1, getString(R.string.frq_menu) + ": $count")
        menu?.add(v.tag as Int, Menu.NONE, 2, getString(R.string.about_menu))
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val pn = "package:" + newData!![item.groupId].app.packageName
        val packageUri = Uri.parse(pn)
        when (item.order) {
            0 -> {
//                if(Build.VERSION.SDK_INT >= 26){
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        if (!packageManager.canRequestPackageInstalls()) {
//                            startActivityForResult(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", packageName))), 1234)
//                        } else {
//                            callInstallProcess()
//                        }
//                    } else {
//                        callInstallProcess()
//                    }
//                    val appPackage = newData!![item.groupId].app.packageName
//                    val intent = Intent(this, this::class.java)
//                    val sender = PendingIntent.getActivity(this, 0, intent, 0)
//                    val mPackageInstaller = this.packageManager.packageInstaller
//                    mPackageInstaller.uninstall(appPackage, sender.intentSender)
//                }else{
                    val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
                    startActivity(uninstallIntent)
//                }
            }

            2 -> {
                val aboutIntent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
                startActivity(aboutIntent)
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
        var somethingChanged = true
        var sortMethodChanged = true
        var popularApps: MutableList<Int> = mutableListOf()
        var newData: MutableList<CustomAppInfo>? = null
        class CustomAppInfo(var app: ApplicationInfo, var drawable: Drawable?, var name: String?)

        @Synchronized
        fun sort(context: Context) {
            Log.i("Shad", "sort!")

            val pm = context.packageManager
            if(newData == null) {
                val pmData = context.packageManager.getInstalledApplications(0)
//                        .filter { appInfo -> appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                newData = pmData
                        .map { app -> CustomAppInfo(app, null, null) }
                        .toMutableList()
                AppDatabase.getInstance(context).appInfo().updateAllAps(pmData.map { app -> AppInfo(app.packageName) })
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
            Log.i("Shad", popularApps.size.toString())
        }
    }
}
