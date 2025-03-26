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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intentItemId = intent.getIntExtra("clicked_item_id", -1)  // non null

        val database = MealDatabase.getMealDatabase(this)
        val mealDao = database.getMealDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val intentMeal = mealDao.getExactMeal(intentItemId)

            withContext(Dispatchers.Main) {
                prepopulateFields(intentMeal)
            }
        }
    }

    private fun setupIngredientRecyclerView(ingredientsList: MutableList<IngredientClass>) {
        val adapterIngredients = DetailsIngredientAdapter(ingredientsList)
        binding.rvIngredients.adapter = adapterIngredients
        binding.rvIngredients.layoutManager = LinearLayoutManager(applicationContext)
    }

    private fun prepopulateFields(intentMeal: MealItemClass) {
        //val ing = mutableListOf(IngredientClass(amount="6", ingredient="apples"), IngredientClass(amount="3", ingredient="eggs"), IngredientClass(amount="2", ingredient="carrots"), IngredientClass(amount="100ml", ingredient="milk"), IngredientClass(amount="1 table spoon", ingredient="water"), IngredientClass(amount="12", ingredient="strawberries"), IngredientClass(amount="80g", ingredient="flour"), IngredientClass(amount="50g", ingredient="cheese"))
        //val ingg = Json.encodeToString(ing)
        //println(ingg)
        val ing =
            "[{\"amount\":\"6\",\"ingredient\":\"apples\"},{\"amount\":\"3\",\"ingredient\":\"eggs\"},{\"amount\":\"2\",\"ingredient\":\"carrots\"},{\"amount\":\"100ml\",\"ingredient\":\"milk\"},{\"amount\":\"1 table spoon\",\"ingredient\":\"water\"},{\"amount\":\"12\",\"ingredient\":\"strawberries\"},{\"amount\":\"80g\",\"ingredient\":\"flour\"},{\"amount\":\"50g\",\"ingredient\":\"cheese\"}]"
        val ingredientsList =
            Json.decodeFromString<MutableList<IngredientClass>>(ing)
        setupIngredientRecyclerView(ingredientsList)

        binding.ivMeal.setImageBitmap(intentMeal.image)
        binding.tvTitle.text = intentMeal.title
        binding.tvRecipe.text = intentMeal.recipe
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
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}