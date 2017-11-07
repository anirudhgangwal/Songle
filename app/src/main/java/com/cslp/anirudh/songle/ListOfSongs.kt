package com.cslp.anirudh.songle

import android.os.Bundle
import android.app.ListActivity
import android.app.PendingIntent.getActivity
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

        var songs = MainActivity.songList

        // Replace below code . Use XML to get this data



        listAdapter = MyAdapter(this, songs)

        val lv = listView
        val inflater = layoutInflater
        val header = inflater.inflate(R.layout.listheader, lv, false)
        lv.addHeaderView(header, null, false)
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        // Instead of toast, open another ativity like
        // showSongMap(songs[position])
        Toast.makeText(this, "Item: " + position, Toast.LENGTH_SHORT).show();
    }

}