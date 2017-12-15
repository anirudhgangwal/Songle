package com.cslp.anirudh.songle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.content.Context
import android.net.ConnectivityManager
import android.support.v4.content.ContextCompat
import android.support.design.widget.Snackbar
import android.widget.Toast


/**
 *
 * Downloads songs.xml and parses it to initialise Song objects. Each Song object does only the
 * minimal required. Does not re-download maps or lyrics... or attributes. They are picked from
 * internal storage and Shared preference -- see Song.kt
 *
 * Checks for internet access and doesn't let player continue until Song(s) data is initialised.
 * If data is available, then user may continue but he is still made aware that internet connection
 * has issues. Of course, with no internet, user may get stuck when map does not load -- but no
 * awkward errors or fatal errors will be encountered by user.
 *
 *
 */
class MainActivity : AppCompatActivity() {

    companion object {
        var songList = ArrayList<Song>() // Store all songs here
        var isListSet = false // relaunch of activity shouldn't download songs again. (eg. by back button)
    }

    val tag = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Show Intro Activity only the very first time app launches.
        val sp = getSharedPreferences("Intro", Context.MODE_PRIVATE)
        if (!sp.getBoolean("first", false)) {
            val editor = sp.edit()
            editor.putBoolean("first", true)
            editor.apply()
            val intent = Intent(this, IntroActivity::class.java) // Call the AppIntro java class
            startActivity(intent)
        }
    }


    override fun onStart() {
        super.onStart()

        if (isNetworkAvailable()){
            Log.d(tag,"Network Available")
            downloadSongList() // always download song list and set things in motion.
        }
        else {
            Log.d(tag, "Network Unavailable")
            makeSnackBar() // Will keep making snack bar until songs.xml is downloaded
        }

    }

    private fun makeSnackBar(){

        val snackbar = Snackbar.make(findViewById(android.R.id.content),
                "No internet connection.",
                Snackbar.LENGTH_INDEFINITE)

        snackbar.setActionTextColor(ContextCompat.getColor(applicationContext,
                R.color.colorAccent))

        snackbar.setAction(R.string.try_again, View.OnClickListener {
            if (isNetworkAvailable()){  // User may click bar to retry song.xml download
                downloadSongList()
            } else {
                makeSnackBar()  // if download still fails, bar is made again.
            }
        }).show()
    }

    // Launch ListOfSongs activity where user can choose and review progress with all puzzles.
    // Activity launched on clicking Play
    fun showListOfSongs(view: View) {
        // Starts Puzzle list activity
        if (!songList.isEmpty()){
            val intent = Intent(this,ListOfSongs::class.java)
            startActivity(intent)
        }
        else { // let user know files have not been downloaded
            Toast.makeText(this, "Failed to download latest songs", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Ensure active internet connection", Toast.LENGTH_SHORT).show();
        }

    }

    // Show Stats activity
    fun showStats(view: View) { // start stats activity
        val intent = Intent(this,StatsActivity::class.java)
        startActivity(intent)
    }

    // Show setting activity
    fun showSettings(view: View){ // start settings activity
        val intent = Intent(this,SettingsActivity::class.java)
        startActivity(intent)
    }

    // Helper function to check if network is available.
    private fun isNetworkAvailable(): Boolean {
        // Network available?
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    // Downloads songs.xml and set songList: List<Song> -- each song initializes further details itself
    private fun downloadSongList() {
        if (MainActivity.isListSet == false) {
            Log.d(tag, "Network available")
            val listener = SongDownloadListener(this) // Makes list of Song objects from xml
            val downloader = DownloadXmlTask(listener)
            downloader.execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
            MainActivity.isListSet = true // Don't do this again until app is closed and launched.
        }
    }

    // Back button from home should exit the app
    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }



}







