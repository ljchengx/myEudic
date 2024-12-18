package com.ljchengx.eudic.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialFadeThrough
import com.ljchengx.eudic.R
import com.ljchengx.eudic.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }

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
        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // 添加头像点击波纹效果
        binding.avatar.apply {
            isClickable = true
            isFocusable = true
            foreground = requireContext().getDrawable(R.drawable.ripple_circular)
        }

        // 添加进入动画
        val fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        binding.avatar.startAnimation(fadeIn)
        binding.username.startAnimation(fadeIn)

        binding.tokenSetting.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_tokenSettingFragment)
        }
    }

    private fun setupClickListeners() {
        binding.avatar.setOnClickListener {
            // TODO: 实现头像更换功能
        }

        binding.wordbookSetting.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_wordbookSettingFragment)
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