package com.example.userblinkitclone.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.userblinkitclone.R
import com.example.userblinkitclone.activity.AuthMainActivity
import com.example.userblinkitclone.databinding.AddressBookLayoutBinding
import com.example.userblinkitclone.databinding.FragmentProfileBinding
import com.example.userblinkitclone.utils
import com.example.userblinkitclone.viewModels.UserViewModel

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        onBackButtonClicked()
        onOrdersLayoutClicked()
        onAddressBookClicked()
        onLogoutClicked()

        return binding.root
    }

    private fun onLogoutClicked() {
        binding.llLogOut.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            val alertDialog = builder.create()
                builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes"){_,_->
                    viewModel.logOutUser()
                    startActivity(Intent(requireContext(), AuthMainActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("No"){_,_->
                    alertDialog.dismiss()
                }
                    .show()
                    .setCancelable(false)
        }
    }

    private fun onAddressBookClicked() {
        binding.llAddressBook.setOnClickListener {
            val addressBookLayoutBinding = AddressBookLayoutBinding.inflate(LayoutInflater.from(requireContext()))
            viewModel.getUserAddress {address->
                addressBookLayoutBinding.etAddress.setText(address.toString())
            }
            val alertDialoge = AlertDialog.Builder(requireContext())
                .setView(addressBookLayoutBinding.root)
                .create()
            alertDialoge.show()

            addressBookLayoutBinding.btnEdit.setOnClickListener {
                addressBookLayoutBinding.etAddress.isEnabled = true
            }
            addressBookLayoutBinding.btnSave.setOnClickListener {
                viewModel.saveAddress(addressBookLayoutBinding.etAddress.text.toString())
                alertDialoge.dismiss()
                utils.showToast(requireContext(),"Address Saved..")
            }

        }
    }



    private fun onOrdersLayoutClicked() {
        binding.llOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_ordersFragment)
        }
    }

    private fun onBackButtonClicked() {
        binding.tbProfileFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }
    }
}
