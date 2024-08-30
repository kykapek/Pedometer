package ru.glack.pedometer.ui.database

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.glack.pedometer.R
import ru.glack.pedometer.core.IPValidationUtil
import ru.glack.pedometer.databinding.FragmentDatabaseBinding

class DatabaseFragment : Fragment() {

    private lateinit var viewModel: DatabaseViewModel

    private lateinit var binding: FragmentDatabaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDatabaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = DatabaseViewModel(requireActivity().application)
        setUpSenderObserver()
        lifecycleScope.launch {
            viewModel.ipAddress.collect { ip ->
                binding.etIpAddress.setText(ip)
            }
        }

        lifecycleScope.launch {
            viewModel.port.collect { port ->
                binding.etPort.setText(port)
            }
        }

        lifecycleScope.launch {
            viewModel.username.collect { username ->
                binding.etUsername.setText(username)
            }
        }

        lifecycleScope.launch {
            viewModel.password.collect { password ->
                binding.etPassword.setText(password)
            }
        }
        binding.tvSave.setOnClickListener {
            val ip = binding.etIpAddress.text.toString()
            val port = binding.etPort.text.toString()
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if (validateInputs(ip, port)) {
                viewModel.savePreferences(ip, port, username, password)
                Toast.makeText(requireContext(), "Данные сохранены", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnSubmit.setOnClickListener {
            viewModel.sendDataToServer(
                ip = binding.etIpAddress.text.toString(),
                port = binding.etPort.text.toString(),
                user = binding.etUsername.text.toString(),
                password = binding.etPassword.text.toString()
            )
        }
    }

    private fun validateInputs(ip: String, port: String): Boolean {
        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)) {
            Toast.makeText(requireContext(), "IP-адрес и порт не могут быть пустыми", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!IPValidationUtil.isValidIPAddress(ip)) {
            Toast.makeText(requireContext(), "Неверный формат IP-адреса", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun setUpSenderObserver() {
        lifecycleScope.launch {
            viewModel.sendDataState.collect {
                when (it) {
                    is SendDataState.None -> {
                        binding.btnSubmit.isEnabled = true
                    }
                    is SendDataState.Loading -> {
                        binding.btnSubmit.isEnabled = false
                    }
                    is SendDataState.Success -> {
                        Toast.makeText(requireContext(), "Данные отправлены", Toast.LENGTH_SHORT).show()
                        binding.btnSubmit.isEnabled = true
                    }
                    is SendDataState.Error -> {
                        Toast.makeText(requireContext(), it.errorMessage, Toast.LENGTH_SHORT).show()
                        binding.btnSubmit.isEnabled = true
                    }
                }
            }
        }
    }
}