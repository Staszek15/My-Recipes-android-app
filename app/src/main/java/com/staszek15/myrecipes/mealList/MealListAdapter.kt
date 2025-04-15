package com.staszek15.myrecipes.mealList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.staszek15.myrecipes.R
import com.staszek15.myrecipes.mealDB.MealItemClass
import com.staszek15.myrecipes.databinding.MealListItemBinding

class MealListAdapter(
    private val mealItemsList: List<Pair<MealItemClass,String>>,
    private val listener: RecyclerViewEvent
):
    RecyclerView.Adapter<MealListAdapter.MealListViewHolder>(){

    inner class MealListViewHolder(binding: MealListItemBinding):
        RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        val heading = binding.textViewHeading
        val description = binding.textViewDescription
        val image = binding.imageViewItem
        val rating = binding.ratingBar
        val favLabel = binding.favLabel

        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.myOnItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MealListItemBinding.inflate(inflater, parent, false)
        return MealListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return mealItemsList.size
    }

    override fun onBindViewHolder(holder: MealListViewHolder, position: Int) {
        holder.heading.text = mealItemsList[position].first.title
        holder.description.text = mealItemsList[position].first.description
        holder.rating.rating = mealItemsList[position].first.rating
        // TODO: set image
        holder.image.setImageResource(R.drawable.dinner)

        // favorite label visible only for favorite records
        if (mealItemsList[position].first.favourite) {
            holder.favLabel.visibility = View.VISIBLE
        }
    }


    interface RecyclerViewEvent{
        fun myOnItemClick(position: Int)
    }
}