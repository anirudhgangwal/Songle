package com.cslp.anirudh.songle

import android.os.Bundle
import android.app.ListActivity
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.ListView
import android.widget.Toast


/**
 * Created by anirudh on 06/11/17.
 */

class ListOfSongs : ListActivity() {

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        var song: Song
        setStatus(MainActivity.songList)
        listAdapter = MyAdapter(this, MainActivity.songList)

        val lv = listView
        val inflater = layoutInflater
        val header = inflater.inflate(R.layout.listheader, lv, false)
        header.elevation=8.0f
        lv.addHeaderView(header, null, false)
    }



    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        // Instead of toast, open another ativity like
        // showSongMap(songs[position])
        // Toast.makeText(this, "Song: " + position, Toast.LENGTH_SHORT).show();

        // Implementation


        val intent = Intent(this,MapsActivity::class.java)
        intent.putExtra("ListClick",position)
        startActivity(intent)

    }


    private fun setStatus(lst:ArrayList<Song>){
        for (i in 0..2) {
            lst[i].unlocked = true
        }
    }

}