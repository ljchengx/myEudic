package com.ljchengx.eudic.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ljchengx.eudic.R
import com.ljchengx.eudic.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
        // Token设置
        binding.tokenSetting.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_tokenSettingFragment)
        }

        // 单词本设置
        binding.wordbookSetting.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_wordbookSettingFragment)
        }

        // 小组件设置
        binding.widgetSetting.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_widgetSettingFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 