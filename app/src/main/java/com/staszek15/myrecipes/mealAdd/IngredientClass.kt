package com.staszek15.myrecipes.mealAdd

import kotlinx.serialization.Serializable

@Serializable
data class IngredientClass (
    var amount :String,
    var ingredient :String
)