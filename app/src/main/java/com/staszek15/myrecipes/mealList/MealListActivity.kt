package com.staszek15.myrecipes.mealList

import android.app.AlertDialog
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
import com.staszek15.myrecipes.loadingDialog
import com.staszek15.myrecipes.mealDB.MealDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MealListActivity : AppCompatActivity(), MealListAdapter.RecyclerViewEvent {

    private lateinit var binding: ActivityMealListBinding
    private var mealList: MutableList<Pair<MealItemClass, String?>> = mutableListOf()

    // delete 's' to match type in database
    private val mealType by lazy { intent.getStringExtra("mealType")!!.dropLast(1) }

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
        var loadingDialog: AlertDialog? = null
        val dialogJob = lifecycleScope.launch {
            delay(500)
            loadingDialog = loadingDialog(this@MealListActivity)
        }

        val userId = Firebase.auth.currentUser!!.uid

        if (mealType != "Favourite") {
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
                        mealList
                            .sortWith(compareByDescending<Pair<MealItemClass, String?>> { it.first.favourite }
                                .thenBy { it.first.title })

                        withContext(Dispatchers.Main) {
                            dialogJob.cancel()
                            loadingDialog?.dismiss()
                            displayRecyclerView(mealList)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    loadingDialog?.dismiss()
                    Log.e("Firestore", "Error getting documents: ", e)
                }
        } else {
            val types = listOf("Dinner", "Breakfast", "Dessert", "Shake", "Alcohol", "Decoration")
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    for (i in types) {
                        try {
                            val result = Firebase.firestore.collection("Recipes/$i/$userId")
                                .whereEqualTo("favourite", true)
                                .get()
                                .await()
                            val tempMealList = result.mapNotNull { document ->
                                Pair<MealItemClass, String?>(
                                    document.toObject(MealItemClass::class.java),
                                    document.id
                                )
                            }.toMutableList()
                            mealList.addAll(tempMealList)
                        } catch (e: Exception) {
                            loadingDialog?.dismiss()
                            Log.e("Firestore", "Error getting favourite documents: ", e)
                        }
                    }
                    mealList.sortBy { it.first.title }
                }

                dialogJob.cancel()
                loadingDialog?.dismiss()
                displayRecyclerView(mealList)
            }
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