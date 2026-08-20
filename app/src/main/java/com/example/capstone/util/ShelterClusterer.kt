package com.example.capstone.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.capstone.R
import com.example.capstone.data.local.database.entity.ShelterEntity
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class ShelterClusterer(private val context: Context) {
    private val gridSize = 150 // Grid size in pixels

    fun cluster(mapView: MapView, shelters: List<ShelterEntity>): List<Marker> {
        if (shelters.isEmpty()) return emptyList()

        val projection = mapView.projection
        val clusters = mutableMapOf<Pair<Int, Int>, MutableList<ShelterEntity>>()
        
        for (shelter in shelters) {
            val geoPoint = GeoPoint(shelter.latitude, shelter.longitude)
            val screenPoint = projection.toPixels(geoPoint, null)
            
            val gridX = screenPoint.x / gridSize
            val gridY = screenPoint.y / gridSize
            
            val key = Pair(gridX, gridY)
            clusters.getOrPut(key) { mutableListOf<ShelterEntity>() }.add(shelter)
        }
        
        val markers = mutableListOf<Marker>()
        val shelterIcon = ContextCompat.getDrawable(context, R.drawable.ic_shelter)

        for (clusterShelters in clusters.values) {
            if (clusterShelters.size == 1) {
                val shelter = clusterShelters[0]
                markers.add(createShelterMarker(mapView, shelter, shelterIcon))
            } else {
                markers.add(createClusterMarker(mapView, clusterShelters))
            }
        }
        
        return markers
    }

    private fun createShelterMarker(mapView: MapView, shelter: ShelterEntity, icon: Drawable?): Marker {
        val marker = Marker(mapView)
        marker.position = GeoPoint(shelter.latitude, shelter.longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = icon
        marker.title = shelter.name
        marker.snippet = """
            Type: ${shelter.disasterType}
            Address: ${shelter.address}
            Capacity: ${shelter.capacity} (Inmates: ${shelter.inmates})
            Camp Type: ${shelter.campType}
        """.trimIndent()
        return marker
    }

    private fun createClusterMarker(mapView: MapView, clusterShelters: List<ShelterEntity>): Marker {
        val avgLat = clusterShelters.map { it.latitude }.average()
        val avgLon = clusterShelters.map { it.longitude }.average()
        
        val marker = Marker(mapView)
        marker.position = GeoPoint(avgLat, avgLon)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        marker.icon = createClusterIcon(clusterShelters.size)
        marker.title = "${clusterShelters.size} Shelters"
        marker.setOnMarkerClickListener { _, _ ->
            mapView.controller.zoomIn()
            mapView.controller.animateTo(GeoPoint(avgLat, avgLon))
            true
        }
        return marker
    }

    private fun createClusterIcon(count: Int): BitmapDrawable {
        val size = 120
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        
        // Draw circle
        paint.color = Color.parseColor("#E91E63") // Material Pink
        paint.style = Paint.Style.FILL
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        
        // Draw border
        paint.color = Color.WHITE
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2.5f, paint)
        
        // Draw text
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        paint.textSize = 40f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        val text = count.toString()
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        canvas.drawText(text, size / 2f, size / 2f - textBounds.centerY(), paint)
        
        return BitmapDrawable(context.resources, bitmap)
    }
}
