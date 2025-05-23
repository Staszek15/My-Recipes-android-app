package com.staszek15.myrecipes.mealList

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.mealDB.MealItemClass
import com.staszek15.myrecipes.mealAdd.AddMealActivity
import com.staszek15.myrecipes.mealDetails.DetailsActivity
import com.staszek15.myrecipes.databinding.ActivityMealListBinding
import com.staszek15.myrecipes.mealDB.MealDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealListActivity : AppCompatActivity(), MealListAdapter.RecyclerViewEvent {

    private lateinit var binding: ActivityMealListBinding
    private lateinit var mealList: MutableList<Pair<MealItemClass, String?>>
    // delete 's' to match type in database
    private val mealType by lazy {intent.getStringExtra("mealType")!!.dropLast(1)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.title = mealType + "s"
    }

    override fun onResume() {
        super.onResume()
        setupFirestore()
        handleClickListeners()
    }

    private fun setupFirestore() {
        val userId = Firebase.auth.currentUser!!.uid
        Firebase.firestore.collection("Recipes/$mealType/$userId")
            .get()
            .addOnSuccessListener { result ->
                val database = MealDatabase.getMealDatabase(this)
                val mealDao = database.getMealDao()

                lifecycleScope.launch(Dispatchers.IO) {
                    val defaultMeals = mealDao.getTypeMeals(mealType)
                        .map { meal -> Pair(meal, null as String?) }.toMutableList()
                    mealList = result.mapNotNull { document ->
                        Pair<MealItemClass, String?>(
                            document.toObject(MealItemClass::class.java),
                            document.id
                        )
                    }.toMutableList()
                    mealList.addAll(defaultMeals)

                    withContext(Dispatchers.Main) { displayRecyclerView(mealList) }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error getting documents: ", e)
            }
    }


    private fun displayRecyclerView(list: List<Pair<MealItemClass, String?>>) {
        if (list.isEmpty()) {
            binding.emptyListLayout.visibility = View.VISIBLE
        } else {
            binding.emptyListLayout.visibility = View.GONE
            val adapter = MealListAdapter(list, this)
            binding.mealsRecyclerView.setHasFixedSize(true)
            binding.mealsRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
            binding.mealsRecyclerView.adapter = adapter
            val divider = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
            AppCompatResources.getDrawable(this, R.drawable.divider_custom)?.let {
                divider.setDrawable(it)
            }
            binding.mealsRecyclerView.addItemDecoration(divider)
        }
    }

    private fun handleClickListeners() {
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
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("clicked_meal", mealList[position].first)
        intent.putExtra("clicked_document", mealList[position].second as String?)
        startActivity(intent)
    }

}