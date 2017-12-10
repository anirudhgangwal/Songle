package com.cslp.anirudh.songle

import android.app.ListFragment
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import java.text.FieldPosition
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import kotlinx.android.synthetic.main.activity_guess.*
import java.util.*


class GuessActivity : AppCompatActivity() {

    var song_number:Int? = null // MainActivity.songList subtract one for correct song
    val tag = "GuessActivity"
    var guessWords = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guess)

        song_number = intent.getIntExtra("songNumber",0)
        Log.d(tag,"song_number = $song_number")


        var lyr = Lyrics(this,song_number!!)
        var listOfWords = MainActivity.songList[song_number!!-1].words.map { w ->
            lyr.getWord(w)
        }

        var listFragment = fragmentManager.findFragmentById(R.id.list) as ListFragment
        var myAdapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,listOfWords)
        listFragment.listAdapter = myAdapter

        listFragment.listView.onItemClickListener = OnItemClickListener { a, v, position, id ->
            sentence.text = sentence.text.toString() + " " + a.getItemAtPosition(position).toString()
            // Allow only 120 cahracters?
        }

        guessWords = intent.getStringArrayListExtra("guessWords")
    }

    fun resetSentence(view:View){
        sentence.text = "Sentence:  "
    }

    fun hint(view:View) {
        println("GUESS WORD LIST: $guessWords")
        if (guessWords.size > 0) {
            val word = guessWords[Random().nextInt(guessWords.size)]
            val lyr = Lyrics(this, song_number!!)
            val w = lyr.getWord(word)
            makeAlert("Here is a random word that you might find interesting: $w")
        } else {
            makeAlert("Sorry. This song has no very-interesting word.")
        }
    }

    private fun makeAlert(message: String,title:String = "Hint Word"){
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, which -> dialog.dismiss() }
        alertDialog.show()
    }

    override fun onStart() {
        super.onStart()
    }


    fun showCorrect(view: View){
        // Checks if the guess is correct and shows Congratulation screen.
        // Lets user know if song was incorrect.
        var correctGuess = MainActivity.songList[song_number!!-1].title
        if (editText.text.toString().toLowerCase() == correctGuess.toLowerCase()) {
            MainActivity.songList[song_number!!-1].guessed = true

            val sharedPref = getSharedPreferences("guessedSongs",Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(song_number.toString(), true);
            editor.commit()

            val intent = Intent(this,CorrectActivity::class.java)
            intent.putExtra("songNumber",song_number!!)
            startActivity(intent)
        }
        else {
            Toast.makeText(this, "Incorrect song!", Toast.LENGTH_SHORT).show();
        }
    }
}

