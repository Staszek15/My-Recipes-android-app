package com.staszek15.myrecipes.meal

import android.app.ActionBar
import android.content.Intent
import android.graphics.BitmapFactory
import android.health.connect.datatypes.MealType
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.staszek15.myrecipes.AddMealActivity
import com.staszek15.myrecipes.Converters
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.ActivityMealListBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealListActivity : AppCompatActivity(), MealListAdapter.RecyclerViewEvent {

    private lateinit var binding: ActivityMealListBinding
    private lateinit var mealList: List<MealItemClass>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var mealType: String = intent.getStringExtra("mealType")!!
        this.title = mealType
        mealType = mealType.dropLast(1)   // delete 's' to match type in database

        setupDatabase(mealType)
        handleClickListeners()
    }


    private fun setupDatabase(mealType: String) {
        val database = MealDatabase.getMealDatabase(this)
        val mealDao = database.getMealDao()
        lifecycleScope.launch(Dispatchers.IO) {
            mealList = if (mealType == "Favourite") {
                mealDao.getFavMeals()
            } else {
                mealDao.getTypeMeals(mealType)
            }
            withContext(Dispatchers.Main) { displayRecyclerView(mealList) } // also displaying recycler view
        }
    }

    private fun displayRecyclerView(list: List<MealItemClass>) {
        val adapter = MealListAdapter(list, this)
        binding.mealsRecyclerView.setHasFixedSize(true)
        binding.mealsRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        binding.mealsRecyclerView.adapter = adapter
    }

    private fun handleClickListeners() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, AddMealActivity::class.java)
            startActivity(intent)
        }
    }

    // override function from interface in adapter file
    override fun myOnItemClick(position: Int) {
        val clickedItem = mealList[position]
        Toast.makeText(
            this,
            "Clicked ${clickedItem.title}, ID: ${clickedItem.mealId}.",
            Toast.LENGTH_SHORT
        ).show()
    }

}