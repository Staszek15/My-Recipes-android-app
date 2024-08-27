package com.staszek15.myrecipes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.staszek15.myrecipes.databinding.ActivityMealListBinding
import com.staszek15.myrecipes.databinding.MealListItemBinding

class MealListActivity : AppCompatActivity(), MealListAdapter.RecyclerViewEvent {

    private lateinit var binding: ActivityMealListBinding
    private var mealsList = createMealList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.title = intent.getStringExtra("header")

        displayRecyclerView()
        handleClickListeners()
    }

    private fun handleClickListeners() {
        binding.fab.setOnClickListener {
            val intent = Intent(this, AddMealActivity::class.java)
            startActivity(intent)
        }
    }

    private fun displayRecyclerView() {
        val adapter = MealListAdapter(mealsList, this)
        binding.mealsRecyclerView.setHasFixedSize(true)
        binding.mealsRecyclerView.layoutManager = LinearLayoutManager(applicationContext)
        binding.mealsRecyclerView.adapter = adapter
    }

    private fun createMealList(): List<MealItemClass> = buildList {
        add(MealItemClass("Development 1", "Absolutely astonishing meal description containing every detail. Elegant and easy to prepare delicious food", R.drawable.meal, 4f))
        add(MealItemClass("Development 2", "Absolutely astonishing meal description containing every detail. Elegant and easy to prepare delicious food", R.drawable.meal, 5f))
        add(MealItemClass("Development 3", "Absolutely astonishing meal description containing every detail. Elegant and easy to prepare delicious food", R.drawable.meal, 2f))
        add(MealItemClass("Development 4", "Absolutely astonishing meal description containing every detail. Elegant and easy to prepare delicious food", R.drawable.meal, 4.5f))
        add(MealItemClass("Development 5", "Absolutely astonishing meal description containing every detail. Elegant and easy to prepare delicious food", R.drawable.meal, 5f))
        add(MealItemClass("Development 6", "Absolutely astonishing meal description containing every detail. Elegant and easy to prepare delicious food", R.drawable.meal, 3f))
        add(MealItemClass("Development 7", "Absolutely astonishing meal description containing every detail. Elegant and easy to prepare delicious food", R.drawable.meal, 5f))
        add(MealItemClass("Development 8", "Absolutely astonishing meal description containing every detail. Elegant and easy to prepare delicious food", R.drawable.meal, 2.5f))
        add(MealItemClass("Development 9", "Absolutely astonishing meal description containing every detail. Elegant and easy to prepare delicious food", R.drawable.meal, 5f))
    }

    override fun myOnItemClick(position: Int) {
        val clickedItem = mealsList[position]
        Toast.makeText(this, "Clicked ${clickedItem.heading}", Toast.LENGTH_SHORT).show()
    }

}