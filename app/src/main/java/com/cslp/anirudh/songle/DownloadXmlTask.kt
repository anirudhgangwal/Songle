package com.cslp.anirudh.songle

import android.os.AsyncTask
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader


/**
 * Created by anirudh on 06/11/17.
 */

class DownloadXmlTask(private val caller: DownloadCompleteListener) :
        AsyncTask<String, Void, String>(){

    val tag = "DownloadXmlTask"

    override public fun doInBackground(vararg urls: String?): String {
        return try {
            Log.d(tag,"Do in background ... ")
            loadXmlFromNetwork(urls[0])
        } catch (e: IOException) {
            "Unable to load content. Check your network connection"
        } catch (e: XmlPullParserException) {
            "Error parsing XML"
        }
    }

    private fun loadXmlFromNetwork(urlString: String?): String {
        Log.d(tag,"load xml from network ...")
        val result = downloadUrl(urlString)
        // Do something with stream e.g. parse as XML, build result

        Log.d(tag,"loadXmlFromNetwork returns "+ result)
        return result
    }

    @Throws(IOException::class)
    private fun downloadUrl(urlString: String?): String {
        Log.d(tag,"download url ...")
        val url = URL(urlString)
        // Starts the query
        val inp = url.openStream()
        val reader = BufferedReader(InputStreamReader(inp))
        val result = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            result.append(line)
            line = reader.readLine()
        }

        Log.d(tag,"returning value\n " + result.toString())
        return result.toString()
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (result != null) {
            Log.d(tag,"Post execution - calling download complete on caller")
            caller.downloadComplete(result)
        }
    }
}

interface DownloadCompleteListener {
    fun downloadComplete(result: String)
}
