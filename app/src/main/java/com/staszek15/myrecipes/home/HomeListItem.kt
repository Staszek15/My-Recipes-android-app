package com.staszek15.myrecipes.home

import androidx.annotation.DrawableRes

data class HomeListItem(
    val heading: String,
    val description: String,
    @DrawableRes
    val image: Int,
    val rating: Float
)
