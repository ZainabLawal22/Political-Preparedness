package com.example.android.politicalpreparedness.representative

import android.app.Application
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
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

    val addressLine1 = savedStateHandle.getLiveData<String>("addressLine1")
    val addressLine2 = savedStateHandle.getLiveData<String>("addressLine2")
    val city = savedStateHandle.getLiveData<String>("city")
    val state = savedStateHandle.getLiveData<String>("state")
    val zip = savedStateHandle.getLiveData<String>("zip")


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
    class Factory(
        private val app: Application,
        private val savedStateRegistryOwner: SavedStateRegistryOwner
    ) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner, null) {

        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            if (modelClass.isAssignableFrom(RepresentativeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RepresentativeViewModel(app, handle) as T
            }
            throw IllegalArgumentException("Unable to construct ViewModel")
        }
    }

}



