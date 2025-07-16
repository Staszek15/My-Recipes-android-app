package com.staszek15.serveit.mealDB

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "meal")
@Parcelize
data class MealItemClass(
    @PrimaryKey(autoGenerate = true) val mealId: Int = 0,
    val type: String ="",
    val title: String = "",
    val description: String = "",
    val ingredients: String? = "",
    val recipe: String = "",
    val imageUrl: String? = null,
    val drawableName: String? = null,
    val rating: Float = 0f,
    val favourite: Boolean = false
) : Parcelable
