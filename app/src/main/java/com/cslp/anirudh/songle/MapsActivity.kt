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

    override fun onLocationChanged(current: Location?) {
        if (current == null) {
            println("[$tag] [onLocationChanged] Location unknown")
        } else {
            println("""[$tag] [onLocationChanged] Lat/long now
                (${current.latitude},
                ${current.longitude})"""
            )
            //
            val results = FloatArray(10)
            var removeMarker: Marker? = null
            for ((m,w) in markerList) {
                Location.distanceBetween(current.latitude,current.longitude,
                        m.position.latitude,m.position.longitude,results)
                if (results[0] <= current.accuracy || results[0] <= 5) {
                    m.remove()
                    val lyr = Lyrics(this,song_number!!)
                    val word = lyr.getWord(w)
                    song!!.words += w
                    Toast.makeText(this, "Word Added: ${word}", Toast.LENGTH_SHORT).show()
                    removeMarker = m    // No need to check distance with this marker again.
                }
            }
            markerList.remove(removeMarker) // removing this marker from list

            updateLevel() // need to update level?

            updateProgressBar() // progress bar need to update

            addDistance(current) // add distance

            song!!.setPercentageComplete() // change percentage complete
        }
    }

    private fun addDistance(current: Location?) {
        // Add distance travelled

        distance = song!!.distance // Get distance

        if (oldLocation != null){
            distance = distance!! + current!!.distanceTo(oldLocation)
            println("distance = $distance")
            oldLocation = Location(current)
        } else {
            oldLocation = Location(current)
        }

        song!!.distance =+ distance!! // update distance
    }

    private fun makeAlert(message: String, title:String = "You have leveled up!"){
        // Makes a simple alert
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, which -> dialog.dismiss() }
        alertDialog.show()
    }

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
                mMap.clear()
                openCorrectMap()  // open next level of map
                updateProgressBar() // show level has changed on progress bar
            }
        } else {
            // All words collected
            if (wordsCollected == totalWordsinLevel){
                makeAlert("You have collected all the lyrics of this song! Try and guess now!","All Words Collected")
            }
        }
    }



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

        // Get song number to set everything according to it
        song_number = intent.getIntExtra("ListClick",0)
        song = MainActivity.songList[song_number!!-1]

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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

        changeMyLocationButtonPosition()   // Shift MyLocation button to the bottom

    }

    private fun openCorrectMap() {
        // Open map k if MainActivity.songList[position-1] has map level = k
        val mapFileName = "song_"+correct(song_number!!)+"_map${MainActivity.
                songList[song_number!!-1].mapLevel}"
        Log.d(tag,"Attempting to open file with name: $mapFileName")
        val mapFile = openFileInput(mapFileName)
        mapLayer = KmlLayer(mMap,mapFile,this)

        // Here - Only show words which are not in caught words.
        var counter = 0 // count number of words in this map level
        val container = mapLayer!!.getContainers().iterator().next(); // get placemark container

        for (placemark in container.placemarks) {
            counter = counter + 1  // words found
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
                markerList[m] = name
            }
        }
        // mapWordCount[MapLevel] = num_of_words_in_map
        song!!.mapWordCount[song!!.mapLevel] = counter
        updateProgressBar()
    }

    private fun updateProgressBar(){
        // Should be called with every change (in onLocationChanged)

        val wordsCollected = song!!.words.size
        val mapLevel = song!!.mapLevel
        val totalWordsinSong = song!!.totalWords

        textViewProgress.text = "Song ${song_number}: ${wordsCollected}/${totalWordsinSong} Words Lvl: ${mapLevel}"

    }

    private fun correct(n:Int): String {
        // Correct naming
        if (n<10)
            return "0"+n
        else
            return n.toString()
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

    override fun onStart() {
        super.onStart()
        mGoogleApiClient.connect()
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient.isConnected)
            mGoogleApiClient.disconnect()

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
        Log.d(tag,"saving distance: $song_number -- $distance")
        editor3.commit()
    }

    private fun changeMyLocationButtonPosition(){ // My location button is moved to right bottom
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

    fun showGuessActivity(view: View){  // Shows guess activity

        // Get map5's very-interesting words if any and send as hints to guess activity
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
        // Launch guess activity with
        val intent = Intent(this,GuessActivity::class.java)
        intent.putExtra("songNumber",song_number!!)
        intent.putExtra("guessWords",lst)
        startActivity(intent)
    }

    fun centreAtGeorgeSquare(view:View){
        val centralArea = LatLng(55.944335, -3.1889770)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centralArea, 16F))
    }
}
