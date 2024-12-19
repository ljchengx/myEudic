package com.ljchengx.eudic.ui.wordbook

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
import com.ljchengx.eudic.databinding.FragmentWordbookSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WordbookSettingFragment : Fragment() {
    private var _binding: FragmentWordbookSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WordbookSettingViewModel by viewModels()
    private lateinit var wordbookAdapter: WordbookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordbookSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupViews() {
        // 设置工具栏返回按钮
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // 设置刷新按钮
        binding.fabAddWordbook.setOnClickListener {
            refreshWordbooks()
        }
    }

    private fun setupRecyclerView() {
        wordbookAdapter = WordbookAdapter { wordbook ->
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    viewModel.selectWordbook(wordbook.id)
                    wordbookAdapter.setSelectedWordbook(wordbook.id)
                    Snackbar.make(binding.root, "已选择单词本：${wordbook.name}", Snackbar.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    XLog.e("选择单词本失败", e)
                    Snackbar.make(binding.root, "选择失败：${e.message}", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.wordbookList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = wordbookAdapter
        }

        // 加载当前选中的单词本
        viewLifecycleOwner.lifecycleScope.launch {
            val selectedWordbook = viewModel.getSelectedWordbook()
            wordbookAdapter.setSelectedWordbook(selectedWordbook?.id)
        }
    }

    private fun observeViewModel() {
        viewModel.wordbooks.observe(viewLifecycleOwner) { wordbooks ->
            wordbookAdapter.submitList(wordbooks)
        }
    }

    private fun refreshWordbooks() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.refreshWordbooks()
                Snackbar.make(binding.root, "刷新成功", Snackbar.LENGTH_SHORT).show()
            } catch (e: Exception) {
                XLog.e("刷新单词本失败", e)
                when (e) {
                    is IllegalStateException -> {
                        if (e.message == "Token not found" || e.message == "Token is empty") {
                            Snackbar.make(binding.root, "请先设置Token", Snackbar.LENGTH_LONG)
                                .setAction("去设置") {
                                    findNavController().navigate(R.id.action_wordbookSettingFragment_to_tokenSettingFragment)
                                }
                                .show()
                        } else {
                            Snackbar.make(binding.root, "刷新失败：${e.message}", Snackbar.LENGTH_SHORT).show()
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