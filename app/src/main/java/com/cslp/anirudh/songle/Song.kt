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
 *
 * Represents a song.
 *
 * No need to check time stamp - if files exist, they are simply not re-downloaded again.
 *
 * Self contained with all information about itself.
 */
class Song(private val ctx: Context, val number: String, val artist: String, val title: String, val link: String) {
    val tag = "Song"

    // These will be set in init {..} below.
    // They are set as needed. Some in MapsActivity
    var percentageComplete = "0.00" // words collected by total words in this song.
    var unlocked = false    // is this song playable yet?
    var guessed = false     // has user guessed this song
    var distance:Float = 0f // distance travelled by user playing this puzzle
    var mapLevel = 1        // User has reached which map level?
    var words: Set<String> = setOf() // words collected by user
    var mapWordCount = HashMap<Int,Int>() // How many words in map1, map2 ... map5 ?
    var totalWords = 0      // total words in this song


    fun getNumberName(): String = "Puzzle " + number // Used when displaying puzzles

    override fun toString(): String {
        return("Numer: $number  Artist: $artist Title: $title   Link: $link")
    }

    init {
        // Initialise everything here. Files downloaded only in the first run. On subsequent runs,
        // they are taken from internal storage. Other values are set from sharedPreference. Some
        // values are computed from stored values.
        //
        // Here is a list --
        //
        // 1. All 5 maps already downloaded? yes - ok no - download them (internal storage)
        // 2. first three puzzles are set unlocked = true (playable) default. Other songs become unlocked
        //      in ListOfSongs Activity based on how many puzzle s have user solved.
        // 3. Map Level is updated (shared preference)
        // 4. Collected words are restored (shared preference)
        // 5. Distance traveled for solving particular song is read (shared preference)
        // 6. Check if song has been guessed before and do needful (shared preference)
        // 7. Set total words   (calculate by using Lyrics class)

        initialMapsAndLyricsDownload()  // only if not already downloaded on the first run.

        if (number.toInt() <= 3)    // first three puzzles always playable
            unlocked = true

        updateMapLevel()
        updateWords()
        updateDistance()
        updateGuessedStatus()

        if ("${number}Lyrics" in ctx.fileList()){
            // Set total words and percentage complete if lyrics in internal storage.
            // Only case when lyrics not in internal storage is when the app is first run, in that -
            // - case, this is done after async task is complete by Download Listener
            val lyr = Lyrics(ctx,number.toInt())
            totalWords = lyr.getTotalWordCount()  // get total words from lyrics

            setPercentageComplete() // cpmpute percentage from total words and num of words collected
        }

    }

    /////////////////////////////////////////////
    /////// Functions ///////////////////////////
    /////////////////////////////////////////////


    private fun updateDistance() {  // Get distance from shared preference
        val sharedPref = ctx.getSharedPreferences("distance",Context.MODE_PRIVATE)
        distance = sharedPref.getFloat(number.toInt().toString(),0f)
        Log.d(tag,"Song: $number mapLevel = $mapLevel")
    }


    private fun updateMapLevel(){ // Get Old Map level from shared preference
        val sharedPref = ctx.getSharedPreferences("mapLevel",Context.MODE_PRIVATE)
        mapLevel = sharedPref.getInt(number.toInt().toString(),1)
        Log.d(tag,"Song: $number mapLevel = $mapLevel")
    }

    private fun updateWords(){ // get words collected from shared preference
        val sharedPref = ctx.getSharedPreferences("collectedWords",Context.MODE_PRIVATE)
        val set = sharedPref.getStringSet(number.toInt().toString(),null)
        if (set != null){
            words = words + set
        }
    }

    private fun updateGuessedStatus(){ // set guessed status from shared preference
        val sharedPref2 = ctx.getSharedPreferences("guessedSongs",Context.MODE_PRIVATE)
        val isGuessed = sharedPref2.getBoolean(number.toInt().toString(),false)
        guessed = isGuessed

        if (guessed) { // if song is guessed, percentage complete = 100%
            percentageComplete = "100"
        }
    }

    fun setTotalWords(){  // return total words in song
        val lyr = Lyrics(ctx,number.toInt())
        totalWords = lyr.getTotalWordCount()


    }

    fun setPercentageComplete() { // percentage complete
        percentageComplete = "%.2f".format(words.size.toFloat()/totalWords*100)
    }

    private fun initialMapsAndLyricsDownload(){ // download everything if not already downloaded.
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

    // Save lyrics (words.txt)
    fun saveLyrics(result: List<String>){
        val FILENAME = "${number}Lyrics"

        Log.d(tag, "Writing file with name: $FILENAME")

        val fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE)
        var lines = ""
        for (line in result){
            lines = lines + line + "\n" // Note new line character needed here
        }
        fos.write(lines.toByteArray())
        fos.close()

    }

    // save maps - eg. song_1_map1 song_1_map2 ... song_2_map1 ....
    fun setMap(kmlString: String, mapNum:Int){
        val FILENAME = "song_"+number+"_map$mapNum"

        Log.d(tag, "Writing file with name: $FILENAME")
        val string = kmlString

        val fos = ctx.openFileOutput(FILENAME, Context.MODE_PRIVATE)
        fos.write(string.toByteArray())
        fos.close()

    }

    // Helper function --  network available?
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =  ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }


}

// Save maps once downloaded
class MapDownloadListener(val number: Int, private val description:Int) : DownloadCompleteListener{
    override fun downloadComplete(result: String) {
        MainActivity.songList[number-1].setMap(result,description)
    }
}

// Save lyrics
class WordsDownloadListener(val number:Int) : DownloadCompleteListener2 {
    override fun downloadComplete(result: List<String>) {
        MainActivity.songList[number-1].saveLyrics(result)

        // These require lyrics to have been downloaded.
        // Calling them before (on the very first run) has issues
        MainActivity.songList[number-1].setTotalWords()
        MainActivity.songList[number-1].setPercentageComplete()
    }
}