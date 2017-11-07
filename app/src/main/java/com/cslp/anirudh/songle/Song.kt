package com.cslp.anirudh.songle

/**
 * Created by anirudh on 06/11/17.
 * Song
 */
class Song(val number: String, val artist: String, val title: String, val link: String) {
    fun getNumberName(): String = "Song " + number

    override fun toString(): String {
        return("Numer: $number  Artist: $artist Title: $title   Link: $link")
    }
}