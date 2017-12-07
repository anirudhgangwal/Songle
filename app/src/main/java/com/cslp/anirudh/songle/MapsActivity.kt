package com.cslp.anirudh.songle

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
                if (results[0] <= 10f) {
                    m.remove()
                    MainActivity.songList[song_number!!].words.add(w)
                    Toast.makeText(this, "Word Added: ${w}", Toast.LENGTH_SHORT).show()
                    removeMarker = m    // No need to check distance with this marker again.
                }
            }
            markerList.remove(removeMarker) // removing this marker from list
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


        textViewProgress.text = "Song ${song_number}: 0/0 Words "

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
        val mapFileName = "song_"+correct(song_number!!)+"_map${MainActivity.songList[song_number!!].mapLevel}"
        Log.d(tag,"Attempting to open file with name: $mapFileName")
        val map1File = openFileInput(mapFileName)
        mapLayer = KmlLayer(mMap,map1File,this)
        // Here - Only show words which are not in caught words.


        //First container in the kmlLayer
        val container = mapLayer!!.getContainers().iterator().next();
        for (placemark in container.placemarks){
            val point = placemark.geometry as KmlPoint
            val name:String = placemark.getProperty("name")
            if (name !in MainActivity.songList[song_number!!].words) {
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
        val intent = Intent(this,GuessActivity::class.java)
        intent.putExtra("songNumber",song_number)
        startActivity(intent)
    }


}
