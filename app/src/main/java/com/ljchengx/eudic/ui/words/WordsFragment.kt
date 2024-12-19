package com.ljchengx.eudic.ui.words

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.appwidget.AppWidgetManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ljchengx.eudic.R
import com.ljchengx.eudic.databinding.FragmentWordsBinding
import com.ljchengx.eudic.widget.WordWidget
import com.elvishew.xlog.XLog
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
            // 添加分割线
            addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: android.graphics.Rect,
                    view: View,
                    parent: androidx.recyclerview.widget.RecyclerView,
                    state: androidx.recyclerview.widget.RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    outRect.left = 0
                    outRect.right = 0
                    outRect.top = if (position == 0) 0 else 12
                    outRect.bottom = if (position == parent.adapter?.itemCount?.minus(1)) 12 else 0
                }
            })
        }
    }

    private fun observeViewModel() {
        viewModel.words.observe(viewLifecycleOwner) { words ->
            wordsAdapter.submitList(words)
            binding.wordCount.text = "${words.size}词"
        }

        viewModel.currentWordbookName.observe(viewLifecycleOwner) { name ->
            binding.toolbarTitle.text = name
        }
    }

    private fun refreshWords() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                XLog.d("开始刷新单词列表")
                // 刷新单词列表
                viewModel.refreshWords()
                Snackbar.make(binding.root, "刷新成功", Snackbar.LENGTH_SHORT).show()
                XLog.d("单词列表刷新成功")
                
                // 等待一小段时间确保数据库更新完成
                kotlinx.coroutines.delay(1000)
                XLog.d("开始更新小组件")
                
                // 更新小组件
                val context = requireContext()
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, WordWidget::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                
                if (appWidgetIds.isEmpty()) {
                    XLog.d("没有找到已添加的小组件")
                    return@launch
                }
                
                XLog.d("找到 ${appWidgetIds.size} 个小组件，发送更新广播")
                val intent = Intent(context, WordWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
                XLog.d("小组件更新广播已发送")
                
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
                                        XLog.d("尝试刷新单词本列表")
                                        // 尝试刷新单词本列表
                                        viewModel.refreshWordbooks()
                                        // 再次尝试刷新单词列表
                                        viewModel.refreshWords()
                                        XLog.d("单词本和单词列表刷新成功")
                                    } catch (e2: Exception) {
                                        XLog.e("刷新单词本和单词列表失败", e2)
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