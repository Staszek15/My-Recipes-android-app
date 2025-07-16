package com.staszek15.serveit.mealList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.staszek15.serveit.R
import com.staszek15.serveit.mealDB.MealItemClass
import com.staszek15.serveit.databinding.MealListItemBinding

class MealListAdapter(
    private val mealItemsList: List<Pair<MealItemClass, String?>>,
    private val listener: RecyclerViewEvent
) :
    RecyclerView.Adapter<MealListAdapter.MealListViewHolder>() {

    inner class MealListViewHolder(binding: MealListItemBinding) :
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

        // if no second element of pair (Firestore document name)
        // then it is a default meal, load its image from local db
        // use name identifier cause DrawableRes depends on device/update
        if (mealItemsList[position].second != null) {
            Glide
                .with(holder.image.context)
                .load(mealItemsList[position].first.imageUrl)
                .error(R.drawable.empty_image)
                .into(holder.image)
        } else {
            val resId = holder.image.context.resources.getIdentifier(
                mealItemsList[position].first.drawableName,
                "drawable",
                holder.image.context.packageName
            )
            if (resId != 0) {
                holder.image.setImageResource(resId)
            } else {
                holder.image.setImageResource(R.drawable.empty_image)
            }
        }
        // favorite label visible only for favorite records
        if (mealItemsList[position].first.favourite) {
            holder.favLabel.visibility = View.VISIBLE
        } else {
            holder.favLabel.visibility = View.INVISIBLE
        }

    }


    interface RecyclerViewEvent {
        fun myOnItemClick(position: Int)
    }
}