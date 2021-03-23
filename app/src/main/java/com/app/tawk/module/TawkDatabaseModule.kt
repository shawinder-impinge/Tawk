package com.app.tawk.module

import android.app.Application
import com.app.tawk.data.local.UserListDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class TawkDatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(application: Application) = UserListDatabase.getInstance(application)

    @Singleton
    @Provides
    fun providePostsDao(database: UserListDatabase) = database.getPostsDao()
}
