package com.example.userblinkitclone.roomdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CartProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCartProduct(products : CartProduct)

    @Update
    fun updateCartProduct(products : CartProduct)

    @Query("SELECT * FROM CartProduct")
    fun getAllCartProducts() : LiveData<List<CartProduct>>

    @Query("DELETE FROM cartProduct WHERE productId = :productId")
    fun deleteCartProduct(productId : String)

    @Query("DELETE FROM CartProduct")
    suspend fun deleteCartProducts()

}