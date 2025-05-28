package com.staszek15.myrecipes.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.staszek15.myrecipes.settings.SettingsActivity
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
        overrideBackNavigation()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_action_bar_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.icon_account -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayRecyclerView() {
        //binding.homeRecyclerView.setHasFixedSize(true)
        binding.homeRecyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val homeAdapter = HomeListAdapter(homeList, this)
        binding.homeRecyclerView.adapter = homeAdapter
    }

    override fun myOnItemClick(position: Int) {
        val clickedItem = homeList[position]
        val intent = Intent(this, MealListActivity::class.java)
        intent.putExtra("mealType", clickedItem.text)
        startActivity(intent)
    }

    private fun createHomeList(): List<HomeItemClass> = buildList {
        add(HomeItemClass("Dinners", R.drawable.new_dinner))
        add(HomeItemClass("Breakfasts", R.drawable.new_breakfast))
        add(HomeItemClass("Desserts", R.drawable.new_breakfast))
        add(HomeItemClass("Shakes", R.drawable.new_dinner))
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

    // empty system back arrow navigation
    private fun overrideBackNavigation() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

}
