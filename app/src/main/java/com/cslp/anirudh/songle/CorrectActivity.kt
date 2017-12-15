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
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_correct.*

/**
 *  Activity shown when user guesses correctly.
 *
 *
 */
class CorrectActivity : AppCompatActivity() {

    private var song_number: Int? = null
    private val tag = "CorrectActivity"
    private var videoId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_correct)

        song_number = intent.getIntExtra("songNumber",0)
        val song = MainActivity.songList[song_number!!-1]

        videoId = getVideoId(song.link)
        textView11.text = song.title
        textView12.text = "By ${song.artist}"
        textView13.text = "Distance covered: %.0f Meters".format(song.distance)
    }

    // onClick() for VIDEO
    fun showVideo(view: View){
        watchYoutubeVideo(this,videoId)
    }

    // Launch video on youtube app
    private fun watchYoutubeVideo(context: Context, id: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id))
        val webIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id))
        try {
            context.startActivity(appIntent)
        } catch (ex: ActivityNotFoundException) {
            context.startActivity(webIntent)
        }
    }

    // Get video ID from url
    fun getVideoId(link : String):String{

        var id = ""
        for (i in 0..link.length-1){
            if (i>16)
                id = id+link[i]
        }
        Log.d(tag,"returned id: $id original link: $link")
        return id
    }

    // on click BACK TO HOME
    fun backToHome(view: View){
        Toast.makeText(this,"More puzzles may be available.", Toast.LENGTH_SHORT) // Feedback
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)

    }

    // Take back to home screen
    override fun onBackPressed() {
        super.onBackPressed()
        Toast.makeText(this,"More puzzles may be available.", Toast.LENGTH_SHORT) // Feedback
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
