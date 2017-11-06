package com.cslp.anirudh.songle

import android.content.Context
import android.widget.TextView
import android.widget.TwoLineListItem
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.nio.file.Files.size
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

        text1.text = (songs[position].getNumberName())
        text2.text = "0%" + " Complete" // Use a function to find % complete

        return twoLineListItem
    }
}