package com.cslp.anirudh.songle

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.kml.KmlLayer
import kotlinx.android.synthetic.main.activity_maps.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    val tag = "MapsActivity"
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
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

        val song_number = intent.getIntExtra("ListClick",0)
        textViewProgress.text = "Song ${song_number}: 0/0 Words "

        val map1FileName = "song_"+correct(song_number)+"_map1"
        Log.d(tag,"Attempting to open file with name: $map1FileName")
        val map1File = openFileInput(map1FileName)
        var map1Layer = KmlLayer(mMap,map1File,this)
        map1Layer.addLayerToMap()

    }
    private fun correct(n:Int):String{
        if (n<10)
            return "0"+n
        else
            return n.toString()
    }
}
