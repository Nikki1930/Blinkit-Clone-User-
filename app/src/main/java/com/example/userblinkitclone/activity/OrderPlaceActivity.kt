package com.example.userblinkitclone.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.userblinkitclone.CartListener
import com.example.userblinkitclone.R
import com.example.userblinkitclone.adapters.AdapterCartProducts
import com.example.userblinkitclone.constants
import com.example.userblinkitclone.databinding.ActivityOrderPlaceBinding
import com.example.userblinkitclone.databinding.AddressLayoutBinding
import com.example.userblinkitclone.databinding.ItemViewProductBinding
import com.example.userblinkitclone.models.Orders
import com.example.userblinkitclone.models.Product
import com.example.userblinkitclone.roomdb.CartProduct
import com.example.userblinkitclone.utils
import com.example.userblinkitclone.viewModels.UserViewModel
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest


class OrderPlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderPlaceBinding
    private val viewModel: UserViewModel by viewModels()
    private lateinit var adapterCartProducts: AdapterCartProducts
    private lateinit var b2BPGRequest: B2BPGRequest
    private var cartListener : CartListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllCartProducts()
        setStatusBarColor()
        backToUserMainActivity()
        onPlaceOrderClicked()
        initializePhonePe()
        saveOrder()


    }

    private fun saveOrder() {
        viewModel.getAll().observe(this){cartProductLists->

            if(cartProductLists.isNotEmpty()){
                viewModel.getUserAddress { address->
                    val order = Orders(
                        orderId = utils.getRandomId(),
                        orderList = cartProductLists,
                        userAddress = address,
                        orderStatus = 0,
                        orderDate = utils.getCurrentDate(),
                        orderingUserId = utils.getCurrentUserId()
                    )

                    viewModel.saveOrderedProducts(order)

                }
                for(products in cartProductLists){
                    val count = products.productCount
                    val stock = products.productStock?.minus(count!!)
                    if(stock != null){
                        viewModel.saveProductsAfterOrder(stock,products)
                    }
                }

            }

        }
    }

    private fun initializePhonePe() {

        val data = JSONObject()
        PhonePe.init(this, PhonePeEnvironment.SANDBOX,constants.MERCHANT_ID, " ")

        data.put("merchantId",constants.MERCHANT_ID)
        data.put("merchantTransactionId",constants.merchantTransactionId)
        data.put("amount",200)
        data.put("mobileNumber","9999999999")
        data.put("callbackurl","https://webhook.site/callback-url")

        val paymentInstrument = JSONObject()
        paymentInstrument.put("type","UPI_INTENT")
        paymentInstrument.put("targetApp","com.phonepe.simulator")

        data.put("paymentInstrument",paymentInstrument)

        val deviceContext = JSONObject()
        deviceContext.put("deviceOS","ANDROID")
        data.put("deviceContext",deviceContext)

        val payloadBase64 = Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()),Base64.NO_WRAP
        )

        val checksum = sha256(payloadBase64 + constants.apiEndPoint + constants.SALT_KEY) + "###1"

         b2BPGRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checksum)
            .setUrl(constants.apiEndPoint)
            .build()

    }

    private fun sha256(input : String) : String{
     val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") {str, it -> str + "%02x".format(it) }
    }

    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener {
            viewModel.getAddressStatus().observe(this){status->
                if(status){
                    getPaymentView()
                }
                else{
                    val addressLayoutBinding = AddressLayoutBinding.inflate(LayoutInflater.from(this))

                    val alertDialog = AlertDialog.Builder(this)
                        .setView(addressLayoutBinding.root)
                        .create()
                    alertDialog.show()

                    addressLayoutBinding.btnAdd.setOnClickListener {
                        saveAddress(alertDialog , addressLayoutBinding)
                    }
                }
            }
        }
    }

    val phonePayView = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            checkStatus()
        }
        else {
            utils.showToast(this, "Payment Failed")
        }
    }

    private fun checkStatus() {
        val xVerify = sha256("/pg/v1/status/${constants.MERCHANT_ID}/${constants.merchantTransactionId}/${constants.SALT_KEY}") + "###1"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "X-VERIFY" to xVerify,
            "X-MERCHANT-ID" to constants.MERCHANT_ID,

        )

        lifecycleScope.launch {
            viewModel.checkPayment(headers)
            viewModel.paymentStatus.collect{status->
                if(status){
                    utils.showToast(this@OrderPlaceActivity,"Payment Successful")
                    saveOrder()
                    viewModel.deleteCartProducts()
                    viewModel.savingCartItemCount(0)
                    cartListener?.hideCartLayout()
                    utils.hideDialog()
                    startActivity(Intent(this@OrderPlaceActivity,UsersMainActivity::class.java))
                    finish()
                }
                else{
                    utils.showToast(this@OrderPlaceActivity,"Payment Failed")
                }
            }

        }

    }



    private fun getPaymentView() {
        try{

            PhonePe.getImplicitIntent(this,b2BPGRequest,"com.phonepe.simulator")
                .let{
                    phonePayView.launch(it!!)
                }

        }
        catch(e : PhonePeInitException){
            utils.showToast(this,e.message.toString())
        }

    }

    private fun saveAddress(
        alertDialog: AlertDialog,
        addressLayoutBinding: AddressLayoutBinding
    ) {
        utils.showDialog(this,"Processing...")
        val userPinCode = addressLayoutBinding.etPinCode.text.toString()
        val userPhoneNo = addressLayoutBinding.etPhoneNo.text.toString()
        val userState = addressLayoutBinding.etState.text.toString()
        val userDistrict = addressLayoutBinding.etDistrict.text.toString()
        val userAddress = addressLayoutBinding.etAddress.text.toString()

        val address = "$userPinCode, $userDistrict($userState), $userAddress ,$userPhoneNo"



        lifecycleScope.launch {
            viewModel.saveUserAddress(address)
            viewModel.savedAddressStatus()

        }
        utils.showToast(this,"Saved...")
        alertDialog.dismiss()


        getPaymentView()
    }

    private fun backToUserMainActivity() {
        binding.tbOrderFragment.setNavigationOnClickListener {
            startActivity(Intent(this,UsersMainActivity::class.java))
            finish()
        }
    }

    private fun getAllCartProducts() {
        viewModel.getAll().observe(this){cartProductLists->

            adapterCartProducts = AdapterCartProducts()
            binding.rvProductsItems.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(cartProductLists)

            var totalPrice = 0

            for(product in cartProductLists){
                val price = product.productPrice?.substring(1)?.toInt()
                val itemCount = product.productCount!!
                totalPrice += (price ?. times(itemCount)!!)
            }

            binding.tvSubTotal.text = totalPrice.toString()

            if(totalPrice < 199) {
                binding.tvDeliveryCharges.text = "₹15"
                totalPrice += 15
            }

            binding.tvGrandTotal.text = totalPrice.toString()


        }
    }

//    private fun onAddButtonClicked(product : Product , productBinding : ItemViewProductBinding){
//        productBinding.tvAdd.visibility = View.GONE
//        productBinding.llProductCount.visibility = View.VISIBLE
//
//        //step 1
//        var itemCount = productBinding.tvProductCount.text.toString().toInt()
//        itemCount++
//
//        productBinding.tvProductCount.text = itemCount.toString()
//
//        cartListener?.showCartLayout(1)
//
//
//        //step 2
//        product.itemCount = itemCount
//        lifecycleScope.launch {
//            cartListener?.savingCartItemcount(1)
//            saveProductInRoomDb(product)
//            viewModel.updateItemCount(product,itemCount)
//        }
//
//
//
//
//    }
//
//
//    private fun onIncrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){
//
//        var itemCountInc = productBinding.tvProductCount.text.toString().toInt()
//        itemCountInc++
//
//        if(product.productStock!! + 1 > itemCountInc){
//            productBinding.tvProductCount.text = itemCountInc.toString()
//
//            cartListener?.showCartLayout(1)
//
//            //step 2
//            product.itemCount = itemCountInc
//            lifecycleScope.launch {
//                cartListener?.savingCartItemcount(1)
//                saveProductInRoomDb(product)
//                viewModel.updateItemCount(product,itemCountInc)
//            }
//        }
//        else{
//            utils.showToast(requireContext(),"Sorry, we have limited quantity available for this item")
//        }
//
//    }
//
//    private fun onDecrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){
//
//        var itemCountDec = productBinding.tvProductCount.text.toString().toInt()
//        itemCountDec--
//
//        //step 2
//        product.itemCount = itemCountDec
//        lifecycleScope.launch {
//            cartListener?.savingCartItemcount(-1)
//            saveProductInRoomDb(product)
//            viewModel.updateItemCount(product,itemCountDec)
//        }
//
//
//        if (itemCountDec > 0) {
//            productBinding.tvProductCount.text = itemCountDec.toString()
//        }
//        else{
//            lifecycleScope.launch{
//                viewModel.deleteCartProduct(product.productRandomId!!)
//            }
//            productBinding.tvAdd.visibility = View.VISIBLE
//            productBinding.llProductCount.visibility = View.GONE
//            productBinding.tvProductCount.text = "0"
//        }
//        cartListener?.showCartLayout(-1)
//
//
//    }
//
//
//    private fun saveProductInRoomDb(product : Product){
//
//        val cartProduct = CartProduct(
//            productId = product.productRandomId!!,
//            productTitle = product.productTitle,
//            productImage = product.productImagesUris?.get(0)!!,
//            productPrice = "₹" + "${product.productPrice}",
//            productQuantity = product.productQuantity.toString() + product.productUnit.toString(),
//            productCount = product.itemCount,
//            productStock = product.productStock,
//            productCategory = product.productCategory,
//            adminUId = product.adminUid,
//            productType = product.productType
//
//        )
//
//        lifecycleScope.launch {
//            viewModel.insertCartProduct(cartProduct)
//        }
//
//    }

    private fun setStatusBarColor(){
        window?.apply{
            val StatusBarColors = ContextCompat.getColor(this@OrderPlaceActivity, R.color.yellow)
            statusBarColor = StatusBarColors
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}


