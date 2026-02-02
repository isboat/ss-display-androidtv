package com.onscreensync.tvapp.di

import android.content.Context
import com.onscreensync.tvapp.services.LocalStorageService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocalStorageService(@ApplicationContext context: Context): LocalStorageService {
        return LocalStorageService(context)
    }
}
