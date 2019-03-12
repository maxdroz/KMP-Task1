package com.example.launcherhack

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_profile.*
import java.lang.Exception
import java.lang.StringBuilder

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.title = "Profile reader"
    }

    fun onClick(v: View){
        Thread(Runnable {
            try {
                val row = arrayOf("_id", "mobile_number", "home_number", "github", "vk", "main_email", "second_email", "work_address")
                val uri = Uri.parse("content://maxim.drozd.maximdrozd_task2/ProfileInfo/profile")
                val cursor = contentResolver.query(uri, null, null, null, null)
                if(cursor != null && cursor.moveToFirst()){
                    val mobileNumber = cursor.getString(1)
                    val homeNumber = cursor.getString(2)
                    val github = cursor.getString(3)
                    val vk = cursor.getString(4)
                    val mainEmail = cursor.getString(5)
                    val secondEmail = cursor.getString(6)
                    val workAddress = cursor.getString(7)

                    val sb = StringBuilder()
                    sb.append("Мобильный номер телефона: ")
                    sb.appendln(mobileNumber)
                    sb.append("Домашний номер телефона: ")
                    sb.appendln(homeNumber)
                    sb.append("Ссылка на github: ")
                    sb.appendln(github)
                    sb.append("Ссылка VK: ")
                    sb.appendln(vk)
                    sb.append("Основной Email: ")
                    sb.appendln(mainEmail)
                    sb.append("Дополнительный Email: ")
                    sb.appendln(secondEmail)
                    sb.append("Место работы: ")
                    sb.append(workAddress)

                    val str = sb.toString()

                    runOnUiThread {
                        profile_data.text = str
                    }

                }
                cursor?.close()
            } catch (e: Exception) {
                Log.i("Shad", "error", e)
                runOnUiThread {
                    Toast.makeText(this, "No profile permission", Toast.LENGTH_LONG).show()
                }
            }
        }).start()
    }
}
