package com.cslp.anirudh.songle

import android.content.Context
import android.widget.TwoLineListItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter



@Suppress("DEPRECATION")
/**
 * Created by anirudh on 06/11/17.
 *
 * Custom List adaptor for ListOfSongs -- Show song name and percentage complete using list of Song
 *
 *
 */

class MyAdapter(private val context: Context, private val songs: ArrayList<Song>) : BaseAdapter() {

    override fun getCount(): Int {
        return songs.size
    }

    override fun getItem(position: Int): Any {
        return songs[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val twoLineListItem: TwoLineListItem

        if (convertView == null) {
            val inflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            twoLineListItem = inflater.inflate(
                    android.R.layout.simple_list_item_2, null) as TwoLineListItem
        } else {
            twoLineListItem = convertView as TwoLineListItem
        }

        val text1 = twoLineListItem.text1
        val text2 = twoLineListItem.text2

        if (songs[position].guessed == true) { // If guessed, percentage complete is 100%
            text1.text = (songs[position].getNumberName()) + " - ${songs[position].title} by ${songs[position].artist}"
            text2.text = "100%" + " Complete"
        } else {
            text1.text = (songs[position].getNumberName())
            text2.text = "${songs[position].percentageComplete}%" + " Complete"
        }

        // change color to differentiate enabled and disabled list items. User feedback.
        if(isEnabled(position)) {
            twoLineListItem.setBackgroundColor(parent.getResources().getColor(R.color.active_list_item));
        } else {
            twoLineListItem.setBackgroundColor(parent.getResources().getColor(R.color.inactive_list_item));
        }

        return twoLineListItem
    }

    override fun isEnabled(position: Int): Boolean {
        // is enabled based on unlocked status -- see song.kt
        return MainActivity.songList[position].unlocked
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

}