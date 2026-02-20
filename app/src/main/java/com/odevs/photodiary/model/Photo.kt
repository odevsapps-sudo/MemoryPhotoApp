package com.odevs.photodiary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val filename: String,
    val uri: String?,
    val dayOfWeek: Int      // 0 = Hétfő, 6 = Vasárnap
)
