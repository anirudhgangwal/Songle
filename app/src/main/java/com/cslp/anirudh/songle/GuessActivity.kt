package com.cslp.anirudh.songle

import android.app.ListFragment
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import kotlinx.android.synthetic.main.activity_guess.*
import java.util.*

/**
 *
 * Activity shows the list of words collected yet.
 *
 * Shows get hint button if hint words are available (very-interesting words from map5).
 *
 * Shows give up button only after user has reached map level 4.
 *
 * Also implements Sentence builder.
 *
 */
class GuessActivity : AppCompatActivity() {

    private var song_number:Int? = null // MainActivity.songList subtract one for correct song
    private val tag = "GuessActivity"
    private var hintWords = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guess)

        song_number = intent.getIntExtra("songNumber",0)
        Log.d(tag,"song_number = $song_number")

        // Change list of words of form "row:column" to actual words
        var lyr = Lyrics(this,song_number!!)
        var listOfWords = MainActivity.songList[song_number!!-1].words.map { w ->
            lyr.getWord(w)
        }

        // FOr user feedback -- no words have been collected.
        if (listOfWords.size == 0){
            listOfWords += "NO WORDS COLLECTED YET"
        }

        // Get the list fragment
        var listFragment = fragmentManager.findFragmentById(R.id.list) as ListFragment
        // Use listOfWords defined above with array adaptor
        var myAdapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,listOfWords)
        // Set list's adaptor
        listFragment.listAdapter = myAdapter

        // Set on click listener for Sentence Builder
        listFragment.listView.onItemClickListener = OnItemClickListener { a, _, position, _ ->
            if ("NO WORDS COLLECTED YET" !in listOfWords) // Sentence builder works only with words
                sentence.text = sentence.text.toString() + " " + a.getItemAtPosition(position).toString()
            else    // If user clicks -- feedback
                Toast.makeText(this,"No words to make a sentence.",Toast.LENGTH_SHORT).show()
        }

        // Show give up button if map level is 4
        if (MainActivity.songList[song_number!!-1].mapLevel == 4){

            giveup.visibility = View.VISIBLE
        }

        // Get hint words
        hintWords = intent.getStringArrayListExtra("guessWords")

        // Hint button not visible if no very-interesting words to give.
        if (hintWords.size == 0){
            button9.visibility = View.INVISIBLE
        }
    }

    // Sentence builder -- reset sentence
    fun resetSentence(view:View){
        sentence.text = "Sentence:  "
    }

    // Give hint onClick
    fun hint(view:View) {
        println("GUESS WORD LIST: %s".format(if (hintWords.size>0) {hintWords} else {"EMPTY"}))

        if (hintWords.size > 0) {
            val word = hintWords[Random().nextInt(hintWords.size)]
            val lyr = Lyrics(this, song_number!!)
            val w = lyr.getWord(word)
            makeAlert("Here is a random word that you might find interesting: $w")
        } else {
            makeAlert("Sorry. This song has no very-interesting word.")
        }
    }

    // helper to make alert
    private fun makeAlert(message: String,title:String = "Hint Word"){
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }

    // Give up -- Show correct answer on editText
    fun giveUp(view:View){
        editText.setText(MainActivity.songList[song_number!!-1].title)
    }

    // Checks if the guess is correct and shows Congratulation screen.
    // Lets user know if song was incorrect.
    fun showCorrect(view: View) {

        var correctGuess = MainActivity.songList[song_number!!-1].title

        if (editText.text.toString().toLowerCase() == correctGuess.toLowerCase()) {

            MainActivity.songList[song_number!!-1].guessed = true // Set song is guessed !

            // Save in shared preference
            val sharedPref = getSharedPreferences("guessedSongs",Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(song_number.toString(), true);
            editor.commit()

            // launch congratulatory screen
            val intent = Intent(this,CorrectActivity::class.java)
            intent.putExtra("songNumber",song_number!!)
            startActivity(intent)
        }
        else { // feedback
            Toast.makeText(this, "Incorrect song!", Toast.LENGTH_SHORT).show();
        }
    }
}

