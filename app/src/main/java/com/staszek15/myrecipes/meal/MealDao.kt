package com.staszek15.myrecipes.meal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MealDao {

    // table name is meal

    @Insert
    fun createMeal(meal: MealItemClass)

    @Query("SELECT * FROM meal")
    fun getAllMeals(): List<MealItemClass>

    @Query("SELECT * FROM meal WHERE type=:mealType")
    fun getTypeMeals(mealType: String): List<MealItemClass>

    @Query("SELECT * FROM meal WHERE favourite=1")
    fun getFavMeals(): List<MealItemClass>

    @Update
    fun updateMeal(meal: MealItemClass)

    @Delete
    fun deleteMeal(meal: MealItemClass)
}