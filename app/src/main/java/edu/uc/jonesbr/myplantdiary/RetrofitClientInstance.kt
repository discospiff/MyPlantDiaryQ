package edu.uc.jonesbr.myplantdiary

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClientInstance {

    private var retrofit: Retrofit? = null;
    private val BASE_URL = "https://www.plantplaces.com"

    val retrofitInstance : Retrofit?
        get() {
            // has this object been created yet?
            if (retrofit == null) {
                // create it!
                retrofit = retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                print("fo")
            }
            return retrofit
        }

}