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

        var songs = ArrayList<Song>()

        // Replace below code . Use XML to get this data
        song = Song("1","Anirudh","My Song","https://www.google.com")
        songs.add(song)

        song = Song("2","Akshay","His Song","https://www.google.com")
        songs.add(song)

        song = Song("3","Akshay","His Song","https://www.google.com")
        songs.add(song)

        song = Song("4","Akshay","His Song","https://www.google.com")
        songs.add(song)

        song = Song("5","Akshay","His Song","https://www.google.com")
        songs.add(song)

        song = Song("6","Akshay","His Song","https://www.google.com")
        songs.add(song)

        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)

        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)
        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)
        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)
        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)
        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)
        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)
        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)
        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)
        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)
        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)
        song = Song("7","Akshay","His Song","https://www.google.com")
        songs.add(song)


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