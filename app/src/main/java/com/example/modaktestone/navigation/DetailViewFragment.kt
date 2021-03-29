package com.example.modaktestone.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.modaktestone.R
import com.example.modaktestone.databinding.FragmentDetailBinding

class DetailViewFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
//        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail, container, false)
//        return super.onCreateView(inflater, container, savedInstanceState)
    }
}