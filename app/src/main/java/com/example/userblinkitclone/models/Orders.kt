package com.example.userblinkitclone.models

import com.example.userblinkitclone.roomdb.CartProduct

data class Orders(
    val orderId : String? = null,
    val orderList : List<CartProduct>? = null,
    val userAddress : String? = null,
    val orderStatus : Int ? = 0,
    val orderDate : String? = null,
    val orderingUserId : String? = null

)