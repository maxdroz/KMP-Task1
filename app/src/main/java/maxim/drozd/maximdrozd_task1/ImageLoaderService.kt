package maxim.drozd.maximdrozd_task1

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.support.v7.preference.PreferenceManager
import android.util.Log
import maxim.drozd.maximdrozd_task1.launcher.LauncherActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.URL


class ImageLoaderService: JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {

        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.i("Shad", "job1")
        //TODO
        Thread(Runnable {
            Log.i("Shad", "job2")

            val url = "https://picsum.photos/720/1080/?random"

            val sp = PreferenceManager.getDefaultSharedPreferences(this)

            var path = sp.getString("file1_path", "")
            if(path != "")
                File(path).delete()

            path = sp.getString("file2_path", "")
            if(path != "")
                File(path).delete()

            path = sp.getString("file3_path", "")
            if(path != "")
                File(path).delete()


            val bmp1 = loadBitmap(url)
            val bmp2 = loadBitmap(url)
            val bmp3 = loadBitmap(url)

            val bmpF1= File.createTempFile("file1", ".png")
            bmp1?.compress(Bitmap.CompressFormat.PNG, 0, bmpF1.outputStream())
            sp.edit().putString("file1_path", bmpF1.absolutePath).apply()

            val bmpF2= File.createTempFile("file2", ".png")
            bmp2?.compress(Bitmap.CompressFormat.PNG, 0, bmpF2.outputStream())
            sp.edit().putString("file2_path", bmpF2.absolutePath).apply()

            val bmpF3= File.createTempFile("file3", ".png")
            bmp3?.compress(Bitmap.CompressFormat.PNG, 0, bmpF3.outputStream())
            sp.edit().putString("file3_path", bmpF3.absolutePath).apply()


//            Cache.getInstance().remove(LauncherActivity.CACHE_1)
//            Cache.getInstance().remove(LauncherActivity.CACHE_2)
//            Cache.getInstance().remove(LauncherActivity.CACHE_3)
//
//            Cache.getInstance().put(LauncherActivity.CACHE_1, bmp1)
//            Cache.getInstance().put(LauncherActivity.CACHE_2, bmp2)
//            Cache.getInstance().put(LauncherActivity.CACHE_3, bmp3)
            sendBroadcast(Intent(LauncherActivity.UPDATE_BACKGROUND))

            Log.i("Shad", "jobFinished")
        }).start()
        return false
    }
    companion object {
        fun loadBitmap(srcUrl: String): Bitmap? {
            try {
                val url = URL(srcUrl)
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