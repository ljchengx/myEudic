package com.ljchengx.eudic.ui.token

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.elvishew.xlog.XLog
import com.google.android.material.snackbar.Snackbar
import com.ljchengx.eudic.databinding.FragmentTokenSettingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TokenSettingFragment : Fragment() {

    private var _binding: FragmentTokenSettingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TokenSettingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTokenSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
    }

    private fun setupViews() {
        // 设置工具栏返回按钮
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // 设置输入框
        binding.tokenInput.apply {
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    saveToken()
                    true
                } else {
                    false
                }
            }
        }

        // 加载已保存的Token
        viewLifecycleOwner.lifecycleScope.launch {
            val savedToken = viewModel.getSavedToken()
            binding.tokenInput.setText(savedToken as CharSequence)
        }

        // 设置保存按钮
        binding.saveButton.setOnClickListener {
            saveToken()
        }
    }

    private fun saveToken() {
        val token = binding.tokenInput.text?.toString()?.trim()
        if (token.isNullOrBlank()) {
            binding.tokenInputLayout.error = "Token不能为空"
            return
        }

        XLog.d("保存Token")
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.saveToken(token)
                Snackbar.make(binding.root, "Token保存成功", Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } catch (e: Exception) {
                XLog.e("保存Token失败", e)
                Snackbar.make(binding.root, "保存失败: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        // 观察ViewModel中的状态变化
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 