package com.example.userblinkitclone.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.R
import com.example.userblinkitclone.adapters.AdapterCartProducts
import com.example.userblinkitclone.databinding.FragmentOrderDetailBinding
import com.example.userblinkitclone.viewModels.UserViewModel
import kotlinx.coroutines.launch

class OrderDetailFragment : Fragment() {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentOrderDetailBinding
    private lateinit var adapterCartProducts: AdapterCartProducts
    private var status = 0
    private var orderId = " "

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false)

        getValues()
        setupStatus()
        onBackButtonClicked()
        viewLifecycleOwner.lifecycleScope.launch { getOrderedProducts() }

        setStatusBarColor()
        return binding.root
    }

    private suspend fun getOrderedProducts() {
        viewModel.getOrderedProducts(orderId).collect { cartList ->
            adapterCartProducts = AdapterCartProducts()
            binding.rvProductsItem.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(cartList)
        }
    }

    private fun setupStatus() {
        val green = ContextCompat.getColorStateList(requireContext(), R.color.green)
        val views = listOf(binding.iv1, binding.iv2, binding.view1, binding.view2, binding.iv3, binding.iv4, binding.view3)

        when (status) {
            0 -> views[0].backgroundTintList = green
            1 -> views.subList(0, 3).forEach { it.backgroundTintList = green }
            2 -> views.subList(0, 5).forEach { it.backgroundTintList = green }
            3 -> views.forEach { it.backgroundTintList = green }
        }
    }

    private fun getValues() {
        val bundle = arguments
        status = bundle?.getInt("status") ?: 0
        orderId = bundle?.getString("orderId").orEmpty()
    }

    private fun onBackButtonClicked() {
        binding.tbOrderDetailFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_orderDetailFragment_to_ordersFragment)
        }
    }

    private fun setStatusBarColor(){
        activity?.window?.apply{
            val StatusBarColors = ContextCompat.getColor(requireContext(), R.color.orange)
            statusBarColor = StatusBarColors
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}
