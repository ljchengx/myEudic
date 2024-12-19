package com.ljchengx.eudic.di

import com.ljchengx.eudic.data.repository.WordRepository
import com.ljchengx.eudic.data.repository.WordRepositoryImpl
import com.ljchengx.eudic.ui.widget.WidgetSettingsRepository
import com.ljchengx.eudic.ui.widget.WidgetSettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindWidgetSettingsRepository(
        widgetSettingsRepositoryImpl: WidgetSettingsRepositoryImpl
    ): WidgetSettingsRepository

    @Binds
    @Singleton
    abstract fun bindWordRepository(
        wordRepositoryImpl: WordRepositoryImpl
    ): WordRepository
} 