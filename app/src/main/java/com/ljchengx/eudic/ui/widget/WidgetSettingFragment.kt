package com.ljchengx.eudic.ui.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.ljchengx.eudic.databinding.FragmentWidgetSettingBinding
import com.ljchengx.eudic.widget.WordWidget
import com.ljchengx.eudic.data.model.WidgetSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WidgetSettingFragment : Fragment() {

    private var _binding: FragmentWidgetSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WidgetSettingViewModel by viewModels()
    private var isSettingInitialized = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWidgetSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupListeners()
        observeSettings()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupListeners() {
        binding.filterDaysGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked && isSettingInitialized) {  // 只在初始化完成后处理选中事件
                val days = when (checkedId) {
                    binding.oneDay.id -> 1
                    binding.twoDays.id -> 2
                    binding.threeDays.id -> 3
                    else -> 1
                }
                viewModel.updateFilterDays(days)
                updateWidget()
            }
        }

        binding.randomOrderSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed && isSettingInitialized) {  // 只在初始化完成后处理用户操作
                viewModel.updateRandomOrder(isChecked)
                updateWidget()
            }
        }

        binding.hideExplanationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed && isSettingInitialized) {  // 只在初始化完成后处理用户操作
                viewModel.updateHideExplanation(isChecked)
                updateWidget()
            }
        }
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { settings ->
                    isSettingInitialized = false
                    applySettings(settings)
                    isSettingInitialized = true
                }
            }
        }
    }

    private fun applySettings(settings: WidgetSettings) {
        // 设置过滤天数
        val buttonId = when (settings.filterDays) {
            1 -> binding.oneDay.id
            2 -> binding.twoDays.id
            3 -> binding.threeDays.id
            else -> binding.oneDay.id
        }
        binding.filterDaysGroup.check(buttonId)

        // 设置其他开关状态
        binding.randomOrderSwitch.isChecked = settings.isRandomOrder
        binding.hideExplanationSwitch.isChecked = settings.hideExplanation
    }

    private fun updateWidget() {
        val context = requireContext()
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(context, WordWidget::class.java)
        )
        
        // 发送广播更新小组件
        val intent = Intent(context, WordWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 