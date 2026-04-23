package com.varuntulsiyani.project.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.varuntulsiyani.project.R
import com.varuntulsiyani.project.SessionManager
import com.varuntulsiyani.project.databinding.FragmentProfileBinding
import com.varuntulsiyani.project.viewmodel.UserViewModel
import com.varuntulsiyani.project.viewmodel.SharedViewModel

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = checkNotNull(_binding)
    
    private val userViewModel: UserViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        val currentUser = auth.currentUser
        if (currentUser != null) {
            userViewModel.fetchUser(currentUser.uid)
        }

        binding.apply {
            tvTitle.text = "My Profile"
            btnBack.setOnClickListener { findNavController().navigateUp() }

            userViewModel.user.observe(viewLifecycleOwner) { user ->
                tvUserName.text = user.name
                tvEmail.text = user.email
                
                // Update UI based on stored user settings
                updateSettingsUI(user.selectedCountry, user.selectedLanguage, user.isDarkMode)
            }

            // Account Actions
            layoutOrders.setOnClickListener { findNavController().navigate(R.id.navigation_order_list) }
            layoutCart.setOnClickListener { findNavController().navigate(R.id.navigation_cart) }
            layoutAddress.setOnClickListener { findNavController().navigate(R.id.navigation_shipping_address) }
            layoutPayment.setOnClickListener { findNavController().navigate(R.id.navigation_payment) }

            setupSettings()

            btnLogout.setOnClickListener {
                userViewModel.logout()
                sessionManager.setLoggedIn(false)
                findNavController().navigate(R.id.navigation_login)
            }
        }
    }

    private fun updateSettingsUI(country: String, language: String, isDarkMode: Boolean) {
        binding.apply {
            tvCurrentCountry.text = country
            tvCurrentLanguage.text = language
            if (switchDarkMode.isChecked != isDarkMode) {
                switchDarkMode.isChecked = isDarkMode
            }
        }
    }

    private fun setupSettings() {
        binding.apply {
            // Dark Mode
            switchDarkMode.setOnCheckedChangeListener { view, isChecked ->
                if (view.isPressed) {
                    userViewModel.updateSetting("isDarkMode", isChecked)
                    
                    val targetMode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                    // Only apply if it's different to avoid redundant activity recreations
                    if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
                        AppCompatDelegate.setDefaultNightMode(targetMode)
                    }
                }
            }

            // Language
            btnLanguage.setOnClickListener {
                val languages = arrayOf("English", "Arabic", "French", "Hindi")
                AlertDialog.Builder(requireContext())
                    .setTitle("Select Language")
                    .setItems(languages) { _, which ->
                        val selected = languages[which]
                        userViewModel.updateSetting("selectedLanguage", selected)
                        Toast.makeText(requireContext(), "Language changed to $selected", Toast.LENGTH_SHORT).show()
                        // Force reload activity to apply language if needed, or just let UI update
                    }
                    .show()
            }

            // Country
            btnCountry.setOnClickListener {
                val countries = arrayOf("UAE", "USA", "UK", "India", "Canada")
                AlertDialog.Builder(requireContext())
                    .setTitle("Select Country")
                    .setItems(countries) { _, which ->
                        val selected = countries[which]
                        userViewModel.updateSetting("selectedCountry", selected)
                        sharedViewModel.setCountry(selected)
                        Toast.makeText(requireContext(), "Country changed to $selected", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }

            // Change Password
            btnChangePassword.setOnClickListener {
                val email = auth.currentUser?.email
                if (email != null) {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(requireContext(), "Failed to send reset email", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
