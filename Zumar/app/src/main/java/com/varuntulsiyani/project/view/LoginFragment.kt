package com.varuntulsiyani.project.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.varuntulsiyani.project.R
import com.varuntulsiyani.project.SessionManager
import com.varuntulsiyani.project.databinding.FragmentLoginBinding
import com.varuntulsiyani.project.viewmodel.UserViewModel

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = checkNotNull(_binding)
    
    private val userViewModel: UserViewModel by activityViewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        binding.apply {
            btnLogin.setOnClickListener {
                val email = etLoginEmail.text.toString().trim()
                val password = etLoginPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (!isNetworkAvailable()) {
                    showNoInternetDialog()
                    return@setOnClickListener
                }

                userViewModel.login(email, password) { success, error ->
                    if (success) {
                        Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                        sessionManager.setLoggedIn(true)
                        findNavController().navigate(R.id.action_login_to_home)
                    } else {
                        if (error?.contains("network error", ignoreCase = true) == true) {
                            showNoInternetDialog()
                        } else {
                            Toast.makeText(requireContext(), error ?: "Login Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            tvForgotPassword.setOnClickListener {
                showForgotPasswordDialog()
            }

            tvSignUpLink.setOnClickListener {
                findNavController().navigate(R.id.action_login_to_signup)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Reset Password")
        builder.setMessage("Enter your email address to receive a password reset link")

        val input = EditText(requireContext())
        input.hint = "Email"
        builder.setView(input)

        builder.setPositiveButton("Send") { dialog, _ ->
            val email = input.text.toString().trim()
            if (email.isNotEmpty()) {
                if (!isNetworkAvailable()) {
                    showNoInternetDialog()
                } else {
                    // Password reset doesn't strictly need a ViewModel but for consistency...
                    com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Reset email sent.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(requireContext(), task.exception?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }
}
