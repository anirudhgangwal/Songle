package com.cslp.anirudh.songle

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.google.maps.android.kml.KmlLayer

/**
 * Created by anirudh on 06/11/17.
 * Song
 */
class Song(val ctx: Context, val number: String, val artist: String, val title: String, val link: String) {
    val tag = "Song"

    var percentageComplete = "0"
    var unlocked = false
    var map1: KmlLayer? = null

    fun getNumberName(): String = "Song " + number

    override fun toString(): String {
        return("Numer: $number  Artist: $artist Title: $title   Link: $link")
    }

    init {
        val url:String = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map1.kml"
        // Send this url to download kml file
        if (isNetworkAvailable()) {
            val caller = MapDownloadListener(number.toInt())
            val kmlDownloader = DownloadXmlTask(caller)
            kmlDownloader.execute(url)

        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =  ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun setMap1(kmlString: String){
        val FILENAME = "song_"+number+"_map1"
        Log.d(tag,"Writin file with name: $FILENAME")
        val string = kmlString

        val fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE)
        fos.write(string.toByteArray())
        fos.close()
    }

}

class MapDownloadListener(val number: Int) : DownloadCompleteListener {
    override fun downloadComplete(kmlString: String) {

        MainActivity.songList[number-1].setMap1(kmlString)
    }
}