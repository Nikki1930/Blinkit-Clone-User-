package com.example.userblinkitclone

interface CartListener {

    fun showCartLayout(itemCount : Int)

    fun savingCartItemcount(itemCount : Int)

    fun onCartClicked()

    fun hideCartLayout()
}