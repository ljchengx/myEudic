package com.ljchengx.eudic.di

import android.content.Context
import com.ljchengx.eudic.data.AppDatabase
import com.ljchengx.eudic.data.dao.RequestRecordDao
import com.ljchengx.eudic.data.dao.WordDao
import com.ljchengx.eudic.data.repository.WordRepository
import com.ljchengx.eudic.network.WordService
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideWordDao(database: AppDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    fun provideRequestRecordDao(database: AppDatabase): RequestRecordDao {
        return database.requestRecordDao()
    }

    @Provides
    @Singleton
    fun provideWordService(): WordService {
        return WordService()
    }

    @Provides
    @Singleton
    fun provideWordRepository(
        wordDao: WordDao,
        requestRecordDao: RequestRecordDao,
        wordService: WordService
    ): WordRepository {
        return WordRepository(wordDao, requestRecordDao, wordService)
    }
} 