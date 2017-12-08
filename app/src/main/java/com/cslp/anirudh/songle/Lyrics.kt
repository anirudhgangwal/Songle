package com.cslp.anirudh.songle

import android.content.Context
import android.R.attr.name
import android.util.Log
import java.io.*


/**
 * Created by anirudh on 07/12/17.
 */
class Lyrics (val ctx:Context,val song_number: Int) {

    val tag = "Lyrics"

    fun getWord(location:String):String{
        val FILENAME = "${correct(song_number)}Lyrics"
        if (FILENAME in ctx.fileList()){
            val row = location.split(":")[0].toInt()
            val column = location.split(":")[1].toInt()

            var file:InputStream = ctx.openFileInput(FILENAME)

            val lines = BufferedReader(InputStreamReader(file))
            val buf = StringBuffer()
            var line:String? = lines.readLine()
            while (line!=null){
                buf.append(line+"\n")
                line = lines.readLine()
            }
            println(buf.toString())

            return "some word at $location"
        } else {
            return "ERROR: LYRICS NOT FOUND."
        }
    }

    private fun correct(index:Int):String{
        if(index<10){
            return "0${index}"
        } else {
            return (index).toString()
        }
    }
}