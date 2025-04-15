package com.staszek15.myrecipes

import com.google.android.material.textfield.TextInputEditText

fun validatorLogIn(etEmail: TextInputEditText, etPassword: TextInputEditText): Boolean {
    val isEmailValid = etEmail.validateRule(
        rules = listOf(
            EmptyTextRule(),
            EmailTextRule()
        )
    )
    val isPasswordValid = etPassword.validateRule(
        rules = listOf(
            EmptyTextRule(),
            PasswordLengthTextRule()
        )
    )
    return isEmailValid && isPasswordValid
}


fun validatorRegister(
    etEmail: TextInputEditText,
    etPassword: TextInputEditText,
    etRepeatPassword: TextInputEditText
): Boolean {
    val isEmailValid = etEmail.validateRule(
        rules = listOf(
            EmptyTextRule(),
            EmailTextRule()
        )
    )
    val isPasswordValid = etPassword.validateRule(
        rules = listOf(
            EmptyTextRule(),
            PasswordLengthTextRule()
        )
    )
    val isRepeatPasswordValid = etRepeatPassword.validateRule(
        rules = listOf(
            EmptyTextRule(),
            RepeatPasswordTextRule(password = etPassword.text.toString())
        )
    )
    return isEmailValid && isPasswordValid && isRepeatPasswordValid
}

fun validatorRemindPassword(etEmail: TextInputEditText): Boolean {
    val isEmailValid = etEmail.validateRule(
        rules = listOf(
            EmptyTextRule(),
            EmailTextRule()
        )
    )
    return isEmailValid
}


fun validatorAddMeal(etTitle: TextInputEditText, etDescription: TextInputEditText, etRecipe: TextInputEditText): Boolean {
    val isTitle = etTitle.validateRule(
        rules = listOf(
            EmptyTextRule(),
        )
    )
    val isDescription = etDescription.validateRule(
        rules = listOf(
            EmptyTextRule()
        )
    )
    val isRecipe = etRecipe.validateRule(
        rules = listOf(
            EmptyTextRule()
        )
    )
    return isTitle && isDescription && isRecipe
}