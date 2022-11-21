package com.example.weeklyupload.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.weeklyupload.DB.ImageDB
import com.example.weeklyupload.Object.Image
import com.example.weeklyupload.Respository.ImageRespository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ImageViewModel(application: Application) : AndroidViewModel(application) {


    val imagelist: LiveData<List<Image>>
    private val respository: ImageRespository
    init {
        val imageDAO = ImageDB.getDB(application).imageDAO()
        respository = ImageRespository(imageDAO)
        imagelist = respository.fetchAllImage
    }
    fun addimage(image: Image) {
        viewModelScope.launch(Dispatchers.IO) {
            respository.addImage(image)
        }
    }
    fun deleteimage(image: Image) {
        viewModelScope.launch(Dispatchers.IO) {
            respository.deleteImage(image)
        }
    }
    fun getallImage(): LiveData<List<Image>> {
        return imagelist
    }
    fun getlastedImage(): LiveData<List<Image>> {
        return respository.getlastedImage().asLiveData()
    }

}

