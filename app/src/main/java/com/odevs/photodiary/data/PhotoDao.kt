package com.odevs.photodiary.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Query("SELECT * FROM photos")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("DELETE FROM photos WHERE date = :date")
    suspend fun deletePhotoByDate(date: String)

    @Insert
    suspend fun insertCollage(collage: CollageEntity)

    @Query("SELECT * FROM collages ORDER BY date DESC")
    fun getAllCollages(): Flow<List<CollageEntity>>
}
