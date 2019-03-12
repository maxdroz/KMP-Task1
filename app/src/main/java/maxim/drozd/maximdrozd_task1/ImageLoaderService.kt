package maxim.drozd.maximdrozd_task1

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.support.v7.preference.PreferenceManager
import android.util.Log
import maxim.drozd.maximdrozd_task1.db.AppDatabase
import maxim.drozd.maximdrozd_task1.launcher.LauncherActivity
import java.io.*
import java.net.URL


class JobOffsetService: JobService(){
    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Thread(Runnable {
            Log.i("ShadJobs", "in scheduler")
            val offset = params?.extras?.getLong("offset")
            val period = params?.extras?.getLong("period")
            Thread.sleep(offset!!)
            val jobber = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobber.schedule(JobInfo.Builder(1,
                    ComponentName(applicationContext, ImageLoaderService::class.java))
                    .setOverrideDeadline(0)
                    .setPeriodic(period!!)
                    .build())
        }).start()

        return false
    }

}

class ImageLoaderService: JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        LauncherActivity.updatingBackground = false
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {

        LauncherActivity.updatingBackground = true
        Log.i("ShadJobs", "in main job")
        Thread(Runnable {
            Log.i("Shad", "job2")

            val sp = PreferenceManager.getDefaultSharedPreferences(this)

            sp.edit().putLong("last_launch", System.currentTimeMillis()).apply()

            val iconsPaths = AppDatabase.getInstance(this).desktopAppInfo().getAppsImagePahs()

            val urlId = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("preference_background_source", "0")
            val url: String = when(urlId){
                "0" -> "https://loremflickr.com/720/1080"
                "1" -> "https://picsum.photos/720/1080/?random"
                "2" -> "http://lorempixel.com/720/1080/"
                "3" -> "https://placeimg.com/720/1080/any"
                "4" -> "https://placekitten.com/720/1080"
                "5" -> "https://placebear.com/720/1080"
                "6" -> "http://placebacon.net/720/1080"
                else -> "https://source.unsplash.com/random/720x1080"
            }

            Log.i("ShadJobs", url)

            val bmp1 = loadBitmap(url)
            val bmp2 = loadBitmap(url)
            val bmp3 = loadBitmap(url)

            val bmpF1= File.createTempFile("file1", ".png")
            sp.edit().putString("file1_path", bmpF1.absolutePath).apply()

            val bmpF2= File.createTempFile("file2", ".png")
            sp.edit().putString("file2_path", bmpF2.absolutePath).apply()

            val bmpF3= File.createTempFile("file3", ".png")
            sp.edit().putString("file3_path", bmpF3.absolutePath).apply()

            cacheDir.listFiles().forEach { file ->
                if(file.isFile){
                    if(file.absolutePath.indexOf("file") != -1 || (file.absolutePath.indexOf("icon") != -1 && file.absolutePath !in iconsPaths)){
                        file.delete()
                    }
                }
            }


            bmp1?.compress(Bitmap.CompressFormat.PNG, 0, bmpF1.outputStream())
            bmp2?.compress(Bitmap.CompressFormat.PNG, 0, bmpF2.outputStream())
            bmp3?.compress(Bitmap.CompressFormat.PNG, 0, bmpF3.outputStream())


            sendBroadcast(Intent(LauncherActivity.UPDATE_BACKGROUND))

            Log.i("Shad", "jobFinished")
        }).start()
        return false
    }
    companion object {
        fun loadBitmap(srcUrl: String): Bitmap? {
            try {
                val url = URL(srcUrl)
                Log.i("Shad", "load: $url")
                val urlConnection = url.openConnection()
                val inputStream = urlConnection.getInputStream()
                val buffer = ByteArrayOutputStream()

                var nRead: Int
                val data = ByteArray(16384)

                do{
                    nRead = inputStream.read(data, 0, data.size)
                    if(nRead != -1)
                        buffer.write(data, 0, nRead)
                }while (nRead != -1)

                buffer.flush()
                val bitmap = buffer.toByteArray()
                return BitmapFactory.decodeByteArray(bitmap, 0, bitmap.size)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }
    }
}