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
    var markerList = HashMap<Marker,String>()


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
            // Do something with current location
            // Particularly, maybe check if near any placemark, if so, call addWord()
            // addWord() - should use the point in palcemark to search for detials and add word.
            val results = FloatArray(10)
            var removeMarker: Marker? = null
            for ((m,w) in markerList) {
                Location.distanceBetween(current.latitude,current.longitude,
                        m.position.latitude,m.position.longitude,results)
                if (results[0] <= current.accuracy || results[0] <= 5) {
                    m.remove()
                    val lyr = Lyrics(this,song_number!!)
                    val word = lyr.getWord(w)
                    MainActivity.songList[song_number!!-1].words.add(w)
                    Toast.makeText(this, "Word Added: ${word}", Toast.LENGTH_SHORT).show()
                    removeMarker = m    // No need to check distance with this marker again.
                }
            }
            markerList.remove(removeMarker) // removing this marker from list

            updateProgressBar() // progress bar need to update

            updateLevel()

            addDistance(current)

            MainActivity.songList[song_number!!-1].setPercentageComplete()
        }
    }

    private fun addDistance(current: Location?) {
        distance = MainActivity.songList[song_number!!-1].distance
        if (oldLocation != null){
            /**val results = FloatArray(10)
            Location.distanceBetween(current!!.latitude,current!!.longitude,
                    oldLocation!!.latitude,oldLocation!!.longitude,results)
            distance = distance + results[0]
            */
            distance = distance!! + current!!.distanceTo(oldLocation)
            println("distance = $distance")
            oldLocation = Location(current)
        } else {
            oldLocation = Location(current)
        }
        MainActivity.songList[song_number!!-1].distance =+ distance!!
    }

    private fun makeAlert(message: String,title:String = "You have leveled up!"){
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK"
        ) { dialog, which -> dialog.dismiss() }
        alertDialog.show()
    }

    private fun updateLevel() {
        val wordsCollected = MainActivity.songList[song_number!!-1].words.size
        val mapLevel = MainActivity.songList[song_number!!-1].mapLevel
        val totalWordsinLevel = MainActivity.songList[song_number!!-1].
                mapWordCount[MainActivity.songList[song_number!!-1].mapLevel]

        if (mapLevel < 4){
            if (wordsCollected == totalWordsinLevel){
                MainActivity.songList[song_number!!-1].mapLevel += 1
                when (MainActivity.songList[song_number!!-1].mapLevel) {
                    2 -> makeAlert("You can now prioritise between \"boring\" and \"notboring\" words!")
                    3 -> makeAlert("You are now shown interesting words!")
                    4 -> makeAlert("This is the last lot of words from lyrics!")
                    else -> makeAlert("ERROR: PLEASE REPORT.")
                }
                updateProgressBar() // show level has changed on progress bar
                openCorrectMap()  // open next level of map
            }
        } else {
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

        song_number = intent.getIntExtra("ListClick",0)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val central_area = LatLng(55.944335, -3.1889770)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(central_area, 16F))


        //textViewProgress.text = "Song ${song_number}: 0/0 Words "

        openCorrectMap()    // Load correct map appropriately.


        try {
            mMap.isMyLocationEnabled = true
        }catch (se: SecurityException) {
            println("security exception thrown on [onMapReady]")
        }

        mMap.uiSettings.isMyLocationButtonEnabled = true

        changeMyLocationButtonPosition()     // Shift the button to the bottom

    }

    private fun openCorrectMap() {
        // Open map k if MainActivity.songList[position-1] has map level = k
        val mapFileName = "song_"+correct(song_number!!)+"_map${MainActivity.
                songList[song_number!!-1].mapLevel}"
        Log.d(tag,"Attempting to open file with name: $mapFileName")
        val mapFile = openFileInput(mapFileName)
        mapLayer = KmlLayer(mMap,mapFile,this)
        // Here - Only show words which are not in caught words.


        //First container in the kmlLayer
        var counter = 0
        val container = mapLayer!!.getContainers().iterator().next();
        for (placemark in container.placemarks){
            counter = counter + 1
            val point = placemark.geometry as KmlPoint
            val name:String = placemark.getProperty("name")
            if (name !in MainActivity.songList[song_number!!-1].words) {
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

                val drawableId = resources.getIdentifier(style, "drawable", packageName)

                val m = mMap.addMarker(MarkerOptions().position(point.geometryObject)
                        .title(title).icon(BitmapDescriptorFactory.fromResource(drawableId)))
                markerList[m] = name
            }
        }
        MainActivity.songList[song_number!!-1].
                mapWordCount[MainActivity.songList[song_number!!-1].mapLevel] = counter
        updateProgressBar()
    }

    private fun updateProgressBar(){
        val wordsCollected = MainActivity.songList[song_number!!-1].words.size
        val mapLevel = MainActivity.songList[song_number!!-1].mapLevel
        val totalWordsinSong = MainActivity.songList[song_number!!-1].totalWords

        textViewProgress.text = "Song ${song_number}: ${wordsCollected}/${totalWordsinSong} Words Lvl: ${mapLevel}"
    }

    private fun correct(n:Int):String{
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

        // Collected words
        val sharedPref = getSharedPreferences("collectedWords",Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        val set = MainActivity.songList[song_number!!-1].words.toSet()
        editor.putStringSet(song_number.toString(), set);
        editor.commit()

        // Map Level
        val sharedPref2 = getSharedPreferences("mapLevel",Context.MODE_PRIVATE)
        val editor2 = sharedPref2.edit()
        editor2.putInt(song_number.toString(),MainActivity.songList[song_number!!-1].mapLevel)
        editor2.commit()

        val sharedPref3 = getSharedPreferences("distance",Context.MODE_PRIVATE)
        val editor3 = sharedPref3.edit()
        editor3.putFloat(song_number.toString(),if (distance!=null) {distance!!} else {0.0f})
        Log.d(tag,"saving distance: $song_number -- $distance")
        editor3.commit()
    }

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

    fun showGuessActivity(view: View){

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

        val intent = Intent(this,GuessActivity::class.java)
        intent.putExtra("songNumber",song_number!!)
        intent.putExtra("guessWords",lst)
        startActivity(intent)
    }


}
