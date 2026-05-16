package com.openhands.android.di

import android.content.Context
import com.openhands.android.data.remote.RuntimeApi
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideBaseUrl(@ApplicationContext context: Context): String {
        val prefs = context.getSharedPreferences("openhands_prefs", Context.MODE_PRIVATE)
        return prefs.getString("server_url", "https://app.all-hands.dev") ?: "https://app.all-hands.dev"
    }
    
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().build()
    }
    
    @Provides
    @Singleton
    fun provideRuntimeApi(
        client: OkHttpClient,
        moshi: Moshi,
        baseUrl: String
    ): RuntimeApi {
        return RuntimeApi(client, moshi, baseUrl)
    }
}