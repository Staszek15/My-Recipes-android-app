package com.staszek15.serveit.mealDetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.staszek15.serveit.databinding.IngredientDisplayItemBinding
import com.staszek15.serveit.mealAdd.IngredientClass

class DetailsIngredientAdapter(
    private var ingredientList: MutableList<IngredientClass>
) : RecyclerView.Adapter<DetailsIngredientAdapter.DetailsIngredientViewHolder>() {


    inner class DetailsIngredientViewHolder(binding: IngredientDisplayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val ingredientField = binding.tvIngredient
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailsIngredientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = IngredientDisplayItemBinding.inflate(inflater, parent, false)
        return DetailsIngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailsIngredientViewHolder, position: Int) {
        holder.ingredientField.text = ingredientList[position].amount + " " + ingredientList[position].ingredient
    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }

}