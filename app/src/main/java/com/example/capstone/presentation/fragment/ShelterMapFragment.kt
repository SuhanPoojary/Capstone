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
import com.example.capstone.presentation.viewmodel.ShelterViewModel
import com.example.capstone.util.ShelterClusterer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.DelayedMapListener
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.File

/**
 * ShelterMapFragment displays shelter locations on an offline-capable map.
 * Implements custom grid-based clustering for high-density marker rendering.
 */
class ShelterMapFragment : Fragment() {
    private var mapView: MapView? = null
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private lateinit var poiMarkers: FolderOverlay
    private lateinit var shelterClusterer: ShelterClusterer

    private val viewModel: ShelterViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = ShelterRepository(database.shelterDao(), requireContext())
        ShelterViewModel.Factory(repository)
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
        setupOsmdroid(ctx)
        
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        
        val mapController = map.controller
        mapController.setZoom(12.0)
        
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
                updateClusters()
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                updateClusters()
                return true
            }
        }, 200)) // 200ms delay to avoid excessive clustering
    }

    private fun setupOsmdroid(ctx: Context) {
        val configuration = Configuration.getInstance()
        configuration.userAgentValue = "SafeReady-Android-App-${ctx.packageName}"
        
        val osmDataDir = ctx.getDir("osmdroid", Context.MODE_PRIVATE)
        configuration.osmdroidBasePath = osmDataDir
        configuration.osmdroidTileCache = File(osmDataDir, "tiles")
    }

    private fun observeShelters() {
        viewModel.allShelters.observe(viewLifecycleOwner) {
            updateClusters()
        }
    }

    private fun updateClusters() {
        val map = mapView ?: return
        val shelters = viewModel.allShelters.value ?: return
        
        // Run clustering in a coroutine to keep UI responsive
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val markers = shelterClusterer.cluster(map, shelters)
            
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
