package com.staszek15.serveit.mealAdd

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.staszek15.serveit.R
import com.staszek15.serveit.databinding.ActivityAddMealBinding
import com.staszek15.serveit.loadingDialog
import com.staszek15.serveit.validatorAddMeal
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AddMealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMealBinding
    private var ingredientsList = mutableListOf(IngredientClass("", ""))
    private lateinit var storageRef: StorageReference
    private var uri: Uri? = null
    private var isFavourite = false
    private var favourite_variant: String = "switch"
    private var favourite_interactions: Int = 0
    private var ingredients_clear_all_interactions: Int = 0
    private var ingredientXButton: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mealType: String = intent.getStringExtra("mealType")!!
        storageRef = Firebase.storage.reference

        binding.editTextDescription.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.editTextDescription.setRawInputType(InputType.TYPE_CLASS_TEXT)

        fetchRemoteConfig()
        setupDropdownMenu(mealType)
        handleImageSelection()
        handleClickListeners()
        overrideBackNavigation()
    }

    private fun fetchRemoteConfig() {
        Firebase.remoteConfig.setDefaultsAsync(
            mapOf(
                "favourites_ui_heart" to false,
                "ingredient_x_button" to false
            )
        )
        Firebase.remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                setupRemoteConfigUI()
                setupIngredientRecyclerView(ingredientsList)
            }
    }

    private fun setupRemoteConfigUI() {
        val favouritesUiHeart = Firebase.remoteConfig.getBoolean("favourites_ui_heart")
        if (favouritesUiHeart) {
            binding.switchFav.visibility = View.GONE
            binding.btnFav.visibility = View.VISIBLE
            favourite_variant = "heart"
            binding.btnFav.setOnClickListener {
                favourite_interactions += 1
                isFavourite = !isFavourite
                updateHeartUI(isFavourite)
            }
        } else {
            binding.btnFav.visibility = View.GONE
            binding.switchFav.visibility = View.VISIBLE
            favourite_variant = "switch"
            binding.switchFav.setOnCheckedChangeListener { _, isChecked ->
                favourite_interactions += 1
                isFavourite = isChecked

            }
        }
    }

    private fun updateHeartUI(isFavourite: Boolean) {
        val icon =
            if (isFavourite) R.drawable.baseline_favorite_48 else R.drawable.outline_favourite_48
        binding.btnFav.setImageResource(icon)
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
        ingredientXButton = Firebase.remoteConfig.getBoolean("ingredient_x_button")
        val adapterIngredients = AddIngredientAdapter(ingredientsList, ingredientXButton)
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
            binding.buttonAdd.isEnabled = false
            val loadingDialog = loadingDialog(this)

            if (!isInputValid()) {
                binding.buttonAdd.isEnabled = true
                loadingDialog?.dismiss()
                return@setOnClickListener
            }
            if (ingredientsList.all { it.ingredient.isNullOrBlank() }) {
                binding.buttonAdd.isEnabled = true
                loadingDialog?.dismiss()
                Snackbar.make(binding.root, "Please enter ingredients.", Snackbar.LENGTH_LONG)
                    .setAction("OK") {}
                    .show()
                return@setOnClickListener
            }

            uri?.let { imageUri ->
                val mealType = binding.dropdownType.text.toString()
                uploadImage(mealType, imageUri) { imageUrl ->
                    val newMeal = createMapOfMeal(imageUrl)
                    saveMealToFirestore(mealType, newMeal)

                    val ingredients_valid_count =
                        ingredientsList.count { !it.ingredient.isNullOrBlank() }
                    val ingredients_invalid_count =
                        ingredientsList.count { it.ingredient.isNullOrBlank() }
                    val bundle = Bundle().apply {
                        putString("user_id", Firebase.auth.currentUser?.uid)
                        putString("favourite_variant", favourite_variant)
                        putBoolean("is_favourite", isFavourite)
                        putInt("favourite_interactions", favourite_interactions)

                        putBoolean("ingredientXButton", ingredientXButton)
                        putInt("ingredients_valid_count", ingredients_valid_count)
                        putInt("ingredients_invalid_count", ingredients_invalid_count)
                        putInt("ingredients_clear_all_interactions", ingredients_clear_all_interactions)
                    }
                    Firebase.analytics.logEvent("recipe_added", bundle)
                    if (isFavourite) {
                        Firebase.analytics.logEvent("recipe_added_fav", bundle)
                    }
                    if (ingredients_invalid_count > 0) {
                        Firebase.analytics.logEvent("ingredients_invalid_min_1", null)
                    }
                    loadingDialog?.dismiss()
                    binding.buttonAdd.isEnabled = true
                }
            } ?: run {
                loadingDialog?.dismiss()
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
            "favourite" to isFavourite
        )
    }

    private fun saveMealToFirestore(mealType: String, meal: HashMap<String, Any>) {
        val userId = Firebase.auth.currentUser!!.uid

        Firebase.firestore.collection("Recipes/$userId/$mealType")
            .add(meal)
            .addOnSuccessListener {
                resetForm()
                Snackbar.make(
                    binding.root,
                    "Your recipe for ${meal["title"]} has been added to the $mealType list. Enjoy!",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("OK") {}.show()
            }
            .addOnFailureListener {
                handleFailure(
                    "Recipe upload to Firestore failed",
                    it,
                    "add_recipe_firestore_failure"
                )
            }
    }

    private fun handleFailure(logMessage: String, exception: Exception, analyticsEvent: String) {
        Firebase.analytics.logEvent(analyticsEvent, null)
        Snackbar.make(
            binding.root,
            "$logMessage. Exception: $exception",
            Snackbar.LENGTH_LONG
        ).setAction("OK") {}.show()
    }


    private fun resetForm() {
        clearIngredients()
        binding.editTextTitle.text?.clear()
        binding.editTextDescription.text?.clear()
        binding.editTextRecipe.text?.clear()
        binding.ratingBar.rating = 0F
        isFavourite = false
        binding.switchFav.isChecked = false
        binding.btnFav.setImageResource(R.drawable.outline_favourite_48)
        binding.imageViewAdd.setImageDrawable(null)
        binding.iconAddPhoto.visibility = View.VISIBLE
        uri = null
        ingredients_clear_all_interactions = 0
        favourite_interactions = 0
    }

    private fun setupDropdownMenu(mealType: String) {
        val items = listOf("Breakfast", "Supper", "Dinner", "Dessert", "Shake", "Drink")
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
                Firebase.analytics.logEvent("ingredients_clear", null)
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
        ingredients_clear_all_interactions += 1
        binding.rvIngredients.clearFocus()
        ingredientsList.clear()
        ingredientsList.add(IngredientClass("", ""))
        binding.rvIngredients.adapter?.notifyDataSetChanged()
    }
}