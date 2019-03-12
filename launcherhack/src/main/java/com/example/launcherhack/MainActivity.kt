package com.example.launcherhack

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    val MAIN_URI = "content://maxim.drozd.maximdrozd_task1/AppInfo"
    val URI_LAST = "/last_launched"
    val URI_LAUNCHED = "/launched"

    private lateinit var apps: MutableList<Pair<String, String>>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "All permissions are granted"
        Thread(Runnable {
            apps = packageManager.getInstalledApplications(0)
                    .filterNot {
                        appInfo -> packageManager.getLaunchIntentForPackage(appInfo.packageName) == null || appInfo.packageName == this.packageName
                    }
                    .map {
                        app -> Pair(app.packageName, app.loadLabel(packageManager).toString())
                    }
                    .toMutableList()

            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, apps.map { app -> app.second })
            runOnUiThread{
                spinner.adapter = adapter
                hack.isEnabled = true
            }
        }).start()
    }

    fun buttonLastAppClick(v: View){
        Thread(Runnable {
            try {
                val app = loadData(MAIN_URI + URI_LAST)[0]
                val sb = StringBuilder()
                sb.append("Name: ")
                sb.appendln(app.name)
                sb.append("Package name: ")
                sb.appendln(app.packageName)
                sb.append("Launch count: ")
                sb.append(app.launchCount)
                val s = sb.toString()
                runOnUiThread {
                    last_app.text = s
                }
            }
            catch (e: Exception){
                runOnUiThread {
                    Toast.makeText(this, "No read permission", Toast.LENGTH_LONG).show()
                }
            }
        }).start()
    }
    fun buttonAppsClick(v: View){
        Thread(Runnable {
            try{
                val apps = loadData(MAIN_URI + URI_LAUNCHED).toMutableList()
                val sb = StringBuilder()
                apps.forEach { app ->
                    sb.append("Name: ")
                    sb.appendln(app.name)
                    sb.append("Package name: ")
                    sb.appendln(app.packageName)
                    sb.append("Launch count: ")
                    sb.appendln(app.launchCount)
                    sb.appendln()
                }
                val s = sb.substring(0, sb.lastIndex - 2)
                runOnUiThread {
                    apps_list.text = s
                }
            }
            catch (e: Exception){
                runOnUiThread {
                    Toast.makeText(this, "No read permission", Toast.LENGTH_LONG).show()
                }
            }
        }).start()
    }
    fun buttonHackClick(v: View){
        val packageName = apps[spinner.selectedItemPosition].first
        val countString = count.text.toString()
        val count = if(countString == "") 0 else countString.toInt()

        hackData(packageName, count)
    }

    private fun hackData(packageName: String, count: Int){
        Thread(Runnable {
            val appUri = Uri.parse("$MAIN_URI/update/$packageName")

            val values = ContentValues()
            values.put("timesLaunched", count)
            try {
                contentResolver.update(appUri, values, null, null)
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "No write permission", Toast.LENGTH_LONG).show()
                }
            }

        }).start()
    }

    private fun loadData(uriS: String): List<App>{
        val ans = mutableListOf<App>()
        val appsUri = Uri.parse(uriS)
        val projection = arrayOf("timesLaunched", "packageName")
            val cursor = contentResolver.query(appsUri!!, projection,
                    null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                while(true){
                    val nameIndex = cursor.getColumnIndex("packageName")
                    val packageName = cursor.getString(nameIndex)

                    val numberIndex = cursor.getColumnIndex("timesLaunched")
                    val number = cursor.getInt(numberIndex)

                    val name = packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager)

                    Log.i("Shad3", "$packageName, $number, $name")
                    ans.add(App(name.toString(), packageName, number))
                    if(cursor.isLast)
                        break
                    else
                        cursor.moveToNext()
                }
            }
            cursor?.close()

        return ans
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        startActivity(Intent(this, ProfileActivity::class.java))
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }
}

private class App(val name: String, val packageName: String, val launchCount: Int)