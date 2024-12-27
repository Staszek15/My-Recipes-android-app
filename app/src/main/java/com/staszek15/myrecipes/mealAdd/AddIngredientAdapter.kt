package com.staszek15.myrecipes.mealList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.staszek15.myrecipes.databinding.IngredientAddItemBinding
import com.staszek15.myrecipes.mealAdd.IngredientClass


class AddIngredientAdapter(
    private var ingredientList: MutableList<IngredientClass>):
    RecyclerView.Adapter<AddIngredientAdapter.AddIngredientViewHolder>(){

    inner class AddIngredientViewHolder(binding: IngredientAddItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        val amountField = binding.etAmount
        val ingredientField = binding.etIngredient
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddIngredientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = IngredientAddItemBinding.inflate(inflater, parent, false)
        return AddIngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddIngredientViewHolder, position: Int) {
        holder.amountField.setText(ingredientList[position].amount)
        holder.ingredientField.setText(ingredientList[position].ingredient)

        holder.amountField.addTextChangedListener {
            ingredientList[position].amount = it.toString()
        }
        holder.ingredientField.addTextChangedListener {
            ingredientList[position].ingredient = it.toString()
        }
    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }

}