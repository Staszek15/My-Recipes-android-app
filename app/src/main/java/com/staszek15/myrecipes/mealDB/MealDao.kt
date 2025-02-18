package com.staszek15.myrecipes.mealDB

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

    @Query("SELECT * FROM meal WHERE mealId=:mealId")
    fun getExactMeal(mealId: Int): MealItemClass

    @Query("SELECT * FROM meal WHERE type=:mealType ORDER BY favourite DESC, title ASC")
    fun getTypeMeals(mealType: String): List<MealItemClass>

    @Query("SELECT * FROM meal WHERE favourite=1 ORDER BY title ASC")
    fun getFavMeals(): List<MealItemClass>

    @Update
    fun updateMeal(meal: MealItemClass)

    @Delete
    fun deleteMeal(meal: MealItemClass)
}