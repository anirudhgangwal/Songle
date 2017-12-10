package com.cslp.anirudh.songle

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.content.DialogInterface
import android.support.v7.app.AlertDialog



class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    fun resetDistanceStats(view:View){
        AlertDialog.Builder(this)
                .setTitle("Delete distance data?")
                .setMessage("This will remove all distance travelled data. OK?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, {
                    dialog, whichButton -> resetDistance() })
                .setNegativeButton(android.R.string.no, null).show()
    }

    fun resetGameProgress(view:View){
        AlertDialog.Builder(this)
                .setTitle("Delete All Progress")
                .setMessage("Are you sure you want to delete all progress?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, {
                    dialog, whichButton -> resetAllProgress() })
                .setNegativeButton(android.R.string.no, null).show()
    }

    fun resetDistance(){
        for (song in MainActivity.songList){
            song.distance = 0f
        }
        val sharedPref = getSharedPreferences("distance", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()
        editor.commit()
    }

    fun resetAllProgress(){
        resetDistance()

        for (song in MainActivity.songList){
            song.guessed = false
            song.mapLevel = 1
            song.words.clear()
        }
        val sharedPref = getSharedPreferences("guessedSongs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()
        editor.commit()

        val sharedPref2 = getSharedPreferences("collectedWords", Context.MODE_PRIVATE)
        val editor2 = sharedPref2.edit()
        editor2.clear()
        editor2.commit()

        val sharedPref3 = getSharedPreferences("mapLevel", Context.MODE_PRIVATE)
        val editor3 = sharedPref3.edit()
        editor3.clear()
        editor3.commit()
    }
}
