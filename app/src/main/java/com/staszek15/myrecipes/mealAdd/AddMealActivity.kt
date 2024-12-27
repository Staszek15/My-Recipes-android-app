package com.staszek15.myrecipes.mealAdd

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.ActivityAddMealBinding
import com.staszek15.myrecipes.mealDB.MealDatabase
import com.staszek15.myrecipes.mealDB.MealItemClass
import com.staszek15.myrecipes.mealList.AddIngredientAdapter

class AddMealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMealBinding
    private var ingredientsList = mutableListOf(IngredientClass("",""))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdownMenu()
        handleImageSelection()
        setupIngredientRecyclerView(ingredientsList)
        handleClickListeners()
    }

    private fun setupIngredientRecyclerView(ingredientsList :MutableList<IngredientClass>) {
        val adapterIngredients = AddIngredientAdapter(ingredientsList)
        binding.rvIngredients.adapter = adapterIngredients
        binding.rvIngredients.layoutManager = LinearLayoutManager(applicationContext)
    }


    private fun handleClickListeners() {

        // button add ingredient
        binding.ButtonAddIngredient.setOnClickListener {
            ingredientsList.add(IngredientClass("",""))
            binding.rvIngredients.adapter?.notifyItemInserted(ingredientsList.size - 1)
        }
        
        // button add meal
        binding.buttonAdd.setOnClickListener {
            val newMeal = MealItemClass(
                type = binding.dropdownType.text.toString(),
                title = binding.editTextTitle.text.toString(),
                description = binding.editTextDescription.text.toString(),
                recipe = binding.editTextRecipe.text.toString(),
                image = binding.imageViewAdd.drawToBitmap(),
                rating = binding.ratingBar.rating,
                favourite = false
            )
            // TODO: coroutines here & uncomment create 
            val database = MealDatabase.getMealDatabase(this)
            val mealDao = database.getMealDao()
            //mealDao.createMeal(newMeal)
            Snackbar.make(binding.root, "Your recipe for ${newMeal.title} has been added to the ${newMeal.type}s list. Enjoy!", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK") { }
                .show()

            clearTextFields()
        }
    }

    private fun clearTextFields() {
        binding.dropdownType.text.clear()
        binding.editTextTitle.text?.clear()
        binding.editTextDescription.text?.clear()
        binding.editTextRecipe.text?.clear()
        binding.ratingBar.rating = 0F

        //don't know how to change background tint
        binding.imageViewAdd.setBackgroundResource(R.drawable.dinner)
        binding.imageViewAdd.backgroundTintMode = PorterDuff.Mode.SRC_OVER
        binding.imageViewAdd.setImageResource(R.drawable.outline_add_photo_alternate_24)
        binding.imageViewAdd.scaleType = ImageView.ScaleType.FIT_CENTER
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