package com.cslp.anirudh.songle

import android.Manifest
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntro2Fragment

/**
 * Created by anirudh on 12/12/17.
 *
 * Intro to be showed first time
 */
class IntroActivity: AppIntro() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /**
         * setFadeAnimation()
        setZoomAnimation()
        setFlowAnimation()
        setSlideOverAnimation()
        setDepthAnimation()
         */
        askForPermissions(arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION), 2)

        val descr1= "Unlock a new puzzle each time you successfully guess one!"
        val descr2= "You play in the real world, so get out and roam the University's central area! " +
                "Tap the progress bar to come back to the playing-area. Tap the guess button " +
                "to review collected words or to enter a guess."
        val descr3 = "You can always look at your stats by choosing Stats from the home screen."
        val descr4 = "The game won't work without internet connection. Which is needed to get the latest puzzles and to connect" +
                " to maps. \nPlease ensure you have active internet connection."

        addSlide(AppIntro2Fragment.newInstance("Unlock More Puzzles As You Play Along", descr1,
                R.drawable.select,resources.getColor(R.color.intro1)))
        addSlide(AppIntro2Fragment.newInstance("Play In The Real World", descr2,
                R.drawable.map,resources.getColor(R.color.intro2)))
        addSlide(AppIntro2Fragment.newInstance("The Game Requires Internet Connection!", descr4,
                R.drawable.nointernet,resources.getColor(R.color.colorAccent)))
        addSlide(AppIntro2Fragment.newInstance("Look At Your Stats", descr3,
                R.drawable.stats,resources.getColor(R.color.intro3)))

        showStatusBar(false)
        setDepthAnimation()
        showBackButtonWithDone = true


    }

    override fun onSkipPressed(currentFragment: Fragment) {
        super.onSkipPressed(currentFragment)
        // Do something when users tap on Skip button.
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment) {
        super.onDonePressed(currentFragment)
        finish()
    }

    override fun onSlideChanged(oldFragment: Fragment?,newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
        // Do something when the slide changes.
    }
}