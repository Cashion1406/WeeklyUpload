package com.example.weeklyupload.DAO

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.weeklyupload.Object.Image
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDAO {

    @Insert
    fun insert(image: Image)

    @Delete
    fun delete(image: Image)

    @Query("DELETE FROM image_table")
    fun deleteALL()

    @Query("SELECT * FROM image_table ORDER BY ID DESC ")
    fun getAlltrip(): LiveData<List<Image>>

    @Query("SELECT * FROM image_table ORDER BY id DESC LIMIT 1")
    fun getlastedImage():Flow<List<Image>>
}