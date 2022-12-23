package com.xplore.paymobile.ui.merchantselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.datasource.NetworkResource
import com.xplore.paymobile.data.datasource.RemoteDataSource
import com.xplore.paymobile.data.remote.model.Merchant
import com.xplore.paymobile.data.remote.model.Terminal
import com.xplore.paymobile.data.remote.model.TerminalsResponse
import com.xplore.paymobile.util.SharedPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MerchantSelectViewModel @Inject constructor(
    private val sharedPrefs: SharedPreferencesDataSource,
    private val remoteDataSource: RemoteDataSource
) : ViewModel() {

    private val _merchantFlow = MutableStateFlow<Merchant?>(null)
    val merchantFlow: Flow<Merchant?> = _merchantFlow

    private val _selectedTerminalFlow = MutableStateFlow<Terminal?>(null)
    val selectedTerminalFlow: Flow<Terminal?> = _selectedTerminalFlow

    private val _terminalsFlow = MutableStateFlow<List<Terminal>>(emptyList())
    val terminalsFlow: Flow<List<Terminal>> = _terminalsFlow

    private val _loadingFlow = MutableStateFlow(true)
    val loadingFlow: Flow<Boolean> = _loadingFlow

    fun fetchMerchantAndTerminal() {
        viewModelScope.launch {
            _loadingFlow.emit(true)
            sharedPrefs.getMerchant()?.also { merchant ->
                Timber.d("TESTEST got merchant $merchant")
                _merchantFlow.emit(merchant)
                Timber.d("TESTEST get terminal")
                sharedPrefs.getTerminal()?.also { terminal ->
                    Timber.d("TESTEST got terminal $terminal")
                    _selectedTerminalFlow.emit(terminal)
                } ?: run {
                    _selectedTerminalFlow.emit(null)
                }
                withContext(Dispatchers.IO) {
                    fetchTerminals(merchant.merchantNumber)
                    _loadingFlow.emit(false)
                }
            } ?: run {
                _loadingFlow.emit(false)
            }
        }
    }

    private suspend fun fetchTerminals(merchantId: String) {
        val networkResponse = remoteDataSource.fetchTerminals(merchantId)
        Timber.d("TESTEST fetch terminals $networkResponse")
        if (networkResponse is NetworkResource.Success) {
            val terminals = networkResponse.data as TerminalsResponse
            Timber.d("TESTEST emit terminals $terminals")
            _terminalsFlow.emit(terminals)
        } else {
            //TODO improvements
            _terminalsFlow.emit(emptyList())
        }
    }

    fun getMerchant() = sharedPrefs.getMerchant()

    fun getTerminal() = sharedPrefs.getTerminal()
}