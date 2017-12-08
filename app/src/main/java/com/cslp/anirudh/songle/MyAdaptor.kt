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

        if (songs[position].percentageComplete =="100")
            text1.text = (songs[position].getNumberName()) + " - ${songs[position].title} by ${songs[position].artist}"
        else
            text1.text = (songs[position].getNumberName())

        text2.text = "${songs[position].percentageComplete}%" + " Complete" // Use a function to find % complete

        if(isEnabled(position)) {
            twoLineListItem.setBackgroundColor(parent.getResources().getColor(R.color.active_list_item));
            //twoLineListItem.background = ColorfulListItemDrawable(R.color.abc_tint_spinner)
        } else {
            twoLineListItem.setBackgroundColor(parent.getResources().getColor(R.color.inactive_list_item));
        }

        return twoLineListItem
    }

    override fun isEnabled(position: Int): Boolean {
        return MainActivity.songList[position].unlocked
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    public fun update(){
        notifyDataSetChanged()
    }



}