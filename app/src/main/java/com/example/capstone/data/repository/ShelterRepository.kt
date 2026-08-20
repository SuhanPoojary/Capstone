package com.example.capstone.data.repository

import android.content.Context
import com.example.capstone.data.local.database.dao.ShelterDao
import com.example.capstone.data.local.database.entity.ShelterEntity
import com.example.capstone.util.ShelterParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ShelterRepository(private val shelterDao: ShelterDao, private val context: Context) {

    val allShelters: Flow<List<ShelterEntity>> = shelterDao.getAllShelters()

    suspend fun refreshSheltersIfEmpty() {
        withContext(Dispatchers.IO) {
            val count = shelterDao.getCount()
            if (count == 0) {
                val shelters = ShelterParser.parseShelters(context)
                val entities = shelters.map {
                    ShelterEntity(
                        state = it.state,
                        district = it.district,
                        name = it.name,
                        disasterType = it.disasterType,
                        address = it.address,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        capacity = it.capacity,
                        inmates = it.inmates,
                        campType = it.campType,
                        year = it.year
                    )
                }
                shelterDao.insertAll(entities)
            }
        }
    }

    fun getSheltersByState(state: String): Flow<List<ShelterEntity>> {
        return shelterDao.getSheltersByState(state)
    }
}
