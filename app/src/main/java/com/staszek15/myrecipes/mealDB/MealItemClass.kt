package com.staszek15.myrecipes.mealDB

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal")
data class MealItemClass(
    @PrimaryKey(autoGenerate = true) val mealId: Int = 0,
    val type: String,
    val title: String,
    val description: String,
    val ingredients: String?,
    val recipe: String,
    val image: Bitmap,
    val rating: Float,
    val favourite: Boolean = false
)
