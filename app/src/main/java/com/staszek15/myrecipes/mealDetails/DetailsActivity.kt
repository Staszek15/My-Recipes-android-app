package com.staszek15.myrecipes.mealDetails

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.ActivityDetailsBinding
import com.staszek15.myrecipes.mealAdd.IngredientClass
import com.staszek15.myrecipes.mealDB.MealDatabase
import com.staszek15.myrecipes.mealDB.MealItemClass
import com.staszek15.myrecipes.mealEdit.EditMealActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var clickedMeal: MealItemClass
    private var clickedDocument: String? = null
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private var DEFAULT_MEAL_FLAG: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clickedMeal = intent.getParcelableExtra<MealItemClass>("clicked_meal")!!
        clickedDocument = intent.getStringExtra("clicked_document")
        DEFAULT_MEAL_FLAG = clickedDocument.isNullOrBlank()

        // Register the launcher
        resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                clickedMeal = result.data?.getParcelableExtra<MealItemClass>("clicked_meal")!!
                clickedDocument = result.data?.getStringExtra("clicked_document")
                prepopulateFields()  
            }
        }
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
                binding.ivMeal.setImageResource(R.drawable.empty_image)
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
                if (DEFAULT_MEAL_FLAG) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val database = MealDatabase.getMealDatabase(this@DetailsActivity)
                        val mealDao = database.getMealDao()
                        mealDao.deleteMeal(clickedMeal)

                        withContext(Dispatchers.Main) {
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                } else {
                    val userId = Firebase.auth.currentUser!!.uid
                    val mealType = clickedMeal.type
                    Firebase.firestore
                        .collection("Recipes/$userId/$mealType")
                        .document(clickedDocument!!)
                        .delete()
                }
                finish()
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
            .setPositiveButton("Yes") { dialog, _ ->
                if (DEFAULT_MEAL_FLAG) {
                    dialog.dismiss()
                    Snackbar.make(binding.root, "Permission denied. You can edit only your own recipes.", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK") {}
                        .show()
                } else {
                    val intent = Intent(this, EditMealActivity::class.java)
                    intent.putExtra("clicked_meal", clickedMeal)
                    intent.putExtra("clicked_document", clickedDocument)
                    resultLauncher.launch(intent)
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

}