package com.staszek15.myrecipes.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.staszek15.myrecipes.mealList.MealListActivity
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), HomeListAdapter.RecyclerViewEvent {

    private lateinit var binding: ActivityMainBinding
    private var homeList = createHomeList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayRecyclerView()
        setUpClickListeners()
    }

    private fun displayRecyclerView() {
        //binding.homeRecyclerView.setHasFixedSize(true)
        binding.homeRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val homeAdapter = HomeListAdapter(homeList, this)
        binding.homeRecyclerView.adapter = homeAdapter
    }

    override fun myOnItemClick(position: Int) {
        val clickedItem = homeList[position]
        val intent = Intent(this, MealListActivity::class.java)
        intent.putExtra("mealType", clickedItem.text)
        startActivity(intent)
    }

    private fun createHomeList() : List<HomeItemClass> = buildList {
        add(HomeItemClass("Dinners", R.drawable.dinner))
        add(HomeItemClass("Breakfasts", R.drawable.breakfast))
        add(HomeItemClass("Desserts", R.drawable.dessert))
        add(HomeItemClass("Shakes", R.drawable.milkshake))
        add(HomeItemClass("Alcohols", R.drawable.alcohol))
        add(HomeItemClass("Decorations", R.drawable.food_decoration))
    }

    private fun setUpClickListeners() {
        binding.homeFavouriteCardView.setOnClickListener {
            val intent = Intent(this, MealListActivity::class.java)
            intent.putExtra("mealType", "Favourites")
            startActivity(intent)
        }
    }

}
