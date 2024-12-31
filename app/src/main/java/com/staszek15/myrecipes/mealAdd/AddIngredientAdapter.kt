package com.staszek15.myrecipes.mealList

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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

        // remove text listeners if they exist to avoid OutOfBound Errors due to clearing recyclerView
        // when adding new recipe
        holder.amountField.removeTextChangedListener(holder.amountField.tag as TextWatcher?)
        holder.ingredientField.removeTextChangedListener(holder.ingredientField.tag as TextWatcher?)

        // create text watchers to update mutable list of ingredients after any change in edit text fields
        val amountTextWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                ingredientList[position].amount = p0.toString()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        }

        val ingredientTextWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                ingredientList[position].ingredient = p0.toString()
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
        }

        // attach text watchers
        holder.amountField.addTextChangedListener(amountTextWatcher)
        holder.amountField.tag = amountTextWatcher
        holder.amountField.setText(ingredientList[position].amount)

        holder.ingredientField.addTextChangedListener(ingredientTextWatcher)
        holder.ingredientField.tag = ingredientTextWatcher
        holder.ingredientField.setText(ingredientList[position].ingredient)

    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }

}