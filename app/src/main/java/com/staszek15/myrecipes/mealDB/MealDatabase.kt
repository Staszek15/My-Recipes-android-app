package com.staszek15.myrecipes.mealDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.staszek15.myrecipes.Converters

@Database(entities = [MealItemClass::class], version = 3)
@TypeConverters(Converters::class)
abstract class MealDatabase : RoomDatabase() {

    abstract fun getMealDao(): MealDao


    companion object {

        // volatile to store this constant in main memory, always visible to any thread
        @Volatile
        private var DATABASE_INSTANCE: MealDatabase? = null

        fun getMealDatabase(context: Context): MealDatabase {
            // ?: elvis operator, return left if exists, otherwise return left
            // synchronized to make sure it is running only on 1 thread
            return DATABASE_INSTANCE ?: synchronized(this) {
                val instance = databaseBuilder(
                    context,
                    MealDatabase::class.java,
                    "meal-database"
                )
                    .createFromAsset("databases/meal.db")
                    .fallbackToDestructiveMigration()
                    .build()
                DATABASE_INSTANCE = instance
                instance
            }
        }
    }
}

