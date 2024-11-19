package com.example.android.politicalpreparedness.representative

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import timber.log.Timber

// Extension for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class RepresentativeViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    companion object {
        // Keys for SavedStateHandle and DataStore
        const val STATE_KEY_ADDRESS_LINE_1 = "address.line1.key"
        const val STATE_KEY_ADDRESS_LINE_2 = "address.line2.key"
        const val STATE_KEY_CITY = "city.key"
        const val STATE_KEY_STATE = "state.key"
        const val STATE_KEY_ZIP = "zip.key"

        val PREF_KEY_ADDRESS_LINE_1 = stringPreferencesKey(STATE_KEY_ADDRESS_LINE_1)
        val PREF_KEY_ADDRESS_LINE_2 = stringPreferencesKey(STATE_KEY_ADDRESS_LINE_2)
        val PREF_KEY_CITY = stringPreferencesKey(STATE_KEY_CITY)
        val PREF_KEY_STATE = stringPreferencesKey(STATE_KEY_STATE)
        val PREF_KEY_ZIP = stringPreferencesKey(STATE_KEY_ZIP)
    }

    private val dataStore: DataStore<Preferences> = application.dataStore

    // LiveData for representatives
    private val _representatives =
        savedStateHandle.getLiveData<List<Representative>>("representatives")
    val representatives: LiveData<List<Representative>> get() = _representatives

    // LiveData for address fields
    val addressLine1 = savedStateHandle.getLiveData<String>(STATE_KEY_ADDRESS_LINE_1)
    val addressLine2 = savedStateHandle.getLiveData<String>(STATE_KEY_ADDRESS_LINE_2)
    val city = savedStateHandle.getLiveData<String>(STATE_KEY_CITY)
    val state = savedStateHandle.getLiveData<String>(STATE_KEY_STATE)
    val zip = savedStateHandle.getLiveData<String>(STATE_KEY_ZIP)

    // MutableLiveData for full address
    private val _address = MutableLiveData<Address>()
    val address: LiveData<Address> get() = _address

    init {
        // Initialize address fields from DataStore
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                addressLine1.value = preferences[PREF_KEY_ADDRESS_LINE_1] ?: ""
                addressLine2.value = preferences[PREF_KEY_ADDRESS_LINE_2] ?: ""
                city.value = preferences[PREF_KEY_CITY] ?: ""
                state.value = preferences[PREF_KEY_STATE] ?: ""
                zip.value = preferences[PREF_KEY_ZIP] ?: ""
            }
        }
    }

    private fun saveToDataStore(key: Preferences.Key<String>, value: String) {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[key] = value
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to save $key to DataStore")
            }
        }
    }

    private fun saveAddressToDataStore(address: Address) {
        viewModelScope.launch {
            try {
                dataStore.edit { preferences ->
                    preferences[PREF_KEY_ADDRESS_LINE_1] = address.line1 ?: ""
                    preferences[PREF_KEY_ADDRESS_LINE_2] = address.line2 ?: ""
                    preferences[PREF_KEY_CITY] = address.city ?: ""
                    preferences[PREF_KEY_STATE] = address.state ?: ""
                    preferences[PREF_KEY_ZIP] = address.zip ?: ""
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to save address to DataStore")
            }
        }
    }

    fun updateAddress(newAddress: Address) {
        _address.value = newAddress

        addressLine1.value = newAddress.line1 ?: ""
        addressLine2.value = newAddress.line2 ?: ""
        city.value = newAddress.city ?: ""
        state.value = newAddress.state ?: ""
        zip.value = newAddress.zip ?: ""

        saveAddressToDataStore(newAddress)
        Timber.tag("RepresentativeFragment").d("Updated address: %s", newAddress)
    }

    fun getRepresentatives() {
        val currentAddress = _address.value ?: Address(
            line1 = addressLine1.value ?: "",
            line2 = addressLine2.value,
            city = city.value ?: "",
            state = state.value ?: "",
            zip = zip.value ?: ""
        )

        viewModelScope.launch {
            try {
                val (offices, officials) = CivicsApi.retrofitService.getRepresentativesAsync(
                    currentAddress.toFormattedString()
                )
                    .await()
                val representativeList =
                    offices.flatMap { office -> office.getRepresentatives(officials) }

                _representatives.value = representativeList
                savedStateHandle["representatives"] = representativeList
            } catch (e: Throwable) {
                Timber.e(e, "Failed to fetch representatives")
            }
        }
    }

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





