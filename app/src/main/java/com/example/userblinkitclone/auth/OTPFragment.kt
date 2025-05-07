package com.example.userblinkitclone.auth

import AuthViewModel
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.R
import com.example.userblinkitclone.activity.UsersMainActivity
import com.example.userblinkitclone.databinding.FragmentOTPBinding
import com.example.userblinkitclone.models.Users
import com.example.userblinkitclone.utils
import kotlinx.coroutines.launch


class OTPFragment : Fragment() {

    private val viewModel : AuthViewModel by viewModels()
    private lateinit var binding: FragmentOTPBinding
    private lateinit var userNumber : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOTPBinding.inflate(layoutInflater)

        getUserNumber()
        customisingEnteringOtp()
        getOTP()
        onLoginButtonClick()
        onBackButtonClicked()
        return binding.root
    }

    private fun onLoginButtonClick() {
        binding.btnLogin.setOnClickListener {
            utils.showDialog(requireContext(),"Verifying OTP...")
            val editTexts = arrayOf(binding.etOtp1,binding.etOtp2,binding.etOtp3,binding.etOtp4,binding.etOtp5,binding.etOtp6)
            val otp = editTexts.joinToString(""){
                it.text.toString()
            }

            if(otp.length != editTexts.size){
                utils.showToast(requireContext(),"Please enter the valid OTP")
            }
            else{
                editTexts.forEach{ it.text?.clear(); it.clearFocus() }
                verifyOtp(otp)
            }
        }
    }
    private fun verifyOtp(otp: String) {

        val user = Users(uid = null, userPhoneNumber = userNumber, userAddress = " ")

        viewModel.signInWithPhoneAuthCredential(otp, userNumber,user)

        lifecycleScope.launch {
            viewModel.isSignedInSuccessfully.collect {
                if (it) {
                    utils.hideDialog()
                    utils.showToast(requireContext(), "Logged In Successfully")
                    startActivity(Intent(requireActivity(), UsersMainActivity::class.java))
                    requireActivity().finish()
                }

            }
        }
    }
    private fun getOTP() {
        utils.showDialog(requireContext(),"Sending OTP...")
        viewModel.apply {

            sendOTP(userNumber,requireActivity())
            lifecycleScope.launch {
                otpSent.collect{
                    if(it){
                        utils.hideDialog()
                        utils.showToast(requireContext(),"OTP sent...")

                    }
                }
            }
        }
    }
    private fun onBackButtonClicked() {
        binding.tbOtpFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_OTPFragment_to_signInFragment)
        }
    }
    private fun customisingEnteringOtp() {
        val editTexts = arrayOf(binding.etOtp1,binding.etOtp2,binding.etOtp3,binding.etOtp4,binding.etOtp5,binding.etOtp6)
        for(i in editTexts.indices){
            editTexts[i].addTextChangedListener (object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    } else if (s?.length == 0) {
                        if (i > 0) {
                            editTexts[i - 1].requestFocus()
                        }
                    }
                }
            })
        }
    }
    private fun getUserNumber() {
        val bundle = arguments
        userNumber = bundle?.getString("number").toString()

        binding.tvUserNumber.text = userNumber

    }


}