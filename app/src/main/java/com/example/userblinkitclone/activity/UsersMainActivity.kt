package com.example.userblinkitclone.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.userblinkitclone.CartListener
import com.example.userblinkitclone.R
import com.example.userblinkitclone.adapters.AdapterCartProducts
import com.example.userblinkitclone.databinding.ActivityUsersMainBinding
import com.example.userblinkitclone.databinding.BsCartProductsBinding
import com.example.userblinkitclone.roomdb.CartProduct
import com.example.userblinkitclone.utils
import com.example.userblinkitclone.viewModels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog

class UsersMainActivity : AppCompatActivity() , CartListener {

    private lateinit var binding : ActivityUsersMainBinding
    private val viewModel : UserViewModel by viewModels()
    private lateinit var cartProductList : List<CartProduct>
    private lateinit var adapterCartProducts: AdapterCartProducts
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        getAllCartProducts()
        getTotalItemCountInCart()

        onCartClicked()
        onNextButtonClicked()

    }

    private fun onNextButtonClicked() {
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, OrderPlaceActivity::class.java))
        }
    }

    private fun getAllCartProducts() {
        viewModel.getAll().observe(this){
            cartProductList = it

        }
    }

    override fun onCartClicked() {
        binding.llItemCart.setOnClickListener {
            val bsCartProductsBinding = BsCartProductsBinding.inflate(LayoutInflater.from(this))

            val bs = BottomSheetDialog(this)
            bs.setContentView(bsCartProductsBinding.root)

            bsCartProductsBinding.tvNumberOfProductCount.text = binding.tvNumberOfProductCount.text.toString()
            bsCartProductsBinding.btnNext.setOnClickListener {
                startActivity(Intent(this, OrderPlaceActivity::class.java))
            }
            adapterCartProducts = AdapterCartProducts()

            bsCartProductsBinding.rvProductItems.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(cartProductList)

            bs.show()
        }

    }

    override fun hideCartLayout() {
        binding.llCart.visibility = View.GONE
        binding.tvNumberOfProductCount.text = "0"
    }

    private fun getTotalItemCountInCart() {
        viewModel.fetchTotalCartItem().observe(this){
            if(it > 0){
                binding.llCart.visibility = View.VISIBLE
                binding.tvNumberOfProductCount.text = it.toString()
            }
            else{
                binding.llCart.visibility = View.GONE
            }
        }
    }

    override fun showCartLayout(itemCount: Int) {
        val previousCount = binding.tvNumberOfProductCount.text.toString().toInt()
        val updatedCount = previousCount + itemCount

        if(updatedCount > 0){
            binding.llCart.visibility = View.VISIBLE
            binding.tvNumberOfProductCount.text = updatedCount.toString()

        }
        else{
            binding.llCart.visibility = View.GONE
            binding.tvNumberOfProductCount.text = "0"

        }

    }

    override fun savingCartItemcount(itemCount: Int) {
       viewModel.fetchTotalCartItem().observe(this){
           viewModel.savingCartItemCount(it + itemCount)
       }

    }
}