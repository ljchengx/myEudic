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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ljchengx.eudic.databinding.FragmentWidgetSettingBinding
import com.ljchengx.eudic.widget.WordWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WidgetSettingFragment : Fragment() {

    private var _binding: FragmentWidgetSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WidgetSettingViewModel by viewModels()

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
        binding.filterDaysGroup.setOnCheckedChangeListener { group, checkedId ->
            if (group.isPressed) {  // 只有用户操作才触发
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
            if (buttonView.isPressed) {  // 只有用户操作才触发
                viewModel.updateRandomOrder(isChecked)
                updateWidget()
            }
        }

        binding.hideExplanationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {  // 只有用户操作才触发
                viewModel.updateHideExplanation(isChecked)
                updateWidget()
            }
        }
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.settings.collect { settings ->
                // 更新UI状态，但不触发监听器
                binding.filterDaysGroup.setOnCheckedChangeListener(null)
                binding.randomOrderSwitch.setOnCheckedChangeListener(null)
                binding.hideExplanationSwitch.setOnCheckedChangeListener(null)

                // 更新选中状态
                val radioButton = when (settings.filterDays) {
                    1 -> binding.oneDay
                    2 -> binding.twoDays
                    3 -> binding.threeDays
                    else -> binding.oneDay
                }
                if (!radioButton.isChecked) {
                    radioButton.isChecked = true
                }

                if (binding.randomOrderSwitch.isChecked != settings.isRandomOrder) {
                    binding.randomOrderSwitch.isChecked = settings.isRandomOrder
                }
                if (binding.hideExplanationSwitch.isChecked != settings.hideExplanation) {
                    binding.hideExplanationSwitch.isChecked = settings.hideExplanation
                }

                // 重新设置监听器
                setupListeners()
            }
        }
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