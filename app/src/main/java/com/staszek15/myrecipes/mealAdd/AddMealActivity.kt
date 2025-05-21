package com.staszek15.myrecipes.mealAdd

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Visibility
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.ActivityAddMealBinding
import com.staszek15.myrecipes.validatorAddMeal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AddMealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMealBinding
    private var ingredientsList = mutableListOf(IngredientClass("", ""))
    private lateinit var storageRef: StorageReference
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mealType: String = intent.getStringExtra("mealType")!!
        storageRef = Firebase.storage.reference

        setupDropdownMenu(mealType)
        handleImageSelection()
        setupIngredientRecyclerView(ingredientsList)
        handleClickListeners(mealType)
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


    private fun handleClickListeners(mealType: String) {
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
            binding.buttonAdd.isEnabled = false
            if (!isInputValid()) {
                binding.buttonAdd.isEnabled = true
                return@setOnClickListener
            }
            uri?.let { imageUri ->
                uploadImage(mealType, imageUri) { imageUrl ->
                    val newMeal = createMapOfMeal(imageUrl)
                    saveMealToFirestore(mealType, newMeal)
                    binding.buttonAdd.isEnabled = true
                }
            } ?: run {
                Snackbar.make(binding.root, "Please select an image.", Snackbar.LENGTH_LONG)
                    .setAction("OK") {}
                    .show()
                binding.buttonAdd.isEnabled = true
            }
        }
    }


    private fun isInputValid(): Boolean {
        return validatorAddMeal(
            binding.editTextTitle,
            binding.editTextDescription,
            binding.editTextRecipe
        )
    }

    private fun uploadImage(mealType: String, imageUri: Uri, onSuccess: (String) -> Unit) {
        val userId = Firebase.auth.currentUser!!.uid
        val timestamp = System.currentTimeMillis()
        val path = "Recipes/$mealType/$userId/$timestamp.jpg"

        storageRef.child(path).putFile(imageUri)
            .addOnSuccessListener { task ->
                task.metadata?.reference?.downloadUrl
                    ?.addOnSuccessListener { url ->
                        onSuccess(url.toString())
                    }
                    ?.addOnFailureListener { handleFailure("Image url download failed", it, "add_recipe_url_failure") }
            }
            .addOnFailureListener { handleFailure("Image upload to Firebase Storage failed", it, "add_recipe_storage_failure") }
    }

    private fun createMapOfMeal(imageUrl: String): HashMap<String, Any> {
        val filteredIngredients = ingredientsList
            .filter { it.amount.isNotEmpty() || it.ingredient.isNotEmpty() }

        return hashMapOf(
            "type" to binding.dropdownType.text.toString(),
            "title" to binding.editTextTitle.text.toString(),
            "description" to binding.editTextDescription.text.toString(),
            "recipe" to binding.editTextRecipe.text.toString(),
            "ingredients" to Json.encodeToString(filteredIngredients),
            "imageUrl" to imageUrl,
            "rating" to binding.ratingBar.rating,
            "favourite" to binding.favSwitch.isChecked
        )
    }

    private fun saveMealToFirestore(mealType: String, meal: HashMap<String, Any>) {
        val userId = Firebase.auth.currentUser!!.uid

        Firebase.firestore.collection("Recipes/$mealType/$userId")
            .add(meal)
            .addOnSuccessListener {
                clearTextFields()
                Snackbar.make(
                    binding.root,
                    "Your recipe for ${meal["title"]} has been added to the $mealType list. Enjoy!",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("OK") {}.show()
            }
            .addOnFailureListener {
                handleFailure("Recipe upload to Firestore failed", it, "add_recipe_firestore_failure")
            }
    }

    private fun handleFailure(logMessage: String, exception: Exception, analyticsEvent: String) {
        Log.e("Add recipe", logMessage, exception)
        Firebase.analytics.logEvent(analyticsEvent, null)
        Snackbar.make(
            binding.root,
            "$logMessage. Exception: $exception",
            Snackbar.LENGTH_LONG
        ).setAction("OK") {}.show()
    }



    private fun clearTextFields() {
        clearIngredients()
        uri = null
        binding.editTextTitle.text?.clear()
        binding.editTextDescription.text?.clear()
        binding.editTextRecipe.text?.clear()
        binding.ratingBar.rating = 0F
        binding.favSwitch.isChecked = false

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
        val pickImage =
            registerForActivityResult(ActivityResultContracts.GetContent()) {
                if (it != null) {
                    uri = it
                    binding.imageViewAdd.setImageURI(uri)
                    //binding.imageViewAdd.scaleType = ImageView.ScaleType.CENTER_CROP   //set scale type
                    binding.iconAddPhoto.visibility = View.GONE
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