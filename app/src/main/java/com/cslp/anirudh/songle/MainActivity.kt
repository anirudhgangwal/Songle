package com.cslp.anirudh.songle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.DownloadListener

class MainActivity : AppCompatActivity() {

    val tag = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()
        // download song list
        val listener = SongDownloadListener()
        val downloader = DownloadXmlTask(listener)
        downloader.execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
    }

    fun showListOfSongs(view: View):Unit {
        val intent = Intent(this,ListOfSongs::class.java)
        startActivity(intent)
    }


}


class SongDownloadListener : DownloadCompleteListener{
    override fun downloadComplete(result: String) {
        Log.i("SongDownloadListener",result)
        assert(result!=null)

    }

    fun parseXml(s: String):ArrayList<Song> {
        var songs = ArrayList<Song>()

        return songs
    }
}


