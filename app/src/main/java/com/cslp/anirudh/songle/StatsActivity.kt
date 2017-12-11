package com.cslp.anirudh.songle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_stats.*

class StatsActivity : AppCompatActivity() {

    val tag = "StatsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
    }

    override fun onStart() {
        super.onStart()

        var songGuessed = 0
        var distance = 0f
        for (song in MainActivity.songList){
            if (song.guessed == true) {
                songGuessed += 1
            }
            Log.d(tag,"${song.number} - distance = ${song.distance}")
            distance += song.distance
        }
        val calorie = distance/1609.0 * 100 // Roughly 100 calories per 1 mile = 1609 metre
        textView7.text = "Songs guessed: $songGuessed"
        textView6.text = "Distance Travelled: %.0f m".format(distance)
        textView8.text = "Tentative Calories burnt: %.2f".format(calorie)
    }
}
