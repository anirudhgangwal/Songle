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
    var words: List<String>? = null

    fun getNumberName(): String = "Song " + number

    override fun toString(): String {
        return("Numer: $number  Artist: $artist Title: $title   Link: $link")
    }

    init {

        val urlMap1 = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map1.kml"
        val urlMap2 = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map2.kml"
        val urlMap3 = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map3.kml"
        val urlMap4 = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map4.kml"
        val urlMap5 = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map5.kml"

        val urlLyrics = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/lyrics.txt"

        if (isNetworkAvailable()) {

            val caller = FileDownloadListener(number.toInt())
            val fileDownloader = DownloadXmlTask(caller)
            fileDownloader.execute(urlLyrics)

            val caller1 = MapDownloadListener(number.toInt(),1)
            val kmlDownloader1 = DownloadXmlTask(caller1)
            kmlDownloader1.execute(urlMap1)

            val caller2 = MapDownloadListener(number.toInt(),2)
            val kmlDownloader2 = DownloadXmlTask(caller2)
            kmlDownloader2.execute(urlMap2)

            val caller3 = MapDownloadListener(number.toInt(),3)
            val kmlDownloader3 = DownloadXmlTask(caller3)
            kmlDownloader3.execute(urlMap3)

            val caller4 = MapDownloadListener(number.toInt(),4)
            val kmlDownloader4 = DownloadXmlTask(caller4)
            kmlDownloader4.execute(urlMap4)

            val caller5 = MapDownloadListener(number.toInt(),5)
            val kmlDownloader5 = DownloadXmlTask(caller5)
            kmlDownloader5.execute(urlMap5)

        }
    }

    fun saveLyrics(fileString: String){
        val FILENAME = "${number}Lyrics"
        Log.d(tag,"Writing file with name: $FILENAME")
        val string = fileString

        val fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE)
        fos.write(string.toByteArray())
        fos.close()
    }



    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =  ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun setMap(kmlString: String, mapNum:Int){
        val FILENAME = "song_"+number+"_map$mapNum"
        Log.d(tag,"Writing file with name: $FILENAME")
        val string = kmlString

        val fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE)
        fos.write(string.toByteArray())
        fos.close()
    }


}

class MapDownloadListener(val number: Int,val description:Int) : DownloadCompleteListener {
    override fun downloadComplete(kmlString: String) {
        MainActivity.songList[number-1].setMap(kmlString,description)
    }
}

class FileDownloadListener(val number: Int) : DownloadCompleteListener {
    override fun downloadComplete(kmlString: String) {
        MainActivity.songList[number-1].saveLyrics(kmlString)
    }
}