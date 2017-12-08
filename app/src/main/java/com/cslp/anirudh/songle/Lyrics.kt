package com.cslp.anirudh.songle

import android.content.Context
import android.R.attr.name
import android.util.Log
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths


/**
 * Created by anirudh on 07/12/17.
 */
class Lyrics (val ctx:Context,val song_number: Int) {

    val tag = "Lyrics"

    fun getWord(location:String):String{
        val FILENAME = "${correct(song_number)}Lyrics"

        if (FILENAME in ctx.fileList()){
            val file = ctx.openFileInput(FILENAME)

            val reader = InputStreamReader(file)
            var lines = reader.readLines()

            var row = -1
            var column = -1
            row = location.split(":")[0].toInt()
            column = location.split(":")[1].toInt()
            println("location = $location row = $row col = $column ")
            val line = lines[row-1].split("\t")[1]
            println("line = $line")

            file.close()

            return line.split(" ")[column-1]



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