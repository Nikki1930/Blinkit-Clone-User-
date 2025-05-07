package com.example.userblinkitclone.viewModels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.userblinkitclone.api.ApiUtilities
import com.example.userblinkitclone.constants
import com.example.userblinkitclone.models.Bestseller
import com.example.userblinkitclone.models.Orders
import com.example.userblinkitclone.models.Product
import com.example.userblinkitclone.roomdb.CartProduct
import com.example.userblinkitclone.roomdb.CartProductDao
import com.example.userblinkitclone.roomdb.CartProductDatabase
import com.example.userblinkitclone.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class UserViewModel(application: Application) : AndroidViewModel(application) {

    // SharedPreferences
    private val sharedPreferences: SharedPreferences = application.getSharedPreferences("My_Pref", MODE_PRIVATE)

    // Room Database
    private val cartProductDao: CartProductDao = CartProductDatabase.getDatabaseInstance(application).cartProductDao()

    private val _paymentStatus = MutableStateFlow(false)
    val paymentStatus: Flow<Boolean> = _paymentStatus

    // ------------------ ROOM DATABASE FUNCTIONS ------------------

    suspend fun insertCartProduct(product: CartProduct) {
        cartProductDao.insertCartProduct(product)
    }

    suspend fun updateCartProduct(product: CartProduct) {
        cartProductDao.updateCartProduct(product)
    }

    fun getAll(): LiveData<List<CartProduct>> {
        return cartProductDao.getAllCartProducts()
    }

    suspend fun deleteCartProduct(productId: String) {
        cartProductDao.deleteCartProduct(productId)
    }

    suspend fun deleteCartProducts() {
        cartProductDao.deleteCartProducts()
    }

    // ------------------ FIREBASE FUNCTIONS ------------------

    fun saveUserAddress(address: String) {
        FirebaseDatabase.getInstance()
            .getReference("AllUsers")
            .child("Users")
            .child(utils.getCurrentUserId())
            .child("userAddress")
            .setValue(address)
    }

    fun getUserAddress(callback: (String) -> Unit) {
        val db = FirebaseDatabase.getInstance()
            .getReference("AllUsers")
            .child("Users")
            .child(utils.getCurrentUserId())
            .child("userAddress")

        db.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val address = snapshot.getValue(String::class.java)
                callback(address ?: "")
            }

            override fun onCancelled(error: DatabaseError) {
                callback("")
            }
        })
    }

    fun saveAddress(address : String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(utils.getCurrentUserId()).child("userAddress").setValue(address)
    }

    fun logOutUser(){
        FirebaseAuth.getInstance().signOut()
    }

    fun saveOrderedProducts(orders: Orders) {
        FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("Orders")
            .child("Orders")
            .child(orders.orderId!!)
            .setValue(orders)
    }

    fun getAllOrders(): Flow<List<Orders>> = callbackFlow {
        val db = FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("Orders")
            .orderByChild("orderStatus")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ordersList = ArrayList<Orders>()
                for (order in snapshot.children) {
                    val orderData = order.getValue(Orders::class.java)
                    if (orderData?.orderingUserId == utils.getCurrentUserId()) {
                        ordersList.add(orderData)
                    }
                }
                trySend(ordersList)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun fetchAllTheProducts(): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("AllProducts")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    prod?.let { productsList.add(it) }
                }
                trySend(productsList)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun getCategoryProduct(category: String): Flow<List<Product>> = callbackFlow {
        val db = FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("ProductCategory/$category")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productsList = ArrayList<Product>()
                for (product in snapshot.children) {
                    val prod = product.getValue(Product::class.java)
                    prod?.let { productsList.add(it) }
                }
                trySend(productsList)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun getOrderedProducts(orderId: String): Flow<List<CartProduct>> = callbackFlow {
        val db = FirebaseDatabase.getInstance()
            .getReference("Admins")
            .child("Orders")
            .child(orderId)

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Orders::class.java)
                trySend(order?.orderList ?: emptyList())
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun updateItemCount(product: Product, itemCount: Int) {
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").child("itemCount").setValue(itemCount)
    }

    fun saveProductsAfterOrder(stock: Int, product: CartProduct) {
        val db = FirebaseDatabase.getInstance().getReference("Admins")
        db.child("AllProducts/${product.productId}").child("itemCount").setValue(0)
        db.child("ProductCategory/${product.productCategory}/${product.productId}").child("itemCount").setValue(0)
        db.child("ProductType/${product.productType}/${product.productId}").child("itemCount").setValue(0)

        db.child("AllProducts/${product.productId}").child("productStock").setValue(stock)
        db.child("ProductCategory/${product.productCategory}/${product.productId}").child("productStock").setValue(stock)
        db.child("ProductType/${product.productType}/${product.productId}").child("productStock").setValue(stock)
    }

    fun fetchProductTypes() : Flow<List<Bestseller>> = callbackFlow {
        val db = FirebaseDatabase.getInstance()
            .getReference("Admins/ProductType")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productTypeList = ArrayList<Bestseller>()
                for (productType in snapshot.children) {
                    val productTypeName = productType.key

                    val productList = ArrayList<Product>()

                    for (product in productType.children) {
                        val product = product.getValue(Product::class.java)
                        productList.add(product!!)
                    }

                    val bestseller =
                        Bestseller(productType = productTypeName, products = productList)
                    productTypeList.add(bestseller)
                }

                trySend(productTypeList)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        }

        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    // ------------------ SHARED PREFERENCES ------------------

    fun savingCartItemCount(itemCount: Int) {
        sharedPreferences.edit().putInt("itemCount", itemCount).apply()
    }

    fun fetchTotalCartItem(): MutableLiveData<Int> {
        val totalItemCount = MutableLiveData<Int>()
        totalItemCount.value = sharedPreferences.getInt("itemCount", 0)
        return totalItemCount
    }

    fun savedAddressStatus() {
        sharedPreferences.edit().putBoolean("addressStatus", true).apply()
    }

    fun getAddressStatus(): MutableLiveData<Boolean> {
        val status = MutableLiveData<Boolean>()
        status.value = sharedPreferences.getBoolean("addressStatus", false)
        return status
    }

    // ------------------ PAYMENT CHECK ------------------

    suspend fun checkPayment(headers: Map<String, String>) {
        val response = ApiUtilities.statusAPI.checkStatus(headers, constants.MERCHANT_ID, constants.merchantTransactionId)
        _paymentStatus.value = response.body()?.success == true
    }
}
