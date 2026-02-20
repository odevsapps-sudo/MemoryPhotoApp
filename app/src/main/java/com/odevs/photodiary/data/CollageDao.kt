package com.odevs.photodiary.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CollageDao {

    @Query("SELECT * FROM collages")
    fun getAllCollages(): Flow<List<CollageEntity>>

    @Insert
    suspend fun insertCollage(collage: CollageEntity)

    @Query("DELETE FROM collages")
    suspend fun clearCollages()
}
