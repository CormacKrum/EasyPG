package udit.programmer.co.easypg.LocationActivities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import kotlinx.android.synthetic.main.activity_p_g_locate.*
import udit.programmer.co.easypg.Models.PG
import udit.programmer.co.easypg.R

class PGLocateActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var lat = 0.0
    private var lng = 0.0

    private var pgName: String = ""

    private val SOURCE_ID = "SOURCE_ID"
    private val ICON_ID: String? = "ICON_ID"
    private val LAYER_ID = "LAYER_ID"
    private var markList = mutableListOf<Feature>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_p_g_locate)

        val pgid = intent.getStringExtra("Ceased Meteor")

        FirebaseDatabase.getInstance().getReference("PGs").child(pgid)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pg = snapshot.getValue(PG::class.java)
                    lat = pg!!.latitude.toDouble()
                    lng = pg.longitude.toDouble()
                    pgName = pg.pgName
                    markList.add(Feature.fromGeometry(Point.fromLngLat(lat, lng)))
                }
            })

        mapView = findViewById(R.id.map_view_pg)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(mapBoxmap: MapboxMap) {
        mapBoxmap.setStyle(Style.OUTDOORS) { style ->
            val drawable = ResourcesCompat.getDrawable(
                resources, R.mipmap.pg_icon, null
            )
            val bitmap = BitmapUtils.getBitmapFromDrawable(drawable)
            val options = MarkerOptions()
            options.title(pgName)
            options.icon(IconFactory.getInstance(this).fromBitmap(bitmap!!))
            options.position = LatLng(lat, lng)
            mapBoxmap.addMarker(options)
            mapBoxmap.animateCamera(
                com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(lat, lng)).zoom(14.0)
                        .build()
                ), 4000
            )
        }

    }
}