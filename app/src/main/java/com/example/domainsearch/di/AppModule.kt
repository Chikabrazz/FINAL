package com.example.domainsearch.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.example.domainsearch.fragments.history.HistoryLoader
import com.example.domainsearch.util.UiActions
import com.example.domainsearch.util.UiActionsImpl
import javax.inject.Singleton

/**
 * AppModule for DI
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideUiActions(@ApplicationContext context: Context): UiActions = UiActionsImpl(context)

    @Singleton
    @Provides
    fun provideHistoryLoader(@ApplicationContext context: Context): HistoryLoader =
        HistoryLoader(provideUiActions(context))
}
