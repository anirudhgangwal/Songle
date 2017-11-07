package com.cslp.anirudh.songle

import android.content.Context
import android.net.ConnectivityManager
import com.google.maps.android.kml.KmlLayer

/**
 * Created by anirudh on 06/11/17.
 * Song
 */
class Song(val ctx: Context, val number: String, val artist: String, val title: String, val link: String) {

    var percentageComplete = "0"
    var unlocked = false
    var mContext: Context? = null
    val map1: KmlLayer? = null

    fun getNumberName(): String = "Song " + number

    override fun toString(): String {
        return("Numer: $number  Artist: $artist Title: $title   Link: $link")
    }

    init {
        val url:String = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map1.kml"
        // Send this url to download kml file
        if (isNetworkAvailable()) {
            val caller = MapDownloadListener()
            val kmlDownloader = DownloadXmlTask(caller)
            kmlDownloader.execute(url)
        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =  ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

}

class MapDownloadListener() : DownloadCompleteListener {
    override fun downloadComplete(result: String) {

    }

}