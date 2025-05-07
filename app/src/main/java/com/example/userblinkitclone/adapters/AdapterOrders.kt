package com.example.userblinkitclone.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.userblinkitclone.R
import com.example.userblinkitclone.databinding.ItemViewOrdersBinding
import com.example.userblinkitclone.models.OrderedItems

class AdapterOrders(
    private val context: Context,
    private val onOrderItemViewClicked: (OrderedItems) -> Unit
) : RecyclerView.Adapter<AdapterOrders.OrdersViewHolder>() {

    val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<OrderedItems>() {
        override fun areItemsTheSame(oldItem: OrderedItems, newItem: OrderedItems): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: OrderedItems, newItem: OrderedItems): Boolean {
            return oldItem == newItem
        }
    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val binding = ItemViewOrdersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrdersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = differ.currentList[position]
        holder.binding.apply {
            tvOrderDate.text = order.itemDate
            tvOrderTitles.text = order.itemTitle
            tvOrderAmount.text = "₹${order.itemPrice}"

            when (order.itemStatus) {
                0 -> {
                    tvOrderStatus.text = "Ordered"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.yellow)
                }
                1 -> {
                    tvOrderStatus.text = "Received"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.blue)
                }
                2 -> {
                    tvOrderStatus.text = "Dispatched"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.orange)
                }
                3 -> {
                    tvOrderStatus.text = "Delivered"
                    tvOrderStatus.backgroundTintList = ContextCompat.getColorStateList(context, R.color.green)
                }
            }
        }

        holder.itemView.setOnClickListener {
            onOrderItemViewClicked(order)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    class OrdersViewHolder(val binding: ItemViewOrdersBinding) : RecyclerView.ViewHolder(binding.root)
}
