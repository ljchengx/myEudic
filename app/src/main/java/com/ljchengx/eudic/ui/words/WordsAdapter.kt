package com.ljchengx.eudic.ui.words

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ljchengx.eudic.data.entity.WordEntity
import com.ljchengx.eudic.databinding.ItemWordBinding

class WordsAdapter(
    private val onDeleteClick: (WordEntity) -> Unit
) : ListAdapter<WordEntity, WordsAdapter.WordViewHolder>(WordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        return WordViewHolder(
            ItemWordBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WordViewHolder(
        private val binding: ItemWordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(word: WordEntity) {
            binding.wordText.text = word.word
            binding.meaningText.text = word.explanation
            binding.deleteButton.setOnClickListener {
                onDeleteClick(word)
            }
        }
    }

    private class WordDiffCallback : DiffUtil.ItemCallback<WordEntity>() {
        override fun areItemsTheSame(oldItem: WordEntity, newItem: WordEntity): Boolean {
            return oldItem.word == newItem.word
        }

        override fun areContentsTheSame(oldItem: WordEntity, newItem: WordEntity): Boolean {
            return oldItem == newItem
        }
    }
} 