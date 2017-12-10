package com.cslp.anirudh.songle

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.kml.KmlLayer
import java.io.FileInputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.sql.Timestamp

/**
 * Created by anirudh on 06/11/17.
 * Song
 */
class Song(val ctx: Context, val number: String, val artist: String, val title: String, val link: String) {
    val tag = "Song"

    var percentageComplete = "0.00"
    var unlocked = false
    var guessed = false // Must implement details
    var distance:Float = 0f
    var mapLevel = 1
    var words: MutableList<String> = mutableListOf()
    var mapWordCount = HashMap<Int,Int>()
    var totalWords = 0


    fun getNumberName(): String = "Puzzle " + number

    override fun toString(): String {
        return("Numer: $number  Artist: $artist Title: $title   Link: $link")
    }

    init {

        initialMapsAndLyricsDownload()  // only if not already downloaded om the first run.
        //initialWordCount()  // only if not calculated in the first run.

        if (number.toInt() <= 3)
            unlocked = true


        updateMapLevel()
        updateWords()

        updateDistance()




        updateGuessedStatus()

    }

    private fun updateDistance() {
        val sharedPref = ctx.getSharedPreferences("distance",Context.MODE_PRIVATE)
        distance = sharedPref.getFloat(number.toInt().toString(),0f)
        Log.d(tag,"Song: $number mapLevel = $mapLevel")
    }


    private fun updateMapLevel(){
        val sharedPref = ctx.getSharedPreferences("mapLevel",Context.MODE_PRIVATE)
        mapLevel = sharedPref.getInt(number.toInt().toString(),1)
        Log.d(tag,"Song: $number mapLevel = $mapLevel")
    }

    private fun updateWords(){
        val sharedPref = ctx.getSharedPreferences("collectedWords",Context.MODE_PRIVATE)
        val set = sharedPref.getStringSet(number.toInt().toString(),null)
        if (set != null){
            words.addAll(set)
        }
    }

    private fun updateGuessedStatus(){
        val sharedPref2 = ctx.getSharedPreferences("guessedSongs",Context.MODE_PRIVATE)
        val isGuessed = sharedPref2.getBoolean(number.toInt().toString(),false)
        guessed = isGuessed

        if (guessed) {
            percentageComplete = "100"
        }
    }

    fun setTotalWords(){
        val lyr = Lyrics(ctx,number.toInt())

        totalWords = lyr.getTotalWordCount()

    }

    private fun initialMapsAndLyricsDownload(){
        val urlMap1 = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map1.kml"
        val urlMap2 = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map2.kml"
        val urlMap3 = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map3.kml"
        val urlMap4 = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map4.kml"
        val urlMap5 = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/map5.kml"

        val urlLyrics = "http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/${number}/words.txt"

        if (isNetworkAvailable()) {

            if ("${number}Lyrics" !in ctx.fileList()) {
                val caller = WordsDownloadListener(number.toInt())
                val fileDownloader = DownloadWordsTask(caller)
                fileDownloader.execute(urlLyrics)
            }


            if ("song_"+number+"_map1" !in ctx.fileList()) {
                val caller1 = MapDownloadListener(number.toInt(), 1)
                val kmlDownloader1 = DownloadXmlTask(caller1)
                kmlDownloader1.execute(urlMap1)
            }

            if ("song_"+number+"_map2" !in ctx.fileList()) {
                val caller2 = MapDownloadListener(number.toInt(), 2)
                val kmlDownloader2 = DownloadXmlTask(caller2)
                kmlDownloader2.execute(urlMap2)
            }

            if ("song_"+number+"_map3" !in ctx.fileList()) {
                val caller3 = MapDownloadListener(number.toInt(), 3)
                val kmlDownloader3 = DownloadXmlTask(caller3)
                kmlDownloader3.execute(urlMap3)
            }

            if ("song_"+number+"_map4" !in ctx.fileList()) {
                val caller4 = MapDownloadListener(number.toInt(), 4)
                val kmlDownloader4 = DownloadXmlTask(caller4)
                kmlDownloader4.execute(urlMap4)
            }
            if ("song_"+number+"_map5" !in ctx.fileList()) {
                val caller5 = MapDownloadListener(number.toInt(), 5)
                val kmlDownloader5 = DownloadXmlTask(caller5)
                kmlDownloader5.execute(urlMap5)
            }
        }
    }

    fun saveLyrics(result: List<String>){
        val FILENAME = "${number}Lyrics"

        Log.d(tag, "Writing file with name: $FILENAME")


        val fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE)
        var lines = ""
        for (line in result){
            //println(line)
            lines = lines + line + "\n"
        }
        //println("lines:\n"+lines)
        fos.write(lines.toByteArray())
        //fos.write(string.toByteArray())
        fos.close()

    }

    fun setMap(kmlString: String, mapNum:Int){
        val FILENAME = "song_"+number+"_map$mapNum"

        Log.d(tag, "Writing file with name: $FILENAME")
        val string = kmlString

        val fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE)
        fos.write(string.toByteArray())
        fos.close()

    }

    fun setPercentageComplete() {
        percentageComplete = "%.2f".format(words.size.toFloat()/totalWords!!*100)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =  ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }


}

class MapDownloadListener(val number: Int,val description:Int) : DownloadCompleteListener {
    override fun downloadComplete(result: String) {
        MainActivity.songList[number-1].setMap(result,description)
    }
}


class WordsDownloadListener(val number:Int) : DownloadCompleteListener2 {
    override fun downloadComplete(result: List<String>) {
        MainActivity.songList[number-1].saveLyrics(result)

        // These require lyrics to have been downloaded.
        // Calling them before (on the very first run) has issues
        MainActivity.songList[number-1].setTotalWords()
        MainActivity.songList[number-1].setPercentageComplete()
    }
}