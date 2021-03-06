package com.example.domainsearch.fragments.search

import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.domainsearch.R
import com.example.domainsearch.annotations.Visibility
import com.example.domainsearch.base.BaseViewModel
import com.example.domainsearch.base.stringMutableLiveData
import com.example.domainsearch.database.SearchQuery
import com.example.domainsearch.di.AppModule
import com.example.domainsearch.extensions.addPrefix
import com.example.domainsearch.extensions.isCorrectAsDomain
import com.example.domainsearch.extensions.removeAll
import com.example.domainsearch.extensions.surroundWith
import com.example.domainsearch.repository.domainrepository.*
import com.example.domainsearch.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * [ViewModel] for [SearchFragment]
 */
@HiltViewModel
class SearchViewModel @Inject constructor(uiActions: UiActions) : BaseViewModel(uiActions) {

    // If livedata true set VISIBLE else INVISIBLE
    val textAboveDomainLiveData = stringMutableLiveData()

    // Text of @+id/domain_text
    val domainTextLiveData = stringMutableLiveData()

    // Specific description of domain
    val descriptionTextLiveData = stringMutableLiveData()

    // If livedata is true set VISIBLE else GONE
    val progressBarVisibilityLiveData = MutableLiveData<@Visibility Int>()

    private var manualSearch: Boolean? = null

    init { clearScreen() }

    /**
     * Gets domain from database or downloads it
     * @param windowToken helps to hide keyboard
     */
    fun onSearch(text: String, windowToken: IBinder?, manualSearch: Boolean) {
        Timber.d("onSearch called")

        this.manualSearch = manualSearch

        if (manualSearch)
            hideKeyboard(windowToken!!)

        val domain: String = text.removeAll(' ').removePrefix(".").lowercase()

        if (domain.isCorrectAsDomain()) {
            progressBarVisibilityLiveData.value = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                val domainRepository = DomainRepository(AppModule.provideUiActions(context)) { result ->
                    setSearchResult(result)
                }

                domainRepository.getDescription(domain)
            }
        } else {
            clearScreen()
            toast(R.string.incorrect_domain, Toast.LENGTH_SHORT)
        }
    }

    /**
     * When repository loads result, it calls this function
     */
    private fun setSearchResult(result: DomainSearchResult) {

        val domain: String
        val description: String?
        val status: Status

        when (result) {

            is Success -> {
                domain = result.domain
                description = result.description
                status = result.status
            }

            is Failure -> {
                domain = result.domain
                description = null
                status = Status.FAILURE
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val id = searchDao.findIdByDomain(domain)
            tryToSaveQueryToHistoryDatabase(status, id!!)
        }

        clearScreen()

        when (status) {
            Status.FOUND -> onDomainExists(domain, description!!)
            Status.NOT_FOUND -> onDomainDoesntExist(domain)
            Status.FAILURE -> onFailure((result as Failure).connectionError, result.errorMessage)
        }
    }

    /** If [manualSearch] equals true and history is enabled, saves query to history */
    private suspend fun tryToSaveQueryToHistoryDatabase(status: Status, id: Int) {
        val historyEnabled = sharedPrefs.getBoolean(getString(R.string.key_history), true)

        if (manualSearch!! && historyEnabled) {
            searchDao.insertQuery(SearchQuery(0, id, status))
            Timber.d("query saved to history")
        }
    }

    /**
     * Sets all textViews to "" and deletes progress bar from screen
     */
    fun clearScreen() {
        textAboveDomainLiveData.postValue("")
        domainTextLiveData.postValue("")
        descriptionTextLiveData.postValue("")
        progressBarVisibilityLiveData.postValue(View.GONE)
    }

    /**
     * Updates [domainTextLiveData] and [descriptionTextLiveData]
     */
    private fun onDomainExists(domain: String, description: String) {
        Timber.d("onDomainExists called")
        domainTextLiveData.postValue(getString(R.string.domain_exists, domain))
        descriptionTextLiveData.postValue(description)
    }

    /**
     * Updates [textAboveDomainLiveData], [domainTextLiveData] and [descriptionTextLiveData]
     */
    private fun onDomainDoesntExist(domain: String) {
        Timber.d("onDomainDoesntExists called")
        textAboveDomainLiveData.postValue(getString(R.string.domain_doesnt_exist))
        domainTextLiveData.postValue(domain.addPrefix(".").surroundWith('"'))
        descriptionTextLiveData.postValue(getString(R.string.try_another_domain))
    }

    /**
     * Updates [domainTextLiveData] and [descriptionTextLiveData] with errors
     */
    private fun onFailure(connectionError: Boolean, errorMessage: String?) {
        Timber.d("onFailure called")

        domainTextLiveData.postValue(getString(
            if (connectionError)
                R.string.cannot_connect_to_internet
            else
                R.string.loading_error
        ))

        errorMessage?.let {
            descriptionTextLiveData.postValue(it)
        }
    }
}
