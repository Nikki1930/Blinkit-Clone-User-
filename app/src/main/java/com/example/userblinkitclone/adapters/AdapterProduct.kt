package com.example.userblinkitclone.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.models.SlideModel
import com.example.userblinkitclone.FilteringProducts
import com.example.userblinkitclone.databinding.ItemViewProductBinding
import com.example.userblinkitclone.models.Product
import kotlin.apply
import kotlin.collections.forEach
import kotlin.toString

class AdapterProduct(
    val onAddButtonClicked: (Product, ItemViewProductBinding) -> Unit,
    val onIncrementButtonClicked : (Product, ItemViewProductBinding) -> Unit,
    val onDecrementButtonClicked : (Product, ItemViewProductBinding) -> Unit
) : RecyclerView.Adapter<AdapterProduct.ProductViewHolder>() , Filterable{

    class ProductViewHolder(val binding: ItemViewProductBinding) : RecyclerView.ViewHolder(binding.root)

    // DiffUtil setup for AsyncListDiffer
    private val diffUtil = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.productRandomId == newItem.productRandomId
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffUtil)

     // Filter-related variables
    private lateinit var filteringProducts: FilteringProducts
    var originalList = ArrayList<Product>()

    fun setProductList(list: ArrayList<Product>) {
        originalList = list
        differ.submitList(list)
       filteringProducts = FilteringProducts(this, originalList)
   }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemViewProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = differ.currentList[position]

        holder.binding.apply {
            val imageList = ArrayList<SlideModel>()
            product.productImagesUris?.forEach {
                imageList.add(SlideModel(it.toString()))
            }

            ivImageSlider.setImageList(imageList)
            tvProductTitle.text = product.productTitle

            val quantity = product.productQuantity.toString() + product.productUnit
            tvProductQuantity.text = quantity

            tvProductPrice.text = "₹${product.productPrice}"

            if(product.itemCount!! > 0){
                tvProductCount.text = product.itemCount.toString()
                tvAdd.visibility = View.GONE
                llProductCount.visibility = View.VISIBLE
            }


            tvAdd.setOnClickListener {
                onAddButtonClicked(product,this)
            }

            tvIncrementCount.setOnClickListener {
                onIncrementButtonClicked(product,this)
            }

            tvDecrementCount.setOnClickListener {
                onDecrementButtonClicked(product,this)
            }
        }


    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun getFilter(): Filter? {
        return filteringProducts
    }
}
