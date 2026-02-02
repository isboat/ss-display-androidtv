package com.onscreensync.tvapp.di

import com.onscreensync.tvapp.Constants
import com.onscreensync.tvapp.apirequests.CodeActivationApiRequest
import com.onscreensync.tvapp.apirequests.ConfigApiRequest
import com.onscreensync.tvapp.apirequests.ContentDataApiRequest
import com.onscreensync.tvapp.apirequests.DeviceApiRequest
import com.onscreensync.tvapp.apirequests.TokenApiRequest
import com.onscreensync.tvapp.network.AuthInterceptor
import com.onscreensync.tvapp.network.BaseUrlInterceptor
import com.onscreensync.tvapp.network.TokenAuthenticator
import com.onscreensync.tvapp.signalR.SignalRServerApiRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        baseUrlInterceptor: BaseUrlInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(baseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.ONSCREENSYNC_ENDPOINT_BASEURL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideConfigApiRequest(retrofit: Retrofit): ConfigApiRequest {
        return retrofit.create(ConfigApiRequest::class.java)
    }

    @Provides
    @Singleton
    fun provideContentDataApiRequest(retrofit: Retrofit): ContentDataApiRequest {
        return retrofit.create(ContentDataApiRequest::class.java)
    }

    @Provides
    @Singleton
    fun provideSignalRServerApiRequest(retrofit: Retrofit): SignalRServerApiRequest {
        return retrofit.create(SignalRServerApiRequest::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenApiRequest(retrofit: Retrofit): TokenApiRequest {
        return retrofit.create(TokenApiRequest::class.java)
    }

    @Provides
    @Singleton
    fun provideCodeActivationApiRequest(retrofit: Retrofit): CodeActivationApiRequest {
        return retrofit.create(CodeActivationApiRequest::class.java)
    }

    @Provides
    @Singleton
    fun provideDeviceApiRequest(retrofit: Retrofit): DeviceApiRequest {
        return retrofit.create(DeviceApiRequest::class.java)
    }
}
