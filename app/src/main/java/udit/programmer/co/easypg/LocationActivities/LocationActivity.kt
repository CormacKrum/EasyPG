package udit.programmer.co.easypg.LocationActivities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mapbox.android.core.location.*
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.mapbox.mapboxsdk.plugins.traffic.TrafficPlugin
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_location.*
import udit.programmer.co.easypg.R
import java.lang.Exception
import java.lang.ref.WeakReference

class LocationActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener {

    private lateinit var firebaseDatabase: DatabaseReference

    private lateinit var mapView: MapView
    var mapboxMap: MapboxMap? = null
    private val geojsonSourceLayerId = "geojsonSourceLayerId"
    private val symbolIconId = "symbolIconId"
    private val REQUEST_CODE_AUTOCOMPLETE = 1

    private val DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID"
    private var hoveringMarker: ImageView? = null
    private var droppedMarkerLayer: Layer? = null

    var lat: Double = 0.0
    var lng: Double = 0.0

    private lateinit var permissionsManager: PermissionsManager

    private var locationEngine: LocationEngine? = null
    private val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
    private val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5

    private lateinit var request: LocationEngineRequest
    private var callback = SearchPickActivityLocationCallback(this)

    private lateinit var dialog: AlertDialog

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_location)

        mapView = findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        firebaseDatabase = FirebaseDatabase.getInstance().getReference("PGs")
        dialog = SpotsDialog.Builder().setContext(this).setCancelable(false).build()

        fab_navigate_pick_btn.setOnClickListener {
            locationEngine!!.requestLocationUpdates(request, callback, mainLooper)
        }

        fab_done_btn.setOnClickListener {
            dialog.show()
            locationEngine!!.removeLocationUpdates(callback)
            val map = mutableMapOf<String, Any>()
            map["latitude"] = lat.toString()
            map["longitude"] = lng.toString()
            firebaseDatabase.child(FirebaseAuth.getInstance().currentUser!!.uid)
                .updateChildren(map).addOnSuccessListener {
                    dialog.dismiss()
                    Toast.makeText(this, "Location Stored :)", Toast.LENGTH_LONG).show()
                    Thread.sleep(2000)
                    onBackPressed()
                }.addOnFailureListener {
                    Toast.makeText(this, "FAILED : $it", Toast.LENGTH_LONG).show()
                }
        }

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.OUTDOORS) { style ->
            TrafficPlugin(mapView, mapboxMap, style).setVisibility(true)

            enableLocationComponent(style)

            initSearchFab()

            val drawable = ResourcesCompat.getDrawable(
                resources, R.mipmap.location_green, null
            )
            val bitmap = BitmapUtils.getBitmapFromDrawable(drawable)
            style.addImage(symbolIconId, bitmap!!)

            setUpSource(style)
            setUpLayer(style)

            hoveringMarker = ImageView(this)
            hoveringMarker!!.setImageResource(R.mipmap.location_red)
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER
            )
            hoveringMarker!!.layoutParams = params
            mapView!!.addView(hoveringMarker)

            initDroppedMarker(style)

            fab_location_pick_btn.setOnClickListener {
                locationEngine!!.removeLocationUpdates(callback)
                if (hoveringMarker!!.visibility == View.VISIBLE) {
                    val mapTargetLatLng = mapboxMap.cameraPosition.target;

                    hoveringMarker!!.visibility = View.INVISIBLE;
                    fab_location_pick_btn.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.BlueViolet)
                    );
                    fab_location_pick_btn.text = "Cancel";
                    Toast.makeText(this, "Selected :)", Toast.LENGTH_LONG).show()
                    fab_done_btn.isClickable = true

                    if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                        val source = style.getSourceAs<GeoJsonSource>("dropped-marker-source-id");
                        source?.setGeoJson(
                            Point.fromLngLat(
                                mapTargetLatLng.longitude,
                                mapTargetLatLng.latitude
                            )
                        )
                        droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID)
                    }
                } else {
                    fab_location_pick_btn.setBackgroundColor(
                        ContextCompat.getColor(this, R.color.DodgerBlue)
                    )
                    fab_location_pick_btn.text = "PICK";
                    hoveringMarker!!.visibility = View.VISIBLE;
                    droppedMarkerLayer = style.getLayer(DROPPED_MARKER_LAYER_ID);
                    fab_done_btn.isClickable = true
                }
            }
        }
    }

    private fun initDroppedMarker(it: Style) {

        val drawable = ResourcesCompat.getDrawable(
            resources, R.mipmap.location_blue, null
        )
        val bitmap = BitmapUtils.getBitmapFromDrawable(drawable)
        it.addImage("dropped-icon-image", bitmap!!)

        it.addSource(GeoJsonSource("dropped-marker-source-id"))
        it.addLayer(
            SymbolLayer(
                DROPPED_MARKER_LAYER_ID,
                "dropped-marker-source-id"
            ).withProperties(
                PropertyFactory.iconImage("dropped-icon-image"),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true)
            )
        )
    }

    @SuppressLint("LogNotTimber", "MissingPermission")
    private fun enableLocationComponent(it: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponent = mapboxMap!!.locationComponent
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, it).build()
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
            initLocationEngine()
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        request = LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
            .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine!!.requestLocationUpdates(request, callback, mainLooper);
        locationEngine!!.getLastLocation(callback)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, "Explanation Needed", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) mapboxMap!!.getStyle { enableLocationComponent(it) }
        else Toast.makeText(this, "Permissions Not Granted", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_AUTOCOMPLETE && resultCode == Activity.RESULT_OK) {
            locationEngine!!.removeLocationUpdates(callback)
            val selectedCameraFeature = PlaceAutocomplete.getPlace(data)
            if (mapboxMap != null) {
                if (mapboxMap!!.style != null) {
                    if (mapboxMap!!.style!!.getSourceAs<GeoJsonSource>(geojsonSourceLayerId) != null) {
                        mapboxMap!!.style!!.getSourceAs<GeoJsonSource>(geojsonSourceLayerId)!!
                            .setGeoJson(
                                FeatureCollection.fromFeature(
                                    com.mapbox.geojson.Feature.fromJson(
                                        selectedCameraFeature.toJson()
                                    )
                                )
                            )
                    }

                    lat = (selectedCameraFeature.geometry() as com.mapbox.geojson.Point).latitude()
                    lng = (selectedCameraFeature.geometry() as com.mapbox.geojson.Point).longitude()
                    mapboxMap!!.animateCamera(
                        com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(LatLng(lat, lng)).zoom(14.0)
                                .build()
                        ), 4000
                    )

                }
            }

        }
    }

    private fun setUpLayer(it: Style) {
        it.addLayer(
            SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId).withProperties(
                PropertyFactory.iconImage(symbolIconId),
                PropertyFactory.iconOffset(arrayOf(0f, -8f))
            )
        )
    }

    private fun setUpSource(it: Style) {
        it.addSource(GeoJsonSource(geojsonSourceLayerId))
    }

    private fun initSearchFab() {
        fab_location_search_btn.setOnClickListener {
            locationEngine!!.removeLocationUpdates(callback)
            val intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(
                    if (Mapbox.getAccessToken() != null) Mapbox.getAccessToken()!! else getString(
                        R.string.mapbox_access_token
                    )
                ).placeOptions(
                    PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS)
                ).build(this)
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

}

class SearchPickActivityLocationCallback(activity: LocationActivity?) :
    LocationEngineCallback<LocationEngineResult?> {
    private val activityWeakReference: WeakReference<LocationActivity?>?

    init {
        activityWeakReference = WeakReference(activity)
    }

    override fun onSuccess(result: LocationEngineResult?) {
        val activity: LocationActivity = activityWeakReference!!.get()!!
        if (activity != null) {
            val location = result!!.lastLocation ?: return
            activity.lat = location.latitude
            activity.lng = location.longitude
            if (activity.mapboxMap != null && result.lastLocation != null) {
                activity.mapboxMap!!.locationComponent
                    .forceLocationUpdate(result.lastLocation)
                activity.mapboxMap!!.animateCamera(
                    com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(activity.lat, activity.lng)).zoom(14.0)
                            .build()
                    ), 4000
                )
            }
        }
    }

    override fun onFailure(exception: Exception) {
        val activity: LocationActivity = activityWeakReference!!.get()!!
        if (activity != null) {
            Toast.makeText(
                activity, exception.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
