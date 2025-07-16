package com.staszek15.serveit.mealAdd

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import com.staszek15.serveit.databinding.IngredientAddItemBinding


class AddIngredientAdapter(
    private var ingredientList: MutableList<IngredientClass>,
    private var showXButton: Boolean):
    RecyclerView.Adapter<AddIngredientAdapter.AddIngredientViewHolder>(){

    inner class AddIngredientViewHolder(binding: IngredientAddItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        val amountField = binding.etAmount
        val ingredientField = binding.etIngredient
        val removeButton = binding.btnRemove
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddIngredientViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = IngredientAddItemBinding.inflate(inflater, parent, false)
        return AddIngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddIngredientViewHolder, position: Int) {

        holder.amountField.imeOptions = EditorInfo.IME_ACTION_DONE
        holder.amountField.setRawInputType(InputType.TYPE_CLASS_TEXT)
        holder.ingredientField.imeOptions = EditorInfo.IME_ACTION_DONE
        holder.ingredientField.setRawInputType(InputType.TYPE_CLASS_TEXT)

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

        if (showXButton) {
            holder.removeButton.visibility = View.VISIBLE
            holder.removeButton.setOnClickListener {
                ingredientList.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
            }
        } else {
            holder.removeButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }
}