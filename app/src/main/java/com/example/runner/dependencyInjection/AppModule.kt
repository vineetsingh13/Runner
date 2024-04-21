package com.example.runner.dependencyInjection

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runner.OTHER.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runner.OTHER.Constants.KEY_NAME
import com.example.runner.OTHER.Constants.KEY_WEIGHT
import com.example.runner.OTHER.Constants.RUNNING_DATABASE_NAME
import com.example.runner.OTHER.Constants.SHARED_PREFERENCE_NAME
import com.example.runner.db.RunningDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
//singletonComponent determines when the instances will be created and destroyed
@InstallIn(SingletonComponent::class)
object AppModule {

    //singleton determines only one instance would be created
    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext context: Context
    )= Room.databaseBuilder(
        context,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()


    //here to getRunDao dagger has to create the database first
    //so it will create it automatically by the function we gave above
    //so in our repository we just create the variable for runDao
    //and dagger will automatically run the codes necessary for creating the dao
    //thus reducing the boiler plate code
    @Singleton
    @Provides
    fun provideRunDao(db:RunningDatabase)=db.getRunDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFERENCE_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME, "")?:""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences) =
        sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
}