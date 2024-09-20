package com.staszek15.myrecipes.meal

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal")
data class MealItemClass(
    @PrimaryKey(autoGenerate = true) val mealId: Int = 0,
    val type: String,
    val title: String,
    val description: String,
    val recipe: String,
    @DrawableRes
    val image: Int,
    val rating: Float,
    val favourite: Boolean = false
)
