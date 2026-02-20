package com.odevs.photodiary.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PhotoEntity::class, CollageEntity::class], version = 2)
abstract class PhotoDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun collageDao(): CollageDao
}