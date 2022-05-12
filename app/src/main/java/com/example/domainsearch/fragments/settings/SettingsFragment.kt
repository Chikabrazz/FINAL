package com.example.domainsearch.fragments.settings

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import com.example.domainsearch.App
import com.example.domainsearch.R
import com.example.domainsearch.base.BaseViewModel
import com.example.domainsearch.extensions.addOnPreferenceChangeListener
import com.example.domainsearch.extensions.setOnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val mViewModel by viewModels<BaseViewModel>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference(R.string.key_theme).addOnPreferenceChangeListener {
            val app = requireActivity().application as App
            app.setupTheme(it as String)
        }

        // Set onClickListeners for all buttons
        findPreference(R.string.key_clear_history).apply {
            setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val historyExists = mViewModel.searchDao.getHistoryLength() > 0

                    if (historyExists)
                        mViewModel.searchDao.clearHistory()

                    val toastString =
                        if (historyExists)
                            R.string.history_cleared
                        else
                            R.string.empty_history

                    requireActivity().runOnUiThread {
                        isSelectable = false
                        mViewModel.toast(toastString, Toast.LENGTH_SHORT)
                    }
                }
            }
        }
    }

    private fun findPreference(key: Int) = findPreference<Preference>(mViewModel.getString(key))!!
}
