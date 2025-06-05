package com.staszek15.myrecipes.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.staszek15.myrecipes.databinding.HomeListItemBinding

class HomeListAdapter(
    private val homeItemsList: List<HomeItemClass>,
    private val listener: RecyclerViewEvent
) :
    RecyclerView.Adapter<HomeListAdapter.HomeListViewHolder>() {

    inner class HomeListViewHolder(binding: HomeListItemBinding) :
        RecyclerView.ViewHolder(binding.root), View.OnClickListener{
        val image = binding.imageViewItem
        //val text = binding.text

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HomeListItemBinding.inflate(inflater, parent, false)
        return HomeListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return homeItemsList.size
    }


    override fun onBindViewHolder(holder: HomeListViewHolder, position: Int) {

        //holder.text.text = homeItemsList[position].text
        holder.image.setImageResource(homeItemsList[position].image)
    }


    interface RecyclerViewEvent{
        fun myOnItemClick(position: Int)
    }

}