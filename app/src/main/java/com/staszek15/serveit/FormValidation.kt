package com.staszek15.serveit

import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

data class TextInputEditTextUIModel(
    val isEnabled: Boolean? = null,
    val errorMessage: String? = null
)

fun TextInputEditText.validateRule(
    rules: List<ValidationRule>,
    callback: ((TextInputEditTextUIModel) -> Unit)? = null
): Boolean {
    val textInputLayout = this.parent.parent as TextInputLayout
    val viewEnabled = textInputLayout.isEnabled

    this.doAfterTextChanged {
        textInputLayout.error = null
        textInputLayout.isErrorEnabled = false
    }

    val validateText = this.text.toString().trim()

    for (i in rules.indices) {
        val isNotValid = rules[i].predicate.test(validateText)
        val message = rules[i].errorMessage
        if (isNotValid) {
            if (!viewEnabled) textInputLayout.isEnabled = true
            textInputLayout.error = message
            callback?.invoke(TextInputEditTextUIModel(isEnabled = viewEnabled))
            return false
        } else {
            continue
        }
    }
    return true
}