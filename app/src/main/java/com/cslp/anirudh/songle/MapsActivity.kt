package com.cslp.anirudh.songle

import android.Manifest
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.kml.KmlLayer
import kotlinx.android.synthetic.main.activity_maps.*
import android.widget.RelativeLayout
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.kml.KmlPlacemark
import com.google.maps.android.kml.KmlPoint
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream


@Suppress("DEPRECATION")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    val tag = "MapsActivity"
    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var mapView: View? = null

    val permissionsRequestAccessFineLocation = 1
    var mLastLocation: Location? = null
    var song_number:Int? = null
    var mapLayer: KmlLayer? = null
    var oldLocation: Location? = null
    var distance:Float? = null
    // map of markers that are currently shown on map to their name "row:column"
    var markerList = HashMap<Marker,String>()
    var song: Song? = null



    override fun onConnected(p0: Bundle?) {
        try{
            createLocationRequest()
        } catch (ise: IllegalStateException){
            println("[$tag] [onConnected] IllegalStateException thrown")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            val api = LocationServices.FusedLocationApi
            mLastLocation = api.getLastLocation(mGoogleApiClient)

            if (mLastLocation == null){
                println("$tag Warning LastLocation is null")
            }
            try {
                mMap.isMyLocationEnabled = true
            }catch (se: SecurityException) {
                println("security exception thrown on [onMapReady]")
            }

        }
        else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    permissionsRequestAccessFineLocation)

        }
    }

    override fun onConnectionSuspended(p0: Int) {
        println(" >>>> onConnectionSuspended")

    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently
        println(" >>>> onConnectionFailed")
    }

    private fun createLocationRequest(){
        //Set the parameters for location service
        val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 5000    // preferably every 5 seconds
        mLocationRequest.fastestInterval = 1000     // atmost every second
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        //Can we access users current location?
        val permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest,this)

        }
    }


    ////////////////////////////////////////////////////////////////////
    ////////////// Significant game implementation starts form here /////
    ///////////////////////////////////////////////////////////////////

    // Check if near any marker in markerList that is initialised in onMapReady.
    // If near marker, remove marker, give user feedback and remove from markerList<Marker,String>.
    // Check each time if  -
    //  -- progress bar needs to be updated
    //  -- map level has to be updated (and next map need to load?)
    //  -- update percentage if needed
    // Do -
    //  -- Update distance travelled
    override fun onLocationChanged(current: Location?) {
        if (current == null) {
            println("[$tag] [onLocationChanged] Location unknown")
        } else {
            println("""[$tag] [onLocationChanged] Lat/long now
                (${current.latitude},
                ${current.longitude})"""
            )

            // Check if near any marker. If near, remove the marker. Update  words for song.

            val results = FloatArray(10) // store distance between current location and markers
            var removeMarker: Marker? = null // Save marker here if needs to be deleted after the loop

            for ((marker,w) in markerList) {

                Location.distanceBetween(current.latitude,current.longitude, // Save result to results[0]
                        marker.position.latitude,marker.position.longitude,results)

                if (results[0] <= current.accuracy && results[0] <= 10) { // as accurate as possible, at least10 metres

                    marker.remove() //remove this marker from map

                    // Get the word from lyrics (row:column --> word)
                    val lyr = Lyrics(this,song_number!!)
                    val word = lyr.getWord(w)

                    // Add word to collected word list
                    song!!.words += w

                    // User feedback
                    Toast.makeText(this, "Word Added: ${word}", Toast.LENGTH_SHORT).show()

                    removeMarker = marker    // No need to check distance with this marker again.
                }
            }

            markerList.remove(removeMarker) // removing this marker from list

            updateLevel() // check -- need to update level?

            updateProgressBar() // progress bar needs to update?

            // For distacne travelled
            if (current.accuracy < 30) {
                // Don't want noise (initial noise -- tested on real device) -
                // Can be set lower than 30, but won't work with kml trail given (and in emulator.)
                addDistance(current)
            }

            song!!.setPercentageComplete() // change percentage complete?

        }
    }


    // Get which puzzle user has selected and point song to the correct Song object.
    // Rest has to deal with map.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapView = mapFragment.view
        mapFragment.getMapAsync(this)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()

        ///////////////////

        // Get song number to set everything according to it
        song_number = intent.getIntExtra("ListClick",0)
        song = MainActivity.songList[song_number!!-1] // get pointer to the song object

    }

    // Opens correct map using helper function openCorrectMap()
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Move to University Central Area
        val centralArea = LatLng(55.944335, -3.1889770)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centralArea, 16F))

        openCorrectMap()    // Load correct map based on map level and words collected

        try {
            mMap.isMyLocationEnabled = true
        }catch (se: SecurityException) {
            println("security exception thrown on [onMapReady]")
        }

        mMap.uiSettings.isMyLocationButtonEnabled = true

        //////////////////////

        changeMyLocationButtonPosition()   // Shift MyLocation button to the bottom

    }


    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    // Any changes to the Song object is saved so that they are available when app is reopened.
    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnected)
            mGoogleApiClient.disconnect()

        ///////////////// Store changes ///////////////////

        // Store Collected words
        val sharedPref = getSharedPreferences("collectedWords",Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val set = song!!.words.toSet()
        editor.putStringSet(song_number.toString(), set);
        editor.commit()

        // Store map Level
        val sharedPref2 = getSharedPreferences("mapLevel",Context.MODE_PRIVATE)
        val editor2 = sharedPref2.edit()
        editor2.putInt(song_number.toString(),song!!.mapLevel)
        editor2.commit()

        // store distance
        val sharedPref3 = getSharedPreferences("distance",Context.MODE_PRIVATE)
        val editor3 = sharedPref3.edit()
        editor3.putFloat(song_number.toString(),if (distance!=null) {distance!!} else {0.0f})
        Log.d(tag,"saving distance: $song_number -- $distance (0.0f for) null")
        editor3.commit()
    }

    /////////////////////////////////////////////////
    //////////  Helper Functions  ///////////////////
    /////////////////////////////////////////////////


    // Called in omMapReady() and in updateLevel() levels up on map.
    // Checks song.mapLevel and open the correct kml file.
    // Shows marker for only those words which are not yet collected.
    // Also populates marketList<Map, String> -- so that distance with all markers can be computed
    // onLocationChanged.
    private fun openCorrectMap() {

        // Open map k if MainActivity.songList[position-1] has map level = k
        val mapFileName = "song_"+correct(song_number!!)+"_map${song!!.mapLevel}"
        Log.d(tag,"Attempting to open file with name: $mapFileName")
        val mapFile = openFileInput(mapFileName)

        mapLayer = KmlLayer(mMap,mapFile,this)

        // Here - Only show words which are not in caught words.
        var counter = 0 // count number of words in this map level
        val container = mapLayer!!.getContainers().iterator().next(); // get placemark container

        for (placemark in container.placemarks) {

            counter += 1  // words found
            // Get attributes
            val point = placemark.geometry as KmlPoint
            val name:String = placemark.getProperty("name")

            // check if word not collected
            if (name !in song!!.words) {

                // Place on map according to description
                val title = placemark.getProperty("description")
                var style: String
                val styleUrl = placemark.styleId

                if (styleUrl == "#unclassified")
                    style = "whtblank"
                else if (styleUrl == "#boring")
                    style = "ylwblank"
                else if (styleUrl == "#notboring")
                    style = "ylwcircle"
                else if (styleUrl == "#interesting")
                    style = "orangediamond"
                else
                    style = "redstars"

                // get icon from resources based on style
                val drawableId = resources.getIdentifier(style, "drawable", packageName)

                // Place the marker on map
                val m = mMap.addMarker(MarkerOptions().position(point.geometryObject)
                        .title(title).icon(BitmapDescriptorFactory.fromResource(drawableId)))

                markerList[m] = name // Add to markerList<Marker,String>
            }
        }

        // song.mapWordCount used to check if level has upped? If number of collected words is equal
        // to words in this level, increment map Level
        song!!.mapWordCount[song!!.mapLevel] = counter

        updateProgressBar()
    }


    // Called in onLocationCheanged. Update location travelled for this puzzle.
    // Each Song has a distance which is updates each time location changes.
    private fun addDistance(current: Location?) {
        // Add distance travelled

        distance = song!!.distance // Get song.distance

        if (oldLocation != null){
            distance = distance!! + current!!.distanceTo(oldLocation)
            println("distance = $distance")
            oldLocation = Location(current)
        } else {
            oldLocation = Location(current)
        }

        song!!.distance =+ distance!! // update song.distance
    }

    // Check if need to update Song.mapLevel
    // Open next map if level upped.
    // Update the progress bar accordingly.
    // User feedback if level upped.
    private fun updateLevel() {
        // Updates map level if all words in present level are collected

        val wordsCollected = song!!.words.size
        val mapLevel = song!!.mapLevel
        val totalWordsinLevel = song!!.mapWordCount[song!!.mapLevel]

        if (mapLevel < 4){ // Total 4 levels
            if (wordsCollected == totalWordsinLevel){

                song!!.mapLevel += 1

                when (song!!.mapLevel) { // User feedback
                    2 -> makeAlert("You can now prioritise between \"boring\" and \"notboring\" words!")
                    3 -> makeAlert("You are now shown interesting words!")
                    4 -> makeAlert("This is the last lot of words from lyrics!")
                    else -> makeAlert("ERROR: PLEASE REPORT.")
                }

                mMap.clear() // If testing on emulator directly with kml of maps (never possible in reality)
                // i.e. jumping from one marker to another, some marker were skipped (not removed).
                // This makes sure that doesn't become awkward.

                openCorrectMap()  // open next level of map
                updateProgressBar() // show level has changed on progress bar
            }
        } else {
            // Last Level
            if (wordsCollected == totalWordsinLevel) {
                // All words have been collected.
                makeAlert("You have collected all the lyrics of this song! Try and guess now!","All Words Collected")
            }
        }
    }

    // Helper function to make alerts. Mainly used for level updates.
    private fun makeAlert(message: String, title:String = "You have leveled up!"){
        // Makes a simple alert
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }


    // Updates the progress bar with number of collected words and map level
    private fun updateProgressBar(){
        // Should be called with every change (in onLocationChanged)

        val wordsCollected = song!!.words.size
        val mapLevel = song!!.mapLevel
        val totalWordsinSong = song!!.totalWords

        textViewProgress.text = "Song ${song_number}: ${wordsCollected}/${totalWordsinSong} Words Lvl: ${mapLevel}"

    }

    // Helper function for changing 1 --> 01 2--> 02 and so on.
    // So that file naming is uniform.
    private fun correct(n:Int): String {
        // Correct naming
        if (n<10)
            return "0"+n
        else
            return n.toString()
    }

    // My location button is moved to right bottom
    private fun changeMyLocationButtonPosition(){
        if (mapView != null && mapView!!.findViewById<View>(Integer.parseInt("1")) != null) {
            // Get the button view
            val locationButton = (mapView!!.findViewById<View>(Integer.parseInt("1")).getParent()
                    as View).findViewById<View>(Integer.parseInt("2"))
            // and next place it, on bottom right (as Google Maps app)
            val layoutParams = locationButton.layoutParams as RelativeLayout.LayoutParams
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
            layoutParams.setMargins(0, 0, 30, 30)
        }
    }

    ///////////////////////////////////////////////////////
    /////// For Guess! button and progress bar tap ////////
    ///////////////////////////////////////////////////////

    fun showGuessActivity(view: View){

        // Get map5's very-interesting words, if any and send as hints to guess activity
        // If empty list is sent, get hint button is not shown.
        // User can anyway give up if he has reached level 4.
        val mapFileName = "song_"+correct(song_number!!)+"_map5"
        val lst = ArrayList<String>()
        val mapFile = openFileInput(mapFileName)
        mapLayer = KmlLayer(mMap,mapFile,this)
        val container = mapLayer!!.getContainers().iterator().next()
        for (placemark in container.placemarks) {
            val name: String = placemark.getProperty("name")
            val description = placemark.getProperty("description")
            if (description == "veryinteresting"){
                lst.add(name)
            }
        }

        // Launch guess activity
        val intent = Intent(this,GuessActivity::class.java)
        intent.putExtra("songNumber",song_number!!)
        intent.putExtra("guessWords",lst)
        startActivity(intent)
    }

    // Tapping progress bar centres map at George square
    fun centreAtGeorgeSquare(view:View){
        val centralArea = LatLng(55.944335, -3.1889770)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centralArea, 16F))
    }
}
