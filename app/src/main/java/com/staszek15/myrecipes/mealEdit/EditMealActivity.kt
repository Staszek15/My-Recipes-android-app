package com.staszek15.myrecipes.mealEdit

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.ActivityAddMealBinding
import com.staszek15.myrecipes.loadingDialog
import com.staszek15.myrecipes.mealAdd.AddIngredientAdapter
import com.staszek15.myrecipes.mealAdd.IngredientClass
import com.staszek15.myrecipes.mealDB.MealItemClass
import com.staszek15.myrecipes.validatorAddMeal
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EditMealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMealBinding
    private val clickedMeal: MealItemClass by lazy { intent.getParcelableExtra<MealItemClass>("clicked_meal")!! }
    private val clickedDocument: String by lazy { intent.getStringExtra("clicked_document")!! }
    private lateinit var ingredientsList: MutableList<IngredientClass>
    private lateinit var storageRef: StorageReference
    private var uri: Uri? = null
    private var NEW_IMAGE_FLAG = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageRef = Firebase.storage.reference
        binding.buttonAdd.text = "Update recipe"
        binding.iconAddPhoto.visibility = View.GONE

        prepopulateFields()
        handleClickListeners(clickedMeal.type)
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
        Glide
            .with(this)
            .load(clickedMeal.imageUrl)
            .centerCrop()
            .error(R.drawable.empty_image)
            .into(binding.imageViewAdd)
        //binding.imageViewAdd.scaleType = ImageView.ScaleType.CENTER_CROP

        // meal type dropdown menu
        val items = listOf("Dinner", "Breakfast", "Dessert", "Shake", "Alcohol", "Decoration")
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, items)
        binding.dropdownType.setAdapter(adapter)

        // ingredients list
        ingredientsList =
            Json.decodeFromString<MutableList<IngredientClass>>(clickedMeal.ingredients.toString())
        binding.rvIngredients.adapter = AddIngredientAdapter(ingredientsList)
        binding.rvIngredients.layoutManager = LinearLayoutManager(applicationContext)

        binding.dropdownType.setText(clickedMeal.type)
        binding.editTextTitle.setText(clickedMeal.title)
        binding.editTextDescription.setText(clickedMeal.description)
        binding.editTextRecipe.setText(clickedMeal.recipe)
        binding.ratingBar.rating = clickedMeal.rating
        binding.switchFav.isChecked = clickedMeal.favourite
    }


    private fun handleClickListeners(mealType: String) {
        binding.ButtonAddIngredient.setOnClickListener {
            ingredientsList.add(IngredientClass("", ""))
            binding.rvIngredients.adapter?.notifyItemInserted(ingredientsList.size - 1)
        }
        // button clear ingredients
        binding.ButtonDeleteIngredient.setOnClickListener {
            showClearIngredientsDialog()
        }
        binding.buttonAdd.setOnClickListener {
            binding.buttonAdd.isEnabled = false

            if (!isInputValid()) {
                binding.buttonAdd.isEnabled = true
                return@setOnClickListener
            }
            if (ingredientsList.all { it.ingredient.isNullOrBlank() }) {
                binding.buttonAdd.isEnabled = true
                Snackbar.make(binding.root, "Please enter ingredients.", Snackbar.LENGTH_LONG)
                    .setAction("OK") {}
                    .show()
                return@setOnClickListener
            }

            if (NEW_IMAGE_FLAG) {
                uri?.let { newImageUri ->
                    clickedMeal.imageUrl?.let { oldImageUrl ->
                        deleteOldImage(oldImageUrl) {
                            uploadImage(mealType, newImageUri) { newImageUrl ->
                                val updatedMeal = createMapOfMeal(newImageUrl)
                                updateMealInFirestore(mealType, updatedMeal)
                                binding.buttonAdd.isEnabled = true
                            }
                        }
                    }
                }
            } else {
                val updatedMeal = createMapOfMeal(clickedMeal.imageUrl!!)
                updateMealInFirestore(mealType, updatedMeal)
                binding.buttonAdd.isEnabled = true
            }
        }
    }

    private fun updateMealInFirestore(mealType: String, updatedMeal: HashMap<String, Any>) {
        val userId = Firebase.auth.currentUser!!.uid
        Firebase.firestore.collection("Recipes/$userId/$mealType").document(clickedDocument)
            .set(updatedMeal)
            .addOnSuccessListener {
                val resultIntent = Intent()
                val resultMeal = createMeal(updatedMeal["imageUrl"] as String)
                resultIntent.putExtra("clicked_meal", resultMeal)
                resultIntent.putExtra("clicked_document", clickedDocument)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            .addOnFailureListener {
                handleFailure(
                    "Recipe update failed",
                    it,
                    "update_recipe_firestore_failure"
                )
            }
    }

    private fun uploadImage(mealType: String, imageUri: Uri, onSuccess: (String) -> Unit) {
        val userId = Firebase.auth.currentUser!!.uid
        val timestamp = System.currentTimeMillis()
        val path = "Recipes/$userId/$mealType/$timestamp.jpg"

        storageRef.child(path).putFile(imageUri)
            .addOnSuccessListener { task ->
                task.metadata?.reference?.downloadUrl
                    ?.addOnSuccessListener { url ->
                        onSuccess(url.toString())
                    }
                    ?.addOnFailureListener {
                        handleFailure(
                            "Image url download failed",
                            it,
                            "add_recipe_url_failure"
                        )
                    }
            }
            .addOnFailureListener {
                handleFailure(
                    "Image upload to Firebase Storage failed",
                    it,
                    "add_recipe_storage_failure"
                )
            }
    }


    private fun deleteOldImage(oldImageUrl: String, onSuccess: () -> Unit) {
        val photoRef = Firebase.storage.getReferenceFromUrl(oldImageUrl)
        photoRef.delete()
            .addOnSuccessListener {
                Log.d("DeleteImage", "Old image deleted successfully.")
                onSuccess()
            }
            .addOnFailureListener {
                handleFailure(
                    "Failed to delete old image.",
                    it,
                    "delete_old_image_failure"
                )
            }
    }


    private fun isInputValid(): Boolean {
        return validatorAddMeal(
            binding.editTextTitle,
            binding.editTextDescription,
            binding.editTextRecipe
        )
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

    private fun handleImageSelection() {
        val pickImage =
            registerForActivityResult(ActivityResultContracts.GetContent()) {
                if (it != null) {
                    uri = it
                    binding.imageViewAdd.setImageURI(uri)
                    binding.imageViewAdd.scaleType =
                        ImageView.ScaleType.CENTER_CROP   //set scale type
                    NEW_IMAGE_FLAG = true //set flag for Cloud Storage upload
                }
            }
        binding.cardViewImageAdd.setOnClickListener {
            pickImage.launch("image/*")
        }
    }

    private fun createMapOfMeal(imageUrl: String): HashMap<String, Any> {
        val filteredIngredients = ingredientsList
            .filter { it.ingredient.isNotEmpty() }

        return hashMapOf(
            "type" to binding.dropdownType.text.toString(),
            "title" to binding.editTextTitle.text.toString(),
            "description" to binding.editTextDescription.text.toString(),
            "recipe" to binding.editTextRecipe.text.toString(),
            "ingredients" to Json.encodeToString(filteredIngredients),
            "imageUrl" to imageUrl,
            "rating" to binding.ratingBar.rating,
            "favourite" to binding.switchFav.isChecked
        )
    }

    private fun createMeal(imageUrl: String) :MealItemClass {
        val filteredIngredients = ingredientsList
            .filter { it.amount.isNotEmpty() || it.ingredient.isNotEmpty() }
        return MealItemClass(
            type = binding.dropdownType.text.toString(),
            title = binding.editTextTitle.text.toString(),
            description = binding.editTextDescription.text.toString(),
            recipe = binding.editTextRecipe.text.toString(),
            ingredients = Json.encodeToString(filteredIngredients),
            imageUrl = imageUrl,
            rating = binding.ratingBar.rating,
            favourite = binding.switchFav.isChecked
        )
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