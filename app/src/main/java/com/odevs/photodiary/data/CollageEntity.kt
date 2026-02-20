package com.odevs.photodiary.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collages")
data class CollageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filename: String,
    val date: String
)