package com.cslp.anirudh.songle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View


class CorrectActivity : AppCompatActivity() {

    private var song_number: Int? = null
    val tag = "CorrectActivity"
    var videoId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correct)
        song_number = intent.getIntExtra("songNumber",0)
        videoId = getVideoId(MainActivity.songList[song_number!!-1].link)
    }
    fun showVideo(view: View){
        watchYoutubeVideo(this,videoId)
    }

    fun watchYoutubeVideo(context: Context, id: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id))
        val webIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id))
        try {
            context.startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }

    fun getVideoId(link : String):String{
        //https://youtu.be/fJ9rUzIMcZQ
        var id = ""
        for (i in 0..link.length-1){
            if (i>16)
                id = id+link[i]
        }
        Log.d(tag,"Anirudh returned id: $id original link: $link")
        return id
    }
}
