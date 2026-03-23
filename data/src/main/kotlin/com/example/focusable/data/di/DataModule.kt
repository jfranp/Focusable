package com.example.focusable.data.di

import androidx.room.Room
import com.example.focusable.data.local.datastore.PreferencesManager
import com.example.focusable.data.local.db.FocusDatabase
import com.example.focusable.data.notification.NotificationHelper
import com.example.focusable.data.remote.ApiService
import com.example.focusable.data.remote.MockRestInterceptor
import com.example.focusable.data.repository.FocusSessionRepositoryImpl
import com.example.focusable.data.repository.SessionSyncRepositoryImpl
import com.example.focusable.data.repository.UserPreferencesRepositoryImpl
import com.example.focusable.data.sensor.FocusSessionController
import com.example.focusable.data.sensor.MotionSamplerImpl
import com.example.focusable.data.sensor.NoiseSamplerImpl
import com.example.focusable.domain.repository.FocusSessionRepository
import com.example.focusable.domain.repository.SessionSyncRepository
import com.example.focusable.domain.repository.UserPreferencesRepository
import com.example.focusable.domain.sensor.MotionSampler
import com.example.focusable.domain.sensor.NoiseSampler
import com.example.focusable.domain.sensor.SensorTelemetryPort
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val dataModule = module {

    single {
        Room.databaseBuilder(
            get(),
            FocusDatabase::class.java,
            "focusable_database"
        ).build()
    }
    single { get<FocusDatabase>().focusSessionDao() }

    single { PreferencesManager(get()) }

    single { MockRestInterceptor() }
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<MockRestInterceptor>())
            .build()
    }
    single {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl("http://localhost/api/")
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single { get<Retrofit>().create(ApiService::class.java) }

    single<NoiseSampler> { NoiseSamplerImpl(get()) }
    single<MotionSampler> { MotionSamplerImpl(get()) }

    single { NotificationHelper(get()) }

    single { FocusSessionController(get(), get(), get(), get(), get()) }
    single<SensorTelemetryPort> { get<FocusSessionController>() }

    single<FocusSessionRepository> { FocusSessionRepositoryImpl(get(), get()) }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(get()) }
    single<SessionSyncRepository> { SessionSyncRepositoryImpl(get()) }
}
