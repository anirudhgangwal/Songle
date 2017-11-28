package com.cslp.anirudh.songle

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.google.maps.android.kml.KmlLayer
import java.sql.Timestamp

/**
 * Created by anirudh on 06/11/17.
 * Song
 */
class Song(val ctx: Context, val number: String, val artist: String, val title: String, val link: String) {
    val tag = "Song"

    var percentageComplete = "0"
    var unlocked = false
    var guessed = false // Must implement details
    var distance = 1.5
    var mapLevel = 1

    fun getNumberName(): String = "Song " + number

    override fun toString(): String {
        return("Numer: $number  Artist: $artist Title: $title   Link: $link")
    }

    init {
        // Download maps if timestamp shared in shared preference not same.
        val pref = ctx.getSharedPreferences("com.cslp.anirudh.songle.MainActivity.pref",Context.MODE_PRIVATE)
        val t = pref.getString("timestamp","-1")

        val url = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map1.kml"

        if (t=="-1"){
            // Send this url to download kml file
            if (isNetworkAvailable()) {
                val caller = MapDownloadListener(number.toInt(),"map1")
                val kmlDownloader = DownloadXmlTask(caller)
                kmlDownloader.execute(url)
            }
        } else {
            if (MainActivity.timestamp!!.before(Timestamp.valueOf(t))) {
                if (isNetworkAvailable()) {
                    val caller = MapDownloadListener(number.toInt(),"map1")
                    val kmlDownloader = DownloadXmlTask(caller)
                    kmlDownloader.execute(url)
                } else {
                    // Do nothing. time stamp is latest.
                }
            }
        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =  ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun setMap1(kmlString: String){
        val FILENAME = "song_"+number+"_map1"
        Log.d(tag,"Writing file with name: $FILENAME")
        val string = kmlString

        val fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE)
        fos.write(string.toByteArray())
        fos.close()
    }

}

class MapDownloadListener(val number: Int,val description:String) : DownloadCompleteListener {
    override fun downloadComplete(kmlString: String) {
        if (description == "map1")
            MainActivity.songList[number-1].setMap1(kmlString)

    }
}