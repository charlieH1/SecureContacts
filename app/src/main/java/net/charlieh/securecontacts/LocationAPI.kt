package net.charlieh.securecontacts


import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.http.Query

interface LocationService
{
    @GET("geocode/json?")
    fun latLong(@Query("address") Address:String, @Query("key")Key:String):Call<LocationData>

}