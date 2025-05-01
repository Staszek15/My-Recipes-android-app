package com.staszek15.myrecipes.mealEdit

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.ActivityAddMealBinding
import com.staszek15.myrecipes.mealAdd.AddIngredientAdapter
import com.staszek15.myrecipes.mealAdd.IngredientClass
import com.staszek15.myrecipes.mealDB.MealItemClass
import kotlinx.serialization.json.Json

class EditMealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMealBinding
    private val clickedMeal: MealItemClass by lazy { intent.getParcelableExtra<MealItemClass>("clicked_meal")!! }
    private val clickedDocument: String? by lazy { intent.getStringExtra("clicked_document") }
    private lateinit var ingredientsList: MutableList<IngredientClass>
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prepopulateFields()
        handleClickListeners()
        handleImageSelection()
        overrideBackNavigation()
    }

    // system back arrow navigation
    private fun overrideBackNavigation() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showConfirmExitDialog()
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    // back navigation for toolbar back arrow
    override fun onSupportNavigateUp(): Boolean {
        showConfirmExitDialog()
        return true
    }

    private fun prepopulateFields() {
        if (clickedDocument != null) {
            Glide
                .with(this)
                .load(clickedMeal.imageUrl)
                .error(R.drawable.empty_image)
                .into(binding.imageViewAdd)
        } else {
            val resId = resources.getIdentifier(
                clickedMeal.drawableName,
                "drawable",
                packageName
            )
            if (resId != 0) {
                binding.imageViewAdd.setImageResource(resId)
            } else {
                binding.imageViewAdd.setImageResource(R.drawable.empty_image)
            }
        }
        binding.imageViewAdd.scaleType = ImageView.ScaleType.CENTER_CROP

        // meal type dropdown menu
        val items = listOf("Dinner", "Breakfast", "Dessert", "Shake", "Alcohol", "Decoration")
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, items)
        binding.dropdownType.setAdapter(adapter)

        // ingredients list
        ingredientsList = Json.decodeFromString<MutableList<IngredientClass>>(clickedMeal.ingredients.toString())
        binding.rvIngredients.adapter = AddIngredientAdapter(ingredientsList)
        binding.rvIngredients.layoutManager = LinearLayoutManager(applicationContext)

        binding.dropdownType.setText(clickedMeal.type)
        binding.editTextTitle.setText(clickedMeal.title)
        binding.editTextDescription.setText(clickedMeal.description)
        binding.editTextRecipe.setText(clickedMeal.recipe)
        binding.ratingBar.rating = clickedMeal.rating
        binding.favSwitch.isChecked = clickedMeal.favourite
    }


    private fun handleClickListeners() {
        binding.ButtonAddIngredient.setOnClickListener {
            ingredientsList.add(IngredientClass("", ""))
            binding.rvIngredients.adapter?.notifyItemInserted(ingredientsList.size - 1)
        }
        // button clear ingredients
        binding.ButtonDeleteIngredient.setOnClickListener {
            showClearIngredientsDialog()
        }
        binding.buttonAdd.setOnClickListener {  }
    }

    private fun handleImageSelection() {
        val pickImage =
            registerForActivityResult(ActivityResultContracts.GetContent()) {
                if (it != null) {
                    uri = it
                    binding.imageViewAdd.setImageURI(uri)
                    binding.imageViewAdd.scaleType =
                        ImageView.ScaleType.CENTER_CROP   //set scale type
                }
            }
        binding.cardViewImageAdd.setOnClickListener {
            pickImage.launch("image/*")
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
        binding.rvIngredients.clearFocus()
        ingredientsList.clear()
        ingredientsList.add(IngredientClass("", ""))
        binding.rvIngredients.adapter?.notifyDataSetChanged()
    }


}