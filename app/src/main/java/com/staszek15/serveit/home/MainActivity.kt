package com.staszek15.serveit.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.staszek15.serveit.settings.SettingsActivity
import com.staszek15.serveit.mealList.MealListActivity
import com.staszek15.serveit.R
import com.staszek15.serveit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), HomeListAdapter.RecyclerViewEvent {

    private lateinit var binding: ActivityMainBinding
    private var homeList = createHomeList()

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
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
        add(HomeItemClass("Breakfasts", R.drawable.breakfasts))
        add(HomeItemClass("Suppers", R.drawable.suppers))
        add(HomeItemClass("Dinners", R.drawable.dinners))
        add(HomeItemClass("Desserts", R.drawable.desserts))
        add(HomeItemClass("Shakes", R.drawable.shakes))
        add(HomeItemClass("Drinks", R.drawable.drinks))
    }

    private fun setUpClickListeners() {
        binding.ivFavourite.setOnClickListener {
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
