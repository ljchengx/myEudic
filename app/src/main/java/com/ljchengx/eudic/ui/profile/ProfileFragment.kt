package com.ljchengx.eudic.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ljchengx.eudic.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.tokenSetting.setOnClickListener {
            // TODO: 实现 Token 设置
        }
        binding.widgetSetting.setOnClickListener {
            // TODO: 实现小组件设置
        }
        binding.aboutUs.setOnClickListener {
            // TODO: 实现关于我们
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 