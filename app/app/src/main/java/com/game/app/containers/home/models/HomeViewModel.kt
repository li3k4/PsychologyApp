package com.game.app.containers.home.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.game.app.containers.base.BaseViewModel
import com.game.app.network.Resource
import com.game.app.repositories.UserRepository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

class HomeViewModel(
    private val repository: UserRepository
): BaseViewModel(repository) {
    // Данные обо всех категориях, которые существуют в системе
    private val _categories: MutableLiveData<Resource<Response<ResponseBody>>> = MutableLiveData()
    val categories: LiveData<Resource<Response<ResponseBody>>>
        get() = _categories

    // Данные о всех курсах, соответствующих данной категории
    private val _courses: MutableLiveData<Resource<Response<ResponseBody>>> = MutableLiveData()
    val courses: LiveData<Resource<Response<ResponseBody>>>
        get() = _courses

    // Данные об изображении конкретного курса
    private val _courseImage: MutableLiveData<Resource<Response<ResponseBody>>> = MutableLiveData()
    val courseImage: LiveData<Resource<Response<ResponseBody>>>
        get() = _courseImage

    fun getAllCategory() = viewModelScope.launch {
        _categories.value = Resource.Loading
        _categories.value = repository.getAllCategory()
    }

    fun getCoursesByTitle(title: String) = viewModelScope.launch {
        _courses.value = Resource.Loading
        _courses.value = repository.getCoursesByTitle(title)
    }

    fun getCourseTitleImage(coursesId: Int) = viewModelScope.launch {
        _courseImage.value = Resource.Loading
        _courseImage.value = repository.getCourseTitleImage((coursesId))
    }
}