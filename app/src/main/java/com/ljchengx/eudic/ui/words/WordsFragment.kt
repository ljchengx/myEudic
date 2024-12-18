package com.ljchengx.eudic.ui.words

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ljchengx.eudic.data.entity.WordEntity
import com.ljchengx.eudic.databinding.FragmentWordsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WordsFragment : Fragment() {
    private var _binding: FragmentWordsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WordsViewModel by viewModels()
    private lateinit var wordsAdapter: WordsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupRefreshButton()
        observeWords()
        observeRefreshState()
    }

    private fun setupRecyclerView() {
        wordsAdapter = WordsAdapter { word ->
            viewModel.deleteWord(word)
        }
        binding.wordList.apply {
            adapter = wordsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupRefreshButton() {
        binding.refreshButton.setOnClickListener {
            viewModel.refreshWords()
        }
    }

    private fun observeWords() {
        viewModel.words.observe(viewLifecycleOwner) { words: List<WordEntity> ->
            wordsAdapter.submitList(words)
        }
    }

    private fun observeRefreshState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isRefreshing.collectLatest { isRefreshing ->
                binding.refreshButton.isEnabled = !isRefreshing
                if (isRefreshing) {
                    binding.refreshButton.animate()
                        .rotation(binding.refreshButton.rotation + 360f)
                        .setDuration(1000)
                        .start()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 