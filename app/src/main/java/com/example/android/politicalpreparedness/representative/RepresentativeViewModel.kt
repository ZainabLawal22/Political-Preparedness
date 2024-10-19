package com.example.android.politicalpreparedness.representative

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.politicalpreparedness.network.CivicsApi
import com.example.android.politicalpreparedness.network.models.Address
import com.example.android.politicalpreparedness.representative.model.Representative
import kotlinx.coroutines.launch

class RepresentativeViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    // LiveData for list of representatives
    private val _representatives =
        savedStateHandle.getLiveData<List<Representative>>("representatives")
    val representatives: LiveData<List<Representative>> get() = _representatives

    val address = MutableLiveData<Address>()

    // Separate LiveData for each field of the address
    val addressLine1 = MutableLiveData<String>()
    val addressLine2 = MutableLiveData<String>()
    val city = MutableLiveData<String>()
    val state = MutableLiveData<String>()
    val zip = MutableLiveData<String>()

    // Function to get representatives based on the address
    fun getRepresentatives() {
        // Convert LiveData values into an Address object
        val address = Address(
            line1 = addressLine1.value ?: "",
            line2 = addressLine2.value,
            city = city.value ?: "",
            state = state.value ?: "",
            zip = zip.value ?: ""
        )

        viewModelScope.launch {
            try {
                val (offices, officials) = CivicsApi.retrofitService.getRepresentativesAsync(address.toFormattedString())
                    .await()
                val representativeList =
                    offices.flatMap { office -> office.getRepresentatives(officials) }

                // Save representatives to SavedStateHandle
                _representatives.value = representativeList
                savedStateHandle.set("representatives", representativeList)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    // Factory to create the ViewModel with SavedStateHandle
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RepresentativeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RepresentativeViewModel(app, SavedStateHandle()) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel")
        }
    }
}



