package com.example.android.politicalpreparedness.representative

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentRepresentativeBinding
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.adapter.RepresentativeListAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Suppress("DEPRECATION")
class RepresentativeFragment : Fragment() {

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    private lateinit var binding: FragmentRepresentativeBinding
    private lateinit var representativeAdapter: RepresentativeListAdapter

    private val viewModel: RepresentativeViewModel by lazy {
        val application = requireNotNull(this.activity).application
        val viewModelFactory = RepresentativeViewModel.Factory(application)
        ViewModelProvider(this, viewModelFactory)[RepresentativeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_representative, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.address = Address("", "", "", "", "")

        val states = resources.getStringArray(R.array.states)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, states)
        binding.state.adapter = adapter

        binding.buttonLocation.setOnClickListener {
            checkLocationPermissions()
        }

        binding.buttonSearch.setOnClickListener {
            hideKeyboard()
            viewModel.getRepresentatives()
        }

        representativeAdapter = RepresentativeListAdapter()
        binding.representativesRecyclerView.adapter = representativeAdapter
        binding.representativesRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.representatives.observe(viewLifecycleOwner) { representatives ->
            representativeAdapter.submitList(representatives)
        }
    }

    // Replaced deprecated onRequestPermissionsResult with ActivityResultContracts.RequestPermission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getLocation()
            }
        }

    private fun checkLocationPermissions(): Boolean {
        return if (isPermissionGranted()) {
            getLocation()
            true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            false
        }
    }

    private fun isPermissionGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val locationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        locationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val address = geoCodeLocation(location)
                        viewModel.address.value = address
                        val states = resources.getStringArray(R.array.states)
                        val selectedStateIndex = states.indexOf(address.state)
                        binding.state.setSelection(selectedStateIndex)

                        viewModel.getRepresentatives()
                    }
                }
            }
            .addOnFailureListener { e -> e.printStackTrace() }
    }

    private suspend fun geoCodeLocation(location: Location): Address {
        return withContext(Dispatchers.IO) {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            if (!addressList.isNullOrEmpty()) {
                addressList.map { address ->
                    Address(
                        address.thoroughfare ?: "",
                        address.subThoroughfare ?: "",
                        address.locality ?: "",
                        address.adminArea ?: "",
                        address.postalCode ?: ""
                    )
                }.first()
            } else {
                Address("", "", "", "", "")
            }
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }
}

