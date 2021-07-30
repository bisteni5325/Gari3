package com.telematics.data.interceptor

import android.util.Log
import com.google.gson.Gson
import com.telematics.data.api.errors.ApiError
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject

class ErrorInterceptor @Inject constructor(
    private val gson: Gson
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val bodyString = response.body()!!.string()

        val code = response.code()

        Log.d(
            "ErrorInterceptor",
            "code: $code url: ${request.url().uri()} msg: ${response.message()}"
        )

        if (code != 200) {
            //throw RuntimeException(ApiError(code, response.message()))
        }

        try {
            if (!bodyString.isNullOrEmpty()) {
                val p = JSONObject(bodyString)
                val status = p.getInt("Status")
                Log.d("ErrorInterceptor", "apiResponse.status: ${status}")
                if (status != 200) {
                    throw ApiError(status)
                }
            }
        } catch (e: IOException) {

        }

        return response.newBuilder()
            .body(ResponseBody.create(response.body()?.contentType(), bodyString))
            .build()
    }
}