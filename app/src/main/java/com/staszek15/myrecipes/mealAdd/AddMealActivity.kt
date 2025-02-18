package com.staszek15.myrecipes.mealAdd

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.ActivityAddMealBinding
import com.staszek15.myrecipes.mealDB.MealDatabase
import com.staszek15.myrecipes.mealDB.MealItemClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AddMealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMealBinding
    private var ingredientsList = mutableListOf(IngredientClass("", ""))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mealType: String = intent.getStringExtra("mealType")!!

        setupDropdownMenu(mealType)
        handleImageSelection()
        setupIngredientRecyclerView(ingredientsList)
        handleClickListeners()
        overrideBackNavigation()


    }


    private fun overrideBackNavigation() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showConfirmExitDialog()
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)
    }


    private fun setupIngredientRecyclerView(ingredientsList: MutableList<IngredientClass>) {
        val adapterIngredients = AddIngredientAdapter(ingredientsList)
        binding.rvIngredients.adapter = adapterIngredients
        binding.rvIngredients.layoutManager = LinearLayoutManager(applicationContext)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun handleClickListeners() {

        // button add ingredient
        binding.ButtonAddIngredient.setOnClickListener {
            ingredientsList.add(IngredientClass("", ""))
            binding.rvIngredients.adapter?.notifyItemInserted(ingredientsList.size - 1)
        }
        // button clear ingredients
        binding.ButtonDeleteIngredient.setOnClickListener {
            showClearIngredientsDialog()
        }

        // button add meal
        binding.buttonAdd.setOnClickListener {

            ingredientsList =
                ingredientsList.filter { it.amount.isNotEmpty() || it.ingredient.isNotEmpty() }
                    .toMutableList()

            //Log.i("ingredient tag", ingredientsList.toString())
            //Log.i("ingredient tag", Json.encodeToString(ingredientsList))

            val newMeal = MealItemClass(
                type = binding.dropdownType.text.toString(),
                title = binding.editTextTitle.text.toString(),
                description = binding.editTextDescription.text.toString(),
                recipe = binding.editTextRecipe.text.toString(),
                ingredients = Json.encodeToString(ingredientsList),
                image = binding.imageViewAdd.drawToBitmap(),
                rating = binding.ratingBar.rating,
                favourite = false
            )
            // TODO: coroutines here & uncomment create
            val database = MealDatabase.getMealDatabase(this)
            val mealDao = database.getMealDao()

            lifecycleScope.launch(Dispatchers.IO) {
                //mealDao.createMeal(newMeal)
            }
            Snackbar.make(
                binding.root,
                "Your recipe for ${newMeal.title} has been added to the ${newMeal.type}s list. Enjoy!",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("OK") { }
                .show()

            clearTextFields()
        }
    }

    private fun clearTextFields() {
        clearIngredients()
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

    private fun setupDropdownMenu(mealType: String) {
        val items = listOf("Dinner", "Breakfast", "Dessert", "Shake", "Alcohol", "Decoration")
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, items)
        binding.dropdownType.setAdapter(adapter)
        binding.dropdownType.setText(mealType, false)
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
            val intentSelectImage =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
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


    private fun showClearIngredientsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Warning!")
            .setMessage("Do you want to delete all of the entered ingredients?")
            .setPositiveButton("Delete") { _, _ ->
                clearIngredients()
            }
            .setNegativeButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showConfirmExitDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Warning!")
            .setMessage("Are you sure that you want to exit? All your progress will be lost.")
            .setPositiveButton("Exit") { _, _ ->
                finish()
            }
            .setNegativeButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun clearIngredients() {
        ingredientsList.clear()
        ingredientsList.add(IngredientClass("", ""))
        binding.rvIngredients.adapter?.notifyDataSetChanged()
    }
}