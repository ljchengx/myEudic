package com.ljchengx.eudic.ui.words

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.elvishew.xlog.XLog
import com.google.android.material.snackbar.Snackbar
import com.ljchengx.eudic.R
import com.ljchengx.eudic.databinding.FragmentWordsBinding
import dagger.hilt.android.AndroidEntryPoint
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
        setupViews()
        setupRecyclerView()
        observeViewModel()
        refreshWords()
    }

    private fun setupViews() {
        binding.refreshButton.setOnClickListener {
            refreshWords()
        }
    }

    private fun setupRecyclerView() {
        wordsAdapter = WordsAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = wordsAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.words.observe(viewLifecycleOwner) { words ->
            wordsAdapter.submitList(words)
        }

        viewModel.currentWordbookName.observe(viewLifecycleOwner) { name ->
            binding.toolbarTitle.text = name
        }
    }

    private fun refreshWords() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.refreshWords()
                Snackbar.make(binding.root, "刷新成功", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                XLog.e("刷新单词列表失败", e)
                when (e) {
                    is IllegalStateException -> {
                        when (e.message) {
                            "Token not found", "Token is empty" -> {
                                Snackbar.make(binding.root, "请先设置Token", Snackbar.LENGTH_LONG)
                                    .setAction("去设置") {
                                        findNavController().navigate(R.id.action_wordsFragment_to_tokenSettingFragment)
                                    }
                                    .show()
                            }
                            "No wordbook selected" -> {
                                viewLifecycleOwner.lifecycleScope.launch {
                                    try {
                                        // 尝试刷新单词本列表
                                        viewModel.refreshWordbooks()
                                        // 再次尝试刷新单词列表
                                        viewModel.refreshWords()
                                    } catch (e2: Exception) {
                                        // 如果还是失败，提示用户去选择单词本
                                        Snackbar.make(binding.root, "请先选择单词本", Snackbar.LENGTH_LONG)
                                            .setAction("去选择") {
                                                findNavController().navigate(R.id.action_wordsFragment_to_wordbookSettingFragment)
                                            }
                                            .show()
                                    }
                                }
                            }
                            else -> {
                                Snackbar.make(binding.root, "刷新失败：${e.message}", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else -> {
                        Snackbar.make(binding.root, "刷新失败：${e.message}", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 