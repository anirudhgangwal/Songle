package com.cslp.anirudh.songle

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.support.v7.app.AlertDialog


/**
 *
 * Setting activity - Rest game data.
 *
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    // on click - Remove all distance data
    fun resetDistanceStats(view:View){
        // Alert and confirm.
        AlertDialog.Builder(this)
                .setTitle("Delete distance data?")
                .setMessage("This will remove all distance travelled data. OK?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, {
                    dialog, whichButton -> resetDistance() })
                .setNegativeButton(android.R.string.no, null).show()
    }

    // on click -- Rest all progress
    fun resetGameProgress(view:View){
        AlertDialog.Builder(this)
                .setTitle("Delete All Progress")
                .setMessage("Are you sure you want to delete all progress?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, {
                    dialog, whichButton -> resetAllProgress() })
                .setNegativeButton(android.R.string.no, null).show()
    }

    // Helper function to delete distance data
    private fun resetDistance(){
        for (song in MainActivity.songList){
            // set distance 0 for all songs
            song.distance = 0f
        }
        // Remove from shared preference
        val sharedPref = getSharedPreferences("distance", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.clear()
        editor.commit()

        Toast.makeText(this,"Distance stats have been reset", Toast.LENGTH_SHORT).show() // Feedback
    }

    private fun resetAllProgress(){

        resetDistance()

        for (song in MainActivity.songList){
            // set level, guessed and words collected to default values.
            song.guessed = false
            song.mapLevel = 1
            song.words = setOf()
            song.setPercentageComplete()
            // only leave first 3 unlocked.
            if (song.number.toInt() > 3)
                song.unlocked = false
        }

        // Remove all from shared preference
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

        Toast.makeText(this,"Progress has been reset",Toast.LENGTH_SHORT).show() // Feedback
    }
}
