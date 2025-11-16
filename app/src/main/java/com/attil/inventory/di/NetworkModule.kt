package com.attil.inventory.di

import android.content.Context
import com.attil.inventory.BuildConfig
import com.attil.inventory.data.remote.*
import com.attil.inventory.data.repository.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Auth Services
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: AuthApiService, @ApplicationContext context: Context, gson: Gson): AuthRepository =
        AuthRepository(apiService, context, gson)

    // Master Data Services
    @Provides
    @Singleton
    fun provideRoleApiService(retrofit: Retrofit): RoleApiService =
        retrofit.create(RoleApiService::class.java)

    @Provides
    @Singleton
    fun provideRoleRepository(apiService: RoleApiService): RoleRepository =
        RoleRepository(apiService)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService =
        retrofit.create(UserApiService::class.java)

    @Provides
    @Singleton
    fun provideUserRepository(apiService: UserApiService): UserRepository =
        UserRepository(apiService)

    // Management Services
    @Provides
    @Singleton
    fun provideGodownApiService(retrofit: Retrofit): GodownApiService =
        retrofit.create(GodownApiService::class.java)

    @Provides
    @Singleton
    fun provideGodownRepository(apiService: GodownApiService): GodownRepository =
        GodownRepository(apiService)

    @Provides
    @Singleton
    fun provideRackApiService(retrofit: Retrofit): RackApiService =
        retrofit.create(RackApiService::class.java)

    @Provides
    @Singleton
    fun provideRackRepository(apiService: RackApiService): RackRepository =
        RackRepository(apiService)

    @Provides
    @Singleton
    fun provideCategoryApiService(retrofit: Retrofit): CategoryApiService =
        retrofit.create(CategoryApiService::class.java)

    @Provides
    @Singleton
    fun provideCategoryRepository(apiService: CategoryApiService): CategoryRepository =
        CategoryRepository(apiService)

    @Provides
    @Singleton
    fun provideCuisineApiService(retrofit: Retrofit): CuisineApiService =
        retrofit.create(CuisineApiService::class.java)

    @Provides
    @Singleton
    fun provideCuisineRepository(apiService: CuisineApiService): CuisineRepository =
        CuisineRepository(apiService)

    @Provides
    @Singleton
    fun provideVendorApiService(retrofit: Retrofit): VendorApiService =
        retrofit.create(VendorApiService::class.java)

    @Provides
    @Singleton
    fun provideVendorRepository(apiService: VendorApiService): VendorRepository =
        VendorRepository(apiService)

    @Provides
    @Singleton
    fun provideItemApiService(retrofit: Retrofit): ItemApiService =
        retrofit.create(ItemApiService::class.java)

    @Provides
    @Singleton
    fun provideItemRepository(apiService: ItemApiService): ItemRepository =
        ItemRepository(apiService)

    @Provides
    @Singleton
    fun provideUsageApiService(retrofit: Retrofit): UsageApiService =
        retrofit.create(UsageApiService::class.java)

    @Provides
    @Singleton
    fun provideUsageRepository(apiService: UsageApiService): UsageRepository =
        UsageRepository(apiService)

    // Transaction Services
    @Provides
    @Singleton
    fun provideInwardApiService(retrofit: Retrofit): InwardApiService =
        retrofit.create(InwardApiService::class.java)

    @Provides
    @Singleton
    fun provideInwardRepository(apiService: InwardApiService): InwardRepository =
        InwardRepository(apiService)

    @Provides
    @Singleton
    fun provideOutwardApiService(retrofit: Retrofit): OutwardApiService =
        retrofit.create(OutwardApiService::class.java)

    @Provides
    @Singleton
    fun provideOutwardRepository(apiService: OutwardApiService): OutwardRepository =
        OutwardRepository(apiService)

    @Provides
    @Singleton
    fun provideCurrentStockApiService(retrofit: Retrofit): CurrentStockApiService =
        retrofit.create(CurrentStockApiService::class.java)

    @Provides
    @Singleton
    fun provideCurrentStockRepository(apiService: CurrentStockApiService): CurrentStockRepository =
        CurrentStockRepository(apiService)

    @Provides
    @Singleton
    fun provideIndentApiService(retrofit: Retrofit): IndentApiService =
        retrofit.create(IndentApiService::class.java)

    @Provides
    @Singleton
    fun provideIndentRepository(apiService: IndentApiService): IndentRepository =
        IndentRepository(apiService)

    // Report Services
    @Provides
    @Singleton
    fun provideReportApiService(retrofit: Retrofit): ReportApiService =
        retrofit.create(ReportApiService::class.java)

    @Provides
    @Singleton
    fun provideReportRepository(apiService: ReportApiService): ReportRepository =
        ReportRepository(apiService)


}