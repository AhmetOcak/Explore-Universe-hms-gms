package com.spaceapp.data.datasource.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spaceapp.data.utils.RoomTables

@Entity(tableName = RoomTables.APOD_TABLE)
data class ApodEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "image_url")
    var image: String
)
