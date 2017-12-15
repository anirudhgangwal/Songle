package com.cslp.anirudh.songle

import android.content.Context
import android.R.attr.name
import android.util.Log
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths


/**
 * Created by anirudh on 07/12/17.
 *
 * Given song_number, objects of this type can covert "row:column" to words with getWord()
 *
 * getTotalWordCount() return total words in song.
 *
 */
class Lyrics (private val ctx:Context, private val song_number: Int) {

    val tag = "Lyrics"

    fun getWord(location:String):String{
        val FILENAME = "${correct(song_number)}Lyrics"

        if (FILENAME in ctx.fileList()) { // check lyrics for this song is available

            val file = ctx.openFileInput(FILENAME)
            val reader = InputStreamReader(file)

            // all lines
            val lines = reader.readLines()

            print("LINES $song_number \n${lines}")

            var row = -1
            var column = -1

            // get row and column from "row:column"
            row = location.split(":")[0].toInt()
            column = location.split(":")[1].toInt()

            // line number and number seperated by tab character.
            val line = lines[row-1].split("\t")[1]


            file.close()

            return line.split(" ")[column-1]

        } else {  // should never happen
            return "ERROR: LYRICS NOT FOUND."
        }
    }

    fun getTotalWordCount():Int{
        val FILENAME = "${correct(song_number)}Lyrics"

        if (FILENAME in ctx.fileList()) {
            val file = ctx.openFileInput(FILENAME)

            var count = 0
            val reader = InputStreamReader(file)
            val lines = reader.readLines()
            for (line in lines){

                var wordsInLine = 0
                for (word in line.split("\t")[1].split(" ")){
                    if (word.trim().isNotEmpty())
                        wordsInLine += 1
                }
                count += wordsInLine
                //println("COUNT song_number - $song_number line \n$line COUNT: $wordsInLine")

            }
            //println("TOTALCOUNT song_number - $song_number count - $count")
            return count
        } else {
            return -1
        }
    }

    // Correct naming.
    private fun correct(index:Int):String{
        return if(index<10){
            "0${index}"
        } else {
            (index).toString()
        }
    }
}