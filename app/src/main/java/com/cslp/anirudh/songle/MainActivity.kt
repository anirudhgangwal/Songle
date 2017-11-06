package com.cslp.anirudh.songle

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun showListOfSongs(view: View):Unit {
        val intent = Intent(this,ListOfSongs::class.java)
        startActivity(intent)
    }


}
