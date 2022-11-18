package com.example.weeklyupload.Object

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "image_table")
class Image(

    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val path: String,
    val location: String

) : Parcelable