package com.example.weeklyupload.Respository

import androidx.lifecycle.LiveData
import com.example.weeklyupload.DAO.ImageDAO
import com.example.weeklyupload.Object.Image
import java.util.concurrent.Flow


class ImageRespository(val imageDAO: ImageDAO) {

    //fetch all trip data
    val fetchAllImage: LiveData<List<Image>> = imageDAO.getAlltrip()

    fun addImage(image: Image) {
        imageDAO.insert(image)

    }

    fun updateImage(image: Image) {
        return imageDAO.update(image)

    }


    fun deleteImage(image: Image) {
        imageDAO.delete(image)

    }

    fun getlastedImage(): kotlinx.coroutines.flow.Flow<List<Image>> {

        return imageDAO.getlastedImage()
    }


}