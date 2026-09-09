package com.example.capstone.presentation.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.capstone.data.local.database.AppDatabase
import com.example.capstone.data.repository.ShelterRepository
import com.example.capstone.data.repository.HospitalRepository
import com.example.capstone.presentation.viewmodel.ShelterViewModel
import com.example.capstone.util.ShelterClusterer
import com.example.capstone.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import com.example.capstone.data.SafeReadyPreferences
import com.example.capstone.data.local.database.entity.HospitalEntity
import com.example.capstone.data.local.database.entity.ShelterEntity
import org.osmdroid.config.Configuration
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.modules.IFilesystemCache
import org.osmdroid.tileprovider.modules.SqlTileWriter
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

/**
 * ShelterMapFragment displays shelter and hospital locations on an offline-capable map.
 * Implements custom grid-based clustering for high-density marker rendering.
 */
class ShelterMapFragment : Fragment() {
    private var mapView: MapView? = null
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var poiMarkers: FolderOverlay
    private lateinit var shelterClusterer: ShelterClusterer
    private lateinit var prefs: SafeReadyPreferences

    private val viewModel: ShelterViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val shelterRepo = ShelterRepository(database.shelterDao(), requireContext())
        val hospitalRepo = HospitalRepository(database.hospitalDao(), requireContext())
        ShelterViewModel.Factory(shelterRepo, hospitalRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // OSM configuration is now handled in SafeReadyApp
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = MapView(requireContext())
        mapView = view
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val map = mapView ?: return
        val ctx = requireContext().applicationContext
        prefs = SafeReadyPreferences(ctx)
        
        // 1. CONFIGURE TILE SOURCE WITH FALLBACK
        setupTileSource(map)
        applyEmergencyFilter(map)
        map.setMultiTouchControls(true)
        
        // 2. KEEP ZOOM BOUNDARIES SAFE
        map.minZoomLevel = 6.0 
        map.maxZoomLevel = 19.0
        
        val mapController = map.controller
        // Start closer to ground to avoid mass tile fetching
        mapController.setZoom(12.0)
        
        // Default focus on Mumbai region
        mapController.setCenter(GeoPoint(19.0760, 72.8777))
        
        // Add User Location Overlay
        myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), map)
        myLocationOverlay.enableMyLocation()
        map.overlays.add(myLocationOverlay)

        // Initialize ShelterClusterer
        shelterClusterer = ShelterClusterer(requireContext())

        // Initialize FolderOverlay
        poiMarkers = FolderOverlay()
        map.overlays.add(poiMarkers)

        // Load and Display Shelters
        observeShelters()

        // Center on user once location is found
        myLocationOverlay.runOnFirstFix {
            activity?.runOnUiThread {
                mapController.animateTo(myLocationOverlay.myLocation)
            }
        }

        // Add MapListener to re-cluster on zoom or scroll
        map.addMapListener(DelayedMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                // Only re-cluster if we aren't currently showing an info window
                // to prevent the bubble from disappearing immediately after tap
                if (!isAnyInfoWindowOpen()) {
                    updateClusters()
                    
                    // Fetch more hospitals for the current visible area
                    val box = map.boundingBox
                    lifecycleScope.launch {
                        viewModel.fetchHospitalsInArea(
                            box.latSouth, box.lonWest,
                            box.latNorth, box.lonEast
                        )
                    }
                }
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                updateClusters()
                val box = map.boundingBox
                lifecycleScope.launch {
                    viewModel.fetchHospitalsInArea(
                        box.latSouth, box.lonWest,
                        box.latNorth, box.lonEast
                    )
                }
                return true
            }
        }, 500)) // Increased delay to 500ms
    }

    private fun isAnyInfoWindowOpen(): Boolean {
        for (item in poiMarkers.items) {
            if (item is Marker && item.isInfoWindowShown) return true
        }
        return false
    }

    private fun setupTileSource(map: MapView) {
        // Use a customized OSM tile source that avoids the default 'Mapnik' string in requests if possible,
        // or switch to a more resilient public mirror like Wikimedia or Humanitarian.
        
        val humanitarianSource = XYTileSource(
            "OSM_HOT",
            0, 19, 256, ".png", 
            arrayOf(
                "https://a.tile.openstreetmap.fr/hot/",
                "https://b.tile.openstreetmap.fr/hot/",
                "https://c.tile.openstreetmap.fr/hot/"
            ),
            getString(R.string.map_attribution)
        )

        // Try to use HOT (Humanitarian) tiles first as they are often more lenient for disaster-related apps
        map.setTileSource(humanitarianSource)
        
        // Ensure the cache writer is configured
        try {
            val writer = SqlTileWriter()
            // This ensures we're writing to the new bucket
        } catch (e: Exception) {
            // Log cache init failure if necessary
        }
    }

    private fun applyEmergencyFilter(map: MapView) {
        if (prefs.getEmergencyModeEnabled()) {
            val inverseMatrix = ColorMatrix(floatArrayOf(
                -1.0f, 0.0f, 0.0f, 0.0f, 255.0f,
                0.0f, -1.0f, 0.0f, 0.0f, 255.0f,
                0.0f, 0.0f, -1.0f, 0.0f, 255.0f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            ))

            val destinationMatrix = ColorMatrix()
            destinationMatrix.setSaturation(0f)

            val shiftMatrix = ColorMatrix(floatArrayOf(
                0.5f, 0f, 0f, 0f, 0f,
                0f, 0.5f, 0f, 0f, 0f,
                0f, 0f, 0.5f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))

            destinationMatrix.postConcat(inverseMatrix)
            destinationMatrix.postConcat(shiftMatrix)

            val filter = ColorMatrixColorFilter(destinationMatrix)
            map.overlayManager.tilesOverlay.setColorFilter(filter)
        }
    }

    private fun observeShelters() {
        viewModel.allShelters.observe(viewLifecycleOwner) {
            updateClusters()
        }
        viewModel.allHospitals.observe(viewLifecycleOwner) {
            updateClusters()
        }
    }

    private fun updateClusters() {
        val map = mapView ?: return
        val shelters = viewModel.allShelters.value ?: emptyList()
        val hospitals = viewModel.allHospitals.value ?: emptyList()
        
        // Run clustering in a coroutine to keep UI responsive
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val markers = shelterClusterer.cluster(map, shelters, hospitals)
            
            withContext(Dispatchers.Main) {
                poiMarkers.items.clear()
                for (marker in markers) {
                    poiMarkers.add(marker)
                }
                map.invalidate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView = null
    }
}
