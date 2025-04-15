package com.staszek15.myrecipes.mealList

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.staszek15.myrecipes.mealDB.MealItemClass
import com.staszek15.myrecipes.mealAdd.AddMealActivity
import com.staszek15.myrecipes.mealDetails.DetailsActivity
import com.staszek15.myrecipes.databinding.ActivityMealListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealListActivity : AppCompatActivity(), MealListAdapter.RecyclerViewEvent {

    private lateinit var binding: ActivityMealListBinding
    private lateinit var mealList: MutableList<Pair<MealItemClass, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var mealType: String = intent.getStringExtra("mealType")!!
        this.title = mealType
        mealType = mealType.dropLast(1)   // delete 's' to match type in database

        setupFirestore(mealType)
        handleClickListeners(mealType)
    }

    private fun setupFirestore(mealType: String) {
        val userId = Firebase.auth.currentUser!!.uid
        Firebase.firestore.collection("Recipes/$mealType/$userId")
            .get()
            .addOnSuccessListener { result ->
                lifecycleScope.launch(Dispatchers.IO) {
                    mealList = result.mapNotNull { document ->
                        Pair<MealItemClass, String>(document.toObject(MealItemClass::class.java), document.id)
                    }.toMutableList()
                    withContext(Dispatchers.Main) { displayRecyclerView(mealList) }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting documents: ", e)
            }
    }


    private fun displayRecyclerView(list: List<Pair<MealItemClass,String>>) {
        val adapter = MealListAdapter(list, this)
        binding.mealsRecyclerView.setHasFixedSize(true)
        binding.mealsRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        binding.mealsRecyclerView.adapter = adapter
    }

    private fun handleClickListeners(mealType: String) {
        // fab invisible in favourites list
        if (mealType == "Favourite") {
            binding.fab.visibility = View.INVISIBLE
        } else {
            binding.fab.setOnClickListener {
                val intent = Intent(this, AddMealActivity::class.java)
                intent.putExtra("mealType", mealType)
                startActivity(intent)
            }
        }
    }

    // override function from interface in adapter file
    override fun myOnItemClick(position: Int) {
        val clickedMeal: MealItemClass = mealList[position].first
        val clickedDocumentId : String = mealList[position].second
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("clicked_meal", clickedMeal)
        intent.putExtra("clicked_document_id", clickedDocumentId)
        startActivity(intent)
    }

}