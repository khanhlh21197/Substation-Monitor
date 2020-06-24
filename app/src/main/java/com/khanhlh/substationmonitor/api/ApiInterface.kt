package com.khanhlh.substationmonitor.api

import com.khanhlh.substationmonitor.model.Photos
import io.reactivex.Observable
import retrofit2.http.GET

interface ApiInterface {

    @GET("list")
    fun getPhotos(): Observable<List<Photos>>
}