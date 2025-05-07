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
import com.example.userblinkitclone.adapters.AdapterBestseller
import com.example.userblinkitclone.adapters.AdapterCategory
import com.example.userblinkitclone.adapters.AdapterProduct
import com.example.userblinkitclone.constants
import com.example.userblinkitclone.databinding.BsSeeAllBinding
import com.example.userblinkitclone.databinding.FragmentHomeBinding
import com.example.userblinkitclone.databinding.ItemViewProductBinding
import com.example.userblinkitclone.models.Bestseller
import com.example.userblinkitclone.models.Category
import com.example.userblinkitclone.models.Product
import com.example.userblinkitclone.roomdb.CartProduct
import com.example.userblinkitclone.utils
import com.example.userblinkitclone.viewModels.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    private val viewModel : UserViewModel by viewModels()
    private lateinit var adapterBestseller: AdapterBestseller
    private lateinit var adapterProduct : AdapterProduct
    private var cartListener : CartListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHomeBinding.inflate(layoutInflater)
        setAllCategories()
        setStatusBarColor()
        navigatingToSearchFragment()
        onProfileClicked()
        fetchBestseller()
        return binding.root
    }

    private fun fetchBestseller() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.fetchProductTypes().collect{
                adapterBestseller = AdapterBestseller(::onSeeAllButtonClicked)
                binding.rvBestSellers.adapter = adapterBestseller
                adapterBestseller.differ.submitList(it)
                binding.shimmerViewContainer.visibility = View.GONE
            }

        }
    }

    private fun onProfileClicked() {
        binding.ivProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    private fun navigatingToSearchFragment() {
        binding.searchCv.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun setAllCategories() {
        val categoryList = ArrayList<Category>()

        for(i in 0  until  constants.allProductsCategoryIcons.size){
            categoryList.add(
                Category(
                    constants.allProductCategories[i],
                    constants.allProductsCategoryIcons[i]
                )
            )
        }

        binding.rvCategories.adapter = AdapterCategory(categoryList, ::onCategoryIconClicked)
    }

    fun onCategoryIconClicked(category: Category){
        val bundle = Bundle()
        bundle.putString("category", category.title)
        findNavController().navigate(R.id.action_homeFragment_to_categoryFragment,bundle)

    }

    fun onSeeAllButtonClicked(productType : Bestseller){
        val bsSeeAllBinding = BsSeeAllBinding.inflate(LayoutInflater.from(requireContext()))
        val bs = BottomSheetDialog(requireContext())
        bs.setContentView(bsSeeAllBinding.root)

        adapterProduct = AdapterProduct(::onAddButtonClicked, ::onIncrementButtonClicked, ::onDecrementButtonClicked)
        bsSeeAllBinding.rvProducts.adapter = adapterProduct
        adapterProduct.differ.submitList(productType.products)
        bs.show()
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