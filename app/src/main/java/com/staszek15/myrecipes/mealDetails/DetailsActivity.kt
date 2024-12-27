package com.staszek15.myrecipes.mealDetails

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.ActivityDetailsBinding
import com.staszek15.myrecipes.mealAdd.AddMealActivity
import com.staszek15.myrecipes.mealDB.MealDatabase
import com.staszek15.myrecipes.mealDB.MealItemClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            withContext(Dispatchers.Main) { prepopulateFields(intentMeal) }
        }
    }

    private fun prepopulateFields(intentMeal: MealItemClass) {
        binding.ivMeal.setImageBitmap(intentMeal.image)
        binding.tvTitle.text = intentMeal.title
        binding.tvRecipe.text = intentMeal.recipe
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_toolbar, menu)
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
            .setPositiveButton("Yes") { dialog, which ->
                //val database = MealDatabase.getMealDatabase(this)
                //val mealDao = database.getMealDao()
                //mealDao.deleteMeal(thisMeal)
                // TODO: uncomment deletion 
            }
            .setNegativeButton("No") { dialog, which ->
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
            .setPositiveButton("Yes") { dialog, which ->
                val intent = Intent(this, AddMealActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}