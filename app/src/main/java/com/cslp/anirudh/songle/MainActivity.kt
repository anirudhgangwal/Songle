package com.cslp.anirudh.songle

import android.Manifest
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
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat


class MainActivity : AppCompatActivity() {

    companion object {
        var songList = ArrayList<Song>()
        var isListSet = false // relaunch of activity shouldn't download songs again. (eg. by back button)
    }

    val tag = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }


    override fun onStart() {
        super.onStart()

        if (isNetworkAvailable()){
            Log.d(tag,"Network Available")
            downloadSongList()
        }
        else {
            Log.d(tag, "Network Unavailable")
            makeSnackBar()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)

        }
    }

    fun makeSnackBar(){
        val snackbar = Snackbar.make(findViewById(android.R.id.content),
                "No internet connection.",
                Snackbar.LENGTH_INDEFINITE)
        snackbar.setActionTextColor(ContextCompat.getColor(applicationContext,
                R.color.colorAccent))
        snackbar.setAction(R.string.try_again, View.OnClickListener {
            if (isNetworkAvailable()){
                downloadSongList()
            } else {
                makeSnackBar()
            }
        }).show()
    }

    fun showListOfSongs(view: View) {
        if (!songList.isEmpty()){
            val intent = Intent(this,ListOfSongs::class.java)
            startActivity(intent)
        }
        else {
            Toast.makeText(this, "Failed to download latest songs", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Ensure active internet connection", Toast.LENGTH_SHORT).show();
        }

    }

    fun showStats(view: View) {
        val intent = Intent(this,StatsActivity::class.java)
        startActivity(intent)
    }

    fun showSettings(view: View){
        val intent = Intent(this,SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun isNetworkAvailable(): Boolean {

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun downloadSongList() {
        if (MainActivity.isListSet == false) {
            Log.d(tag, "Network available")
            val listener = SongDownloadListener(this)
            val downloader = DownloadXmlTask(listener)
            downloader.execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
            MainActivity.isListSet = true
        }
    }



    override fun onStop() {
        super.onStop()

    }


}







