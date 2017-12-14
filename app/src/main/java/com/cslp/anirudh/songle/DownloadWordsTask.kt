package com.cslp.anirudh.songle

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL



/**
 * Created by anirudh on 08/12/17.
 */
class DownloadWordsTask(private val caller: DownloadCompleteListener2) :
        AsyncTask<String, Void, List<String>>() {

    val tag = "DownloadWordsTask"

    override fun doInBackground(vararg urls: String?): List<String>? {

        try {
            Log.d(tag,"Do in background ... ")
            val url = URL(urls[0])
            val inp = url.openStream()
            val reader = BufferedReader(InputStreamReader(inp))
            val result = ArrayList<String>()
            var line: String? = reader.readLine()
            while (line != null) {
                //println(line)
                result.add(line)
                line = reader.readLine()
            }
            return result
        } catch (e: IOException) {
            return null
        }

    }

    override fun onPostExecute(result: List<String>?) {
        super.onPostExecute(result)
        if (result != null) {
            Log.d(tag,"Post execution - calling download complete on caller")
            caller.downloadComplete(result)
        }

    }

}

interface DownloadCompleteListener2 {
    fun downloadComplete(result: List<String>)
}

