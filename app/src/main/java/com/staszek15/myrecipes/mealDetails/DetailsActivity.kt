package com.staszek15.myrecipes.mealDetails

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
import com.staszek15.myrecipes.mealDB.MealDatabase
import com.staszek15.myrecipes.mealDB.MealItemClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private val clickedMeal: MealItemClass by lazy { intent.getParcelableExtra<MealItemClass>("clicked_meal")!! }
    private val clickedDocument: String? by lazy { intent.getStringExtra("clicked_document") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prepopulateFields()
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

        binding.tvTitle.text = clickedMeal.title
        binding.tvRecipe.text = clickedMeal.recipe

        if (clickedDocument != null) {
            Glide
                .with(this)
                .load(clickedMeal.imageUrl)
                .error(R.drawable.empty_image)
                .into(binding.ivMeal)
        } else {
            val resId = resources.getIdentifier(
                clickedMeal.drawableName,
                "drawable",
                packageName
            )
            if (resId != 0) {
                binding.ivMeal.setImageResource(resId)
            } else {
                binding.ivMeal.setImageResource(R.drawable.dinner)
            }
        }
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
                if (clickedDocument.isNullOrBlank()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val database = MealDatabase.getMealDatabase(this@DetailsActivity)
                        val mealDao = database.getMealDao()
                        mealDao.deleteMeal(clickedMeal)

                        withContext(Dispatchers.Main) {
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                } else {
                    // TODO: delete from firebase
                }

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
                intent.putExtra("clicked_meal", clickedMeal)
                intent.putExtra("clicked_document", clickedDocument)
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}