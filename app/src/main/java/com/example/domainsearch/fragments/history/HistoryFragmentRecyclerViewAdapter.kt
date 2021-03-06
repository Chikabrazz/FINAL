package com.example.domainsearch.fragments.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.domainsearch.databinding.FragmentHistoryItemBinding
import com.example.domainsearch.util.UiActions

/**
 * Recycler view adapter for [HistoryFragment]
 */
class HistoryItemRecyclerViewAdapter(
    uiActions: UiActions,
    private val values: List<HistoryLoader.RecyclerViewItem>,
) : RecyclerView.Adapter<HistoryItemRecyclerViewAdapter.ViewHolder>(), UiActions by uiActions {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            FragmentHistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        with (holder) {
            idView.text = item.id
            domainView.text = item.domain
            descriptionView.text = item.description
        }
    }

    override fun getItemCount() = values.size

    inner class ViewHolder(binding: FragmentHistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView = binding.itemNumber
        val domainView = binding.domain
        val descriptionView = binding.description
    }
}
