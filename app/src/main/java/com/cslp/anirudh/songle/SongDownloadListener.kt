package com.cslp.anirudh.songle

import android.content.Context
import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

/**
 * Created by anirudh on 07/11/17.
 *
 * Parses songs.xml each time app starts. No need to check timestamp as parsing initializes all
 * Song objects (only once). Each Song object in turn only does what is necessary and new. Already downloaded
 * and set setting are read from local storage / shared preference -- see Song.kt
 *
 *
 */
class SongDownloadListener(private val context: Context) : DownloadCompleteListener{

    override fun downloadComplete(result: String) {
        Log.i("SongDownloadListener",result)
        MainActivity.songList =  parseXml(result)

    }


    private fun parseXml(s: String):ArrayList<Song> {
        val songs = parse(s)

        return songs
    }

    // We don’t use namespaces
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    private fun parse(input : String): ArrayList<Song> {

        val stream: InputStream = input.byteInputStream()

        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,
                false)
        parser.setInput(stream,null)
        parser.nextTag()
        return readFeed(parser)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): ArrayList<Song> {
        val songs = ArrayList<Song>()
        parser.require(XmlPullParser.START_TAG, ns, "Songs")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag
            if (parser.name == "Song") {
                songs.add(readSong(parser))
            }
        }
        return songs
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readSong(parser: XmlPullParser): Song {
        parser.require(XmlPullParser.START_TAG, ns, "Song")
        var number = ""
        var artist = ""
        var title = ""
        var link = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG)
                continue
            when(parser.name){
                "Number" -> number = readText(parser)
                "Artist" -> artist = readText(parser)
                "Title" -> title = readText(parser)
                "Link" -> link = readText(parser)
            }
        }
        return Song(context, number, artist,title, link)
    }


    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {

        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }
}
