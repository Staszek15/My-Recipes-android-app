package com.staszek15.myrecipes

import androidx.annotation.DrawableRes

data class MealItemClass(
    val heading: String,
    val description: String,
    @DrawableRes
    val image: Int,
    val rating: Float
)
