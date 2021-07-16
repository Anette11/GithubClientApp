package com.example.githubclientapp.api

import com.example.githubclientapp.models.UserSingleRepository
import com.example.githubclientapp.util.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ClientWebservice {
    @GET("/users/{username}/repos")
    suspend fun getUserAllRepositories(
        @Path("username") userName: String,
        @Query("page") page: Int = Constants.DEFAULT_PAGE
    ): Response<List<UserSingleRepository>>
}