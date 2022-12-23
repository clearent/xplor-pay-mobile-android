package com.xplore.paymobile.ui.merchantselection.search.merchant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xplore.paymobile.data.remote.model.Merchant
import com.xplore.paymobile.ui.merchantselection.search.list.MerchantsListAdapter
import com.xplore.paymobile.util.SharedPreferencesDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MerchantSearchViewModel @Inject constructor(
    private val paginationHelper: MerchantPaginationHelper,
    private val sharedPrefs: SharedPreferencesDataSource
) :
    ViewModel() {

    private val _resultsFlow = MutableStateFlow<List<Merchant>>(listOf())
    val resultsFlow: Flow<List<Merchant>> = _resultsFlow
//    private val _errorFlow = MutableStateFlow<List<??>>(listOf())
//    val errorFlow: StateFlow<List<??>> = _resultsFlow

    init {
        viewModelScope.launch {
            paginationHelper.resultsFlow.collect { merchants ->
                val totalResults = mutableListOf<Merchant>()
                Timber.d("TESTEST collect page ${paginationHelper.currentPage}")
                if (paginationHelper.currentPage >= 2) {
                    //todo improve
                    Timber.d("TESTEST addAll ${_resultsFlow.value.size}")
                    totalResults.addAll(_resultsFlow.value)
                }
                totalResults.addAll(merchants)
                _resultsFlow.emit(totalResults)
            }
        }
    }

    fun searchForQuery(query: String) {
        viewModelScope.launch {
            Timber.d("search for $query")
            paginationHelper.updateQuery(query)
        }
    }

    fun nextPage() {
        paginationHelper.nextPage()
    }

    fun saveMerchant(merchantItem: MerchantsListAdapter.MerchantItem) {
        val merchant =
            _resultsFlow.value.find { it.merchantName == merchantItem.name && it.merchantNumber == merchantItem.id }
        merchant?.let {
            sharedPrefs.setMerchant(merchant)
            sharedPrefs.clearTerminal()
        }
        Timber.d("TESTEST saved merchant ${sharedPrefs.getMerchant()?.merchantName}")
    }

    fun removeTerminal() {
        sharedPrefs.clearTerminal()
    }

    fun currentPage(): Int = paginationHelper.currentPage
}