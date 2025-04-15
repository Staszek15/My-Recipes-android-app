package com.staszek15.myrecipes.mealDetails

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.ActivityDetailsBinding
import com.staszek15.myrecipes.mealAdd.AddMealActivity
import com.staszek15.myrecipes.mealAdd.IngredientClass
import com.staszek15.myrecipes.mealDB.MealItemClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var clickedDocumentId: String
    private lateinit var clickedMeal: MealItemClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch(Dispatchers.IO) {
            clickedDocumentId = intent.getStringExtra("clicked_document_id")!!  // non null
            clickedMeal = intent.getParcelableExtra<MealItemClass>("clicked_meal")!!
            withContext(Dispatchers.Main) {
                prepopulateFields()
            }
        }
    }

    private fun setupIngredientRecyclerView(ingredientsList: MutableList<IngredientClass>) {
        val adapterIngredients = DetailsIngredientAdapter(ingredientsList)
        binding.rvIngredients.adapter = adapterIngredients
        binding.rvIngredients.layoutManager = LinearLayoutManager(applicationContext)
    }

    private fun prepopulateFields() {
        val ingredientsList =
            Json.decodeFromString<MutableList<IngredientClass>>(clickedMeal.ingredients.toString())
        setupIngredientRecyclerView(ingredientsList)

        Glide.with(this).load(clickedMeal.imageUrl).into(binding.ivMeal)
        binding.tvTitle.text = clickedMeal.title
        binding.tvRecipe.text = clickedMeal.recipe
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_action_bar_meal_details, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection.
        return when (item.itemId) {
            R.id.icon_edit -> {
                showEditDialog()
                true
            }

            R.id.icon_delete -> {
                showDeleteDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Warning!")
            .setMessage("Do you want to delete this record?")
            .setPositiveButton("Yes") { _, _ ->
                //val database = MealDatabase.getMealDatabase(this)
                //val mealDao = database.getMealDao()
                //mealDao.deleteMeal(thisMeal)
                // TODO: uncomment deletion 
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    private fun showEditDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setTitle("Warning!")
            .setMessage("Do you want to edit this record?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, AddMealActivity::class.java)
                intent.putExtra(clickedDocumentId, clickedDocumentId)
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}