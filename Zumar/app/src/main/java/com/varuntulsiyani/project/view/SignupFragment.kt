package com.varuntulsiyani.project.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.varuntulsiyani.project.R
import com.varuntulsiyani.project.SessionManager
import com.varuntulsiyani.project.databinding.FragmentSignupBinding
import com.varuntulsiyani.project.model.User
import com.varuntulsiyani.project.viewmodel.UserViewModel
import java.util.regex.Pattern

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = checkNotNull(_binding)
    
    private lateinit var auth: FirebaseAuth
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        sessionManager = SessionManager(requireContext())

        binding.apply {
            etPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkPasswordStrength(s.toString(), tvPasswordStrength)
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            btnSignup.setOnClickListener {
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (phone.length != 9 || !phone.matches(Regex("\\d+"))) {
                    Toast.makeText(requireContext(), "Phone number must be exactly 9 digits", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!isPasswordStrong(password)) {
                    Toast.makeText(requireContext(), "Password is not strong enough!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                if (password != confirmPassword) {
                    Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!isNetworkAvailable()) {
                    showNoInternetDialog()
                    return@setOnClickListener
                }

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser!!.uid
                            val newUser = User(
                                name = name,
                                phone = "+971$phone",
                                email = email
                            )

                            userViewModel.addUser(userId, newUser) { success ->
                                if (success) {
                                    Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_LONG).show()
                                    sessionManager.setLoggedIn(true)
                                    findNavController().navigate(R.id.action_signup_to_home)
                                } else {
                                    Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            val errorMessage = task.exception?.message ?: "Registration failed"
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            tvLoginHere.setOnClickListener {
                findNavController().navigate(R.id.action_signup_to_login)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    private fun showNoInternetDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_no_internet, null)
        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogView.findViewById<Button>(R.id.btnDialogOk).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun checkPasswordStrength(password: String, textView: TextView) {
        when {
            password.isEmpty() -> {
                textView.text = "Password must be at least 8 characters with uppercase, lowercase, number & special character"
                textView.setTextColor(resources.getColor(R.color.burnt_umber_light, null))
            }
            password.length < 8 -> {
                textView.text = "❌ Too short (minimum 8 characters)"
                textView.setTextColor(resources.getColor(R.color.burnt_umber_light, null))
            }
            else -> {
                val hasUppercase = password.matches(Regex(".*[A-Z].*"))
                val hasLowercase = password.matches(Regex(".*[a-z].*"))
                val hasDigit = password.matches(Regex(".*\\d.*"))
                val hasSpecialChar = password.matches(Regex(".*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))

                val strength = when {
                    hasUppercase && hasLowercase && hasDigit && hasSpecialChar -> {
                        textView.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                        "✅ Strong password"
                    }
                    (hasUppercase || hasLowercase) && hasDigit && hasSpecialChar -> {
                        textView.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
                        "⚠️ Medium password - Add uppercase & lowercase"
                    }
                    else -> {
                        textView.setTextColor(resources.getColor(android.R.color.holo_red_dark, null))
                        "❌ Weak password - Use uppercase, lowercase, number & special character"
                    }
                }
                textView.text = strength
            }
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$"
        return Pattern.compile(passwordRegex).matcher(password).matches()
    }
}
