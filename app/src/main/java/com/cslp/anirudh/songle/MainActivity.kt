package com.cslp.anirudh.songle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.view.View
import android.webkit.DownloadListener
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import android.R.attr.name
import android.content.Context
import java.nio.charset.StandardCharsets
import android.net.NetworkInfo
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.support.v4.content.ContextCompat
import android.support.design.widget.Snackbar
import android.widget.Toast
import java.io.*


class MainActivity : AppCompatActivity() {

    companion object {
        var songList = ArrayList<Song>()
    }
    val tag = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()

        // Work on this. First check if timestamp changed.
        try {
            //Log.d(tag,"Trying to open songList")
            //val fis = openFileInput("songList")
            //val ins = ObjectInputStream(fis)
            //MainActivity.songList = ins.readObject() as ArrayList<Song>
            //ins.close()
            //fis.close()
            //Log.d(tag,"songList file found")
        } catch (e: IOException) {
            Log.d(tag,"songList file not found")
        } finally {
            if (songList.isEmpty()){
                if (isNetworkAvailable()){
                    Log.d(tag,"Network Available")
                    downloadSongList()
                }
                else {
                    Log.d(tag,"Network Unavailable")
                    val snackbar = Snackbar.make(findViewById(android.R.id.content),
                            "No internet connection.",
                            Snackbar.LENGTH_INDEFINITE)
                    snackbar.setActionTextColor(ContextCompat.getColor(applicationContext,
                            R.color.colorAccent))
                    snackbar.setAction(R.string.try_again, View.OnClickListener {
                        //recheck internet connection and call DownloadJson if there is internet
                    }).show()
                }
            } else {
                // Do nothing? songList available
            }

        }
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

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun downloadSongList() {
        Log.d(tag,"Network available")
        val listener = SongDownloadListener(this)
        val downloader = DownloadXmlTask(listener)
        downloader.execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
    }



    override fun onStop() {
        super.onStop()
        /**try {
            val fos = openFileOutput("songList", Context.MODE_PRIVATE)
            val os = ObjectOutputStream(fos)
            os.writeObject(MainActivity.songList)
            os.close()
            fos.close()
        } finally {
            //
        }
        **/
    }


}







