package com.example.userblinkitclone.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.userblinkitclone.databinding.ItemViewCartProductsBinding
import com.example.userblinkitclone.roomdb.CartProduct

class AdapterCartProducts : RecyclerView.Adapter<AdapterCartProducts.CartProductsViewHolder>() {

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<CartProduct>() {

        override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
            return oldItem == newItem
        }
    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductsViewHolder {
        val binding = ItemViewCartProductsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartProductsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.binding.apply {
            Glide.with(holder.itemView.context).load(product.productImage).into(ivProductImage)
            tvProductTitle.text = product.productTitle
            tvProductQuantity.text = product.productQuantity
            tvProductPrice.text = product.productPrice
            tvProductCount.text = product.productCount.toString()
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    class CartProductsViewHolder(val binding: ItemViewCartProductsBinding) : RecyclerView.ViewHolder(binding.root)
}
