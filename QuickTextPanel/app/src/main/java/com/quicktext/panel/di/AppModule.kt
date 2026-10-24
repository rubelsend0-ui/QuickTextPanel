package com.quicktext.panel.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.quicktext.panel.data.local.AppDatabase
import com.quicktext.panel.data.local.BoxDao
import com.quicktext.panel.data.local.TemplateDao
import com.quicktext.panel.data.repository.SettingsRepository
import com.quicktext.panel.data.repository.TemplateRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "quicktext.db")
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        "INSERT INTO boxes (name, createdAt) VALUES ('General', ${System.currentTimeMillis()})",
                    )
                }
            })
            .build()

    @Provides
    fun provideBoxDao(db: AppDatabase): BoxDao = db.boxDao()

    @Provides
    fun provideTemplateDao(db: AppDatabase): TemplateDao = db.templateDao()
}

/** Lets the keyboard and overlay services (which are not Hilt components) reach the singletons. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface ServiceEntryPoint {
    fun templateRepository(): TemplateRepository
    fun settingsRepository(): SettingsRepository
}
