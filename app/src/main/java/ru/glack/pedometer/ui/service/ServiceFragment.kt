package ru.glack.pedometer.ui.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import ru.glack.pedometer.R
import ru.glack.pedometer.databinding.FragmentServiceBinding
import ru.glack.pedometer.service.LocationStepService

class ServiceFragment : Fragment() {

    private var _binding: FragmentServiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateServiceStatus()

        binding.toggleServiceButton.setOnClickListener {
            toggleService()
        }
    }

    private fun toggleService() {
        if (isServiceRunning(LocationStepService::class.java)) {
            stopLocationStepService()
        } else {
            startLocationStepService()
        }
    }

    private fun startLocationStepService() {
        val serviceIntent = Intent(requireContext(), LocationStepService::class.java)
        ContextCompat.startForegroundService(requireContext(), serviceIntent)
        updateServiceStatus()
    }

    private fun stopLocationStepService() {
        val serviceIntent = Intent(requireContext(), LocationStepService::class.java)
        requireContext().stopService(serviceIntent)
        updateServiceStatus()
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun updateServiceStatus() {
        val serviceRunning = isServiceRunning(LocationStepService::class.java)
        if (serviceRunning) {
            binding.serviceStatusTextView.text = getString(R.string.service_running)
            binding.toggleServiceButton.text = getString(R.string.stop_service)
        } else {
            binding.serviceStatusTextView.text = getString(R.string.service_not_running)
            binding.toggleServiceButton.text = getString(R.string.start_service)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}