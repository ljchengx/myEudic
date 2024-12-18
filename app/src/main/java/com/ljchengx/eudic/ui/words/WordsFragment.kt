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
import com.elvishew.xlog.XLog
import com.google.android.material.snackbar.Snackbar
import com.ljchengx.eudic.databinding.FragmentWordsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WordsFragment : Fragment() {

    private var _binding: FragmentWordsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WordsViewModel by viewModels()
    private lateinit var adapter: WordAdapter

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
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        adapter = WordAdapter(
            onDeleteClick = { word ->
                viewModel.deleteWord(word)
                Snackbar.make(binding.root, "已删除: $word", Snackbar.LENGTH_SHORT)
                    .setAction("撤销") {
                        // TODO: 实现撤销功能
                    }
                    .show()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@WordsFragment.adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        binding.refreshButton.setOnClickListener {
            refreshWords()
        }
    }

    private fun refreshWords() {
        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.refreshWords()
                Snackbar.make(binding.root, "刷新成功", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                XLog.e("刷新失败", e)
                Snackbar.make(binding.root, "刷新失败: ${e.message}", Snackbar.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.loadingView.root.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun observeViewModel() {
        viewModel.words.observe(viewLifecycleOwner) { words ->
            adapter.submitList(words)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 