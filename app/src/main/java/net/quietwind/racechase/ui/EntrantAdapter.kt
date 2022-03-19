package net.quietwind.racechase.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.quietwind.racechase.databinding.EntrantItemBinding
import net.quietwind.racechase.repository.EntrantIdentity

class EntrantAdapter():
    ListAdapter<EntrantIdentity, EntrantAdapter.EntrantViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<EntrantIdentity>() {
            override fun areItemsTheSame(oldItem: EntrantIdentity, newItem: EntrantIdentity): Boolean {
                Log.d("RaceChase", "Diff Callback areItemsTheSame %s\n\t%s".format(oldItem.toString(), newItem,toString()))
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: EntrantIdentity, newItem: EntrantIdentity): Boolean {
                Log.d("RaceChase", "Diff Callback areContentsTheSame %s\n\t%s".format(oldItem.toString(), newItem,toString()))
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntrantViewHolder {
        val viewHolder = EntrantViewHolder(
            EntrantItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: EntrantViewHolder, position: Int) {
       holder.bind(getItem(position))
    }

    class EntrantViewHolder(
        private var binding: EntrantItemBinding
    ): RecyclerView.ViewHolder(binding.root) {

        fun bind(entrantIdentity: EntrantIdentity) {
            Log.d("RaceChase", "Putting entry %s into ViewHolder text".format(entrantIdentity.toString()))
            binding.entrantTextView.text = entrantIdentity.toString()
        }
    }
}