package com.example.userblinkitclone.fragments

import android.content.Context
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
import com.example.userblinkitclone.CartListener
import com.example.userblinkitclone.R
import com.example.userblinkitclone.adapters.AdapterProduct
import com.example.userblinkitclone.databinding.FragmentCategoryBinding
import com.example.userblinkitclone.databinding.ItemViewProductBinding
import com.example.userblinkitclone.models.Product
import com.example.userblinkitclone.roomdb.CartProduct
import com.example.userblinkitclone.utils
import com.example.userblinkitclone.viewModels.UserViewModel
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {


    private lateinit var binding : FragmentCategoryBinding
    private val viewModel : UserViewModel by viewModels()
    private  var category : String ? = null
    private  var adapterProduct : AdapterProduct = AdapterProduct(::onAddButtonClicked,::onIncrementButtonClicked,::onDecrementButtonClicked)
    private var cartListener : CartListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCategoryBinding.inflate(layoutInflater)
        binding.rvProducts.adapter = adapterProduct



        getProductCategory()
        setToolbarTitle()
        onSearchMenuClick()
        onNavigationIconClick()
        fetchCategoryProduct()
        setStatusBarColor()
        return binding.root
    }

    private fun onNavigationIconClick() {
        binding.tbCategoryFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_categoryFragment_to_homeFragment)
        }
    }

    private fun onSearchMenuClick() {
        binding.tbCategoryFragment.setOnMenuItemClickListener {menuItem->
            when(menuItem.itemId){
                R.id.searchMenu -> {
                    findNavController().navigate(R.id.action_categoryFragment_to_searchFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchCategoryProduct() {

        binding.shimmerViewContainer.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.getCategoryProduct(category!!).collect{ products ->
                if (products.isEmpty()) {
                    binding.rvProducts.visibility = View.GONE
                    binding.tvText.visibility = View.VISIBLE
                } else {
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.tvText.visibility = View.GONE
                }


                adapterProduct.setProductList(ArrayList(products)) // This replaces originalList assignment
                binding.shimmerViewContainer.visibility = View.GONE

            }
        }


    }

    private fun setToolbarTitle() {
        binding.tbCategoryFragment.title = category
    }

    private fun getProductCategory() {
        val bundle = arguments
        category = bundle?.getString("category")
    }

    private fun onAddButtonClicked(product : Product , productBinding : ItemViewProductBinding){
        productBinding.tvAdd.visibility = View.GONE
        productBinding.llProductCount.visibility = View.VISIBLE

        //step 1
        var itemCount = productBinding.tvProductCount.text.toString().toInt()
        itemCount++

        productBinding.tvProductCount.text = itemCount.toString()

        cartListener?.showCartLayout(1)


        //step 2
        product.itemCount = itemCount
        viewLifecycleOwner.lifecycleScope.launch {
            cartListener?.savingCartItemcount(1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product,itemCount)
        }




    }


    private fun onIncrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){

        var itemCountInc = productBinding.tvProductCount.text.toString().toInt()
        itemCountInc++

        if(product.productStock!! + 1 > itemCountInc){
            productBinding.tvProductCount.text = itemCountInc.toString()

            cartListener?.showCartLayout(1)

            //step 2
            product.itemCount = itemCountInc
            viewLifecycleOwner.lifecycleScope.launch {
                cartListener?.savingCartItemcount(1)
                saveProductInRoomDb(product)
                viewModel.updateItemCount(product,itemCountInc)
            }
        }
        else{
            utils.showToast(requireContext(),"Sorry, we have limited quantity available for this item")
        }

    }

    private fun onDecrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){

        var itemCountDec = productBinding.tvProductCount.text.toString().toInt()
        itemCountDec--

        //step 2
        product.itemCount = itemCountDec
        viewLifecycleOwner.lifecycleScope.launch {
            cartListener?.savingCartItemcount(-1)
            saveProductInRoomDb(product)
            viewModel.updateItemCount(product,itemCountDec)
        }


        if (itemCountDec > 0) {
            productBinding.tvProductCount.text = itemCountDec.toString()
        }
        else{

            viewLifecycleOwner.lifecycleScope.launch{
                viewModel.deleteCartProduct(product.productRandomId!!)
            }
            productBinding.tvAdd.visibility = View.VISIBLE
            productBinding.llProductCount.visibility = View.GONE
            productBinding.tvProductCount.text = "0"
        }
        cartListener?.showCartLayout(-1)


    }


    private fun saveProductInRoomDb(product : Product){

        val cartProduct = CartProduct(
            productId = product.productRandomId!!,
            productTitle = product.productTitle,
            productImage = product.productImagesUris?.get(0)!!,
            productPrice = "₹" + "${product.productPrice}",
            productQuantity = product.productQuantity.toString() + product.productUnit.toString(),
            productCount = product.itemCount,
            productStock = product.productStock,
            productCategory = product.productCategory,
            adminUId = product.adminUid,
            productType = product.productType

        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.insertCartProduct(cartProduct)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(context is CartListener){

            cartListener = context
        }
        else{
            throw ClassCastException("Please implement CartListener")
        }

    }

}