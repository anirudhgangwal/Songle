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
        /** [chorus]
        8	Woo-hoo
        9	When I feel heavy-metal
        10	And I'm pins and I'm needles
        11	Well, I lie and I'm easy
        12	All the time but I am never sure
        13	Why I need you
        14	Pleased to meet you
        15
        16	I got my head down
        17	When I was young
        18	It's not my problem
        19	It's not my problem*/
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

