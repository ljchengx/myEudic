package com.ljchengx.eudic.widget

import android.content.Context
import com.ljchengx.eudic.data.entity.WordEntity
import com.ljchengx.eudic.data.repository.WordRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow

object WordWidgetManager {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WordWidgetDependencies {
        fun repository(): WordRepository
    }

    fun getRepository(context: Context): WordRepository {
        val appContext = context.applicationContext
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            WordWidgetDependencies::class.java
        )
        return hiltEntryPoint.repository()
    }

    fun getAllWords(context: Context): Flow<List<WordEntity>> = 
        getRepository(context).getAllWords()
} 