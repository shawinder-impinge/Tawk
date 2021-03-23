package com.app.tawk.data.remote.api


import com.app.tawk.model.User
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Service to fetch users and user detail
 */
interface ApiInterfaceService {

@GET("/users")
suspend fun getUsers(@Query("since") since: Int): Response<List<User>>

 @GET("/users/{username}")
 suspend fun getUserDetail(@Path("username") userName: String):Response<User>

}
