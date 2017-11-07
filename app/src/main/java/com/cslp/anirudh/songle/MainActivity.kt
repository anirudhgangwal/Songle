package com.cslp.anirudh.songle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.view.View
import android.webkit.DownloadListener
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import android.R.attr.name
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets


class MainActivity : AppCompatActivity() {

    companion object {
        var songList = ArrayList<Song>()
    }
    val tag = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()
        // download song list
        val listener = SongDownloadListener()
        val downloader = DownloadXmlTask(listener)
        downloader.execute("http://www.inf.ed.ac.uk/teaching/courses/cslp/data/songs/songs.xml")
    }

    fun showListOfSongs(view: View):Unit {
        val intent = Intent(this,ListOfSongs::class.java)
        startActivity(intent)
    }


}


class SongDownloadListener : DownloadCompleteListener{
    override fun downloadComplete(result: String) {
        Log.i("SongDownloadListener",result)
        assert(result!=null)
        MainActivity.songList =  parseXml(result)
    }

    fun parseXml(s: String):ArrayList<Song> {
        var songs = ArrayList<Song>()
        songs = parse(s)

        return songs
    }

    // We don’t use namespaces
    private val ns: String? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(input : String): ArrayList<Song> {
        var songs = ArrayList<Song>()
        val stream:InputStream = ByteArrayInputStream(input.toByteArray(StandardCharsets.UTF_8))

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
        return Song(number, artist,title, link)
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



