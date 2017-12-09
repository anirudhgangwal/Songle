package com.cslp.anirudh.songle

import android.app.Activity
import android.os.Bundle
import android.app.ListActivity
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.Toast


/**
 * Created by anirudh on 06/11/17.
 */

class ListOfSongs : ListActivity() {
    val tag = "ListOfSongs"

    companion object {
        var fa:Activity? = null
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        var numGuessed = 0
        for (song in MainActivity.songList) {
            if (song.guessed == true){
                numGuessed += 1
            }
        }
        for (song in MainActivity.songList) {
            if(song.number.toInt() > 5){
                if (numGuessed > 0){
                    song.unlocked = true
                    numGuessed -= 1
                }
            }
        }


        listAdapter = MyAdapter(this, MainActivity.songList)

        val lv = listView
        val inflater = layoutInflater
        val header = inflater.inflate(R.layout.listheader, lv, false)
        header.elevation=8.0f
        lv.addHeaderView(header, null, false)

    }



    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)

        // Implementation
        val intent = Intent(this,MapsActivity::class.java)
        Log.i(tag,"Item clicked. Position = $position")
        intent.putExtra("ListClick",position)
        startActivity(intent)


    }




    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        listAdapter = MyAdapter(this, MainActivity.songList)
    }



}