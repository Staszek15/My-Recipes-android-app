package com.staszek15.myrecipes

import android.util.Patterns
import androidx.core.util.Predicate

open class ValidationRule(
    open val errorMessage: String = "*This field is required.",
    val predicate: Predicate<String>
)

class EmptyTextRule(
    override val errorMessage: String = "*This field can not be empty."
) : ValidationRule(predicate = {it.isNullOrEmpty()})

class EmailTextRule(
    override val errorMessage: String = "*This email is not valid."
) : ValidationRule(predicate = {!Patterns.EMAIL_ADDRESS.matcher(it).matches()})

class PasswordLengthTextRule(
    override val errorMessage: String = "*Password should have at least 6 characters"
) : ValidationRule(predicate = {it.length < 6})

class RepeatPasswordTextRule(
    override val errorMessage: String = "*Password does not match the original.",
    private val password :String?
) : ValidationRule(predicate = {it != password})
