package com.veoride.terryschmidt.tweetexplosion


import com.twitter.sdk.android.core.TwitterApiClient
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.models.User

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

class TweetExplosionApiClient(session: TwitterSession) : TwitterApiClient(session) {
    val customService: GetUsersShowAPICustomService
        get() = getService(GetUsersShowAPICustomService::class.java)
}

interface GetUsersShowAPICustomService {
    @GET("/1.1/users/show.json")
    fun show(@Query("user_id") userId: Long): Call<User>

    /*
    * In retrofit v1 you could write like this
    *
    * @GET("/1.1/users/show.json")
    * void show(@Query("user_id") Long userId, Callback<User> cb);
    *
    * */
}