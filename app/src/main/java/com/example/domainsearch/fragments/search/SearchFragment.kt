package com.example.domainsearch.fragments.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.example.domainsearch.R
import com.example.domainsearch.base.BaseFragment
import com.example.domainsearch.databinding.FragmentSearchBinding
import com.example.domainsearch.extensions.notNull
import timber.log.Timber

@AndroidEntryPoint
class SearchFragment : BaseFragment(R.layout.fragment_search) {

    override var _binding: ViewBinding? = null
    private val binding get() = _binding as FragmentSearchBinding

    override val mViewModel by viewModels<SearchViewModel>()

    /** Last query in search view in SearchFragment */
    private var searchViewQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        with (binding) {
            viewModel = mViewModel
            lifecycleOwner = viewLifecycleOwner

            // Hides keyboard on click outside it and clears focuses of editTexts
            searchFragmentLayout.setOnClickListener {
                searchView.clearFocus()
                mViewModel.hideKeyboard(root.windowToken)
            }

            if (searchViewQuery.isNotBlank()) {
                searchView.setQuery(searchViewQuery, false)
                mViewModel.onSearch(searchViewQuery, root.windowToken, false)
            }

            // Search button on keyboard
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    onManualSearch()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    searchViewQuery = newText.notNull()
                    mViewModel.clearScreen()
                    return true
                }
            })

            // Search icon near search box
            searchView.setOnClickListener {
                Timber.d("Search icon clicked")
                onManualSearch()
            }
        }

        return binding.root
    }

    private fun onManualSearch() {
        onClearFocusesAndHideKeyboard()
        mViewModel.onSearch(searchViewQuery, binding.root.windowToken, true)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
