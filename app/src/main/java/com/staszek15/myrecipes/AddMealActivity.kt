package com.staszek15.myrecipes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.drawToBitmap
import com.staszek15.myrecipes.databinding.ActivityAddMealBinding
import com.staszek15.myrecipes.meal.MealItemClass

class AddMealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMealBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdownMenu()
        handleImageSelection()
        //handleClickListeners()
    }

    private fun handleClickListeners() {

    }

    private fun setupDropdownMenu() {
        val items = listOf("Dinner", "Breakfast", "Dessert", "Shake", "Alcohol", "Decoration")
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, items)
        (binding.dropdownTextfield.editText as? AutoCompleteTextView)?.setAdapter(adapter)
    }


    private fun handleImageSelection() {
        val viewImage = binding.imageViewAdd

        val changeImage =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val data = it.data
                    val imgUri = data?.data
                    viewImage.scaleType = ImageView.ScaleType.CENTER_CROP   //set scale type
                    viewImage.setImageURI(imgUri)
                }
            }
        binding.cardViewImageAdd.setOnClickListener {
            val intentSelectImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            changeImage.launch(intentSelectImage)
        }
    }

    // lose focus (hide keyboard) when clicked outside of text field
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}