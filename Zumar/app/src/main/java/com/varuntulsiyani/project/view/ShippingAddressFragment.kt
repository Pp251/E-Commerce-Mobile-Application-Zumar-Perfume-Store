package com.varuntulsiyani.project.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.varuntulsiyani.project.adapter.AddressAdapter
import com.varuntulsiyani.project.databinding.FragmentShippingAddressBinding
import com.varuntulsiyani.project.model.Address
import com.varuntulsiyani.project.viewmodel.UserViewModel
import java.util.Locale

class ShippingAddressFragment : Fragment() {
    private var _binding: FragmentShippingAddressBinding? = null
    private val binding get() = checkNotNull(_binding)
    
    private val userViewModel: UserViewModel by activityViewModels()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: AddressAdapter
    private var editingAddress: Address? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) getCurrentLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShippingAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        
        val userId = auth.currentUser?.uid
        if (userId != null) {
            userViewModel.fetchAddresses(userId)
            userViewModel.addresses.observe(viewLifecycleOwner) { addresses ->
                adapter.submitList(addresses)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AddressAdapter(
            onEdit = { address -> showAddressForm(address) },
            onDelete = { address -> 
                val userId = auth.currentUser?.uid
                if (userId != null) userViewModel.deleteAddress(userId, address.id)
            },
            onSelect = { address ->
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val updatedAddress = address.copy(isDefault = true)
                    userViewModel.saveAddress(userId, updatedAddress)
                }
            }
        )
        binding.rvAddresses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAddresses.adapter = adapter
    }

    private fun setupListeners() {
        binding.apply {
            tvTitle.text = "Shipping Addresses"
            btnBack.setOnClickListener { findNavController().navigateUp() }

            btnAddNewAddress.setOnClickListener { showAddressForm(null) }
            btnCancel.setOnClickListener { hideAddressForm() }
            btnGetCurrentLocation.setOnClickListener { checkLocationPermission() }

            btnSaveAddress.setOnClickListener {
                saveCurrentAddress()
            }
        }
    }

    private fun showAddressForm(address: Address?) {
        editingAddress = address
        binding.apply {
            addressCard.visibility = View.GONE
            cvAddressForm.visibility = View.VISIBLE
            tvFormTitle.text = if (address == null) "New Address" else "Edit Address"
            etFullName.setText(address?.fullName ?: "")
            etPhone.setText(address?.phoneNumber ?: "")
            etAddress.setText(address?.streetAddress ?: "")
            etCity.setText(address?.city ?: "")
            etZip.setText(address?.zipCode ?: "")
        }
    }

    private fun hideAddressForm() {
        editingAddress = null
        binding.addressCard.visibility = View.VISIBLE
        binding.cvAddressForm.visibility = View.GONE
    }

    private fun saveCurrentAddress() {
        val userId = auth.currentUser?.uid ?: return
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val street = binding.etAddress.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val zip = binding.etZip.text.toString().trim()

        if (fullName.isEmpty() || phone.isEmpty() || street.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val address = Address(
            id = editingAddress?.id ?: "",
            fullName = fullName,
            phoneNumber = phone,
            streetAddress = street,
            city = city,
            zipCode = zip,
            isDefault = editingAddress?.isDefault ?: false
        )

        userViewModel.saveAddress(userId, address)
        hideAddressForm()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        binding.etAddress.setText(address.getAddressLine(0))
                        binding.etCity.setText(address.locality)
                        binding.etZip.setText(address.postalCode)
                    }
                }
            }
        } catch (e: SecurityException) { e.printStackTrace() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
