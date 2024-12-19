package com.ljchengx.eudic.ui.wordbook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ljchengx.eudic.data.entity.WordbookEntity
import com.ljchengx.eudic.databinding.ItemWordbookBinding

class WordbookAdapter(
    private val onWordbookSelected: (WordbookEntity) -> Unit
) : ListAdapter<WordbookEntity, WordbookAdapter.ViewHolder>(WordbookDiffCallback()) {

    private var selectedWordbookId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWordbookBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wordbook = getItem(position)
        holder.bind(wordbook)
    }

    fun setSelectedWordbook(wordbookId: String?) {
        val oldSelectedId = selectedWordbookId
        selectedWordbookId = wordbookId
        
        if (oldSelectedId != null) {
            val oldPosition = currentList.indexOfFirst { it.id == oldSelectedId }
            if (oldPosition != -1) {
                notifyItemChanged(oldPosition)
            }
        }
        
        if (wordbookId != null) {
            val newPosition = currentList.indexOfFirst { it.id == wordbookId }
            if (newPosition != -1) {
                notifyItemChanged(newPosition)
            }
        }
    }

    inner class ViewHolder(
        private val binding: ItemWordbookBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val wordbook = getItem(position)
                    onWordbookSelected(wordbook)
                }
            }
        }

        fun bind(wordbook: WordbookEntity) {
            binding.wordbookName.text = wordbook.name
            binding.selectedIcon.visibility = if (wordbook.id == selectedWordbookId) View.VISIBLE else View.GONE
        }
    }
}

private class WordbookDiffCallback : DiffUtil.ItemCallback<WordbookEntity>() {
    override fun areItemsTheSame(oldItem: WordbookEntity, newItem: WordbookEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WordbookEntity, newItem: WordbookEntity): Boolean {
        return oldItem == newItem
    }
} 