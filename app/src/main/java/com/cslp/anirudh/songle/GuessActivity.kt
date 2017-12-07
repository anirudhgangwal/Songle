package com.cslp.anirudh.songle

import android.app.ListFragment
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import java.text.FieldPosition
import android.content.Intent
import android.util.Log
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import kotlinx.android.synthetic.main.activity_guess.*


class GuessActivity : AppCompatActivity() {

    var song_number:Int? = null // MainActivity.songList subtract one for correct song
    val tag = "GuessActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guess)

        song_number = intent.getIntExtra("songNumber",0)
        Log.d(tag,"song_number = $song_number")

        val strList = ArrayList<String>()
        strList.add("all")
        strList.add("the")
        strList.add("When")
        strList.add("time")
        strList.add("down")
        strList.add("easy")
        strList.add("Pleased")
        strList.add("problem")
        strList.add("never")

        var listFragment = fragmentManager.findFragmentById(R.id.list) as ListFragment
        var myAdapter = ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,strList)
        listFragment.listAdapter = myAdapter

        listFragment.listView.onItemClickListener = OnItemClickListener { a, v, position, id ->
            sentence.text = sentence.text.toString() + " " + a.getItemAtPosition(position).toString()
            // Allow only 120 cahracters?
        }

    }

    fun resetSentence(view:View){
        sentence.text = "Sentence:  "
    }

    fun hint(view:View) {
        TODO("Give hint. Call hint from Song maybe?")
    }

    override fun onStart() {
        super.onStart()
    }


    fun showCorrect(view: View){
        // Checks if the guess is correct and shows Congratulation screen.
        // Lets user know if song was incorrect.
        var correctGuess = MainActivity.songList[song_number!!-1].title
        if (editText.text.toString().toLowerCase() == correctGuess.toLowerCase()) {
            val intent = Intent(this,CorrectActivity::class.java)
            intent.putExtra("songNumber",song_number!!)
            startActivity(intent)
        }
        else {
            Toast.makeText(this, "Incorrect song!", Toast.LENGTH_SHORT).show();
        }
    }
}

