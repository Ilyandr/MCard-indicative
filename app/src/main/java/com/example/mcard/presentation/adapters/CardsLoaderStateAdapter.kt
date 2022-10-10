package com.example.mcard.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mcard.databinding.CardsItemProgressBinding
import com.example.mcard.repository.features.setVisible

internal class CardsLoaderStateAdapter : LoadStateAdapter<CardsLoaderStateAdapter.LoaderHolder>() {

    private lateinit var adapterBinding: CardsItemProgressBinding

    inner class LoaderHolder(
        parent: ViewGroup,
    ) : RecyclerView.ViewHolder(parent) {

        infix fun bind(loadState: LoadState) {

            adapterBinding.progressBar.run {

                when (loadState) {

                    is LoadState.Error ->
                        this setVisible false

                    is LoadState.Loading ->
                        this setVisible true

                    is LoadState.NotLoading ->
                        this setVisible false
                }
            }
        }
    }

    override fun onBindViewHolder(holder: LoaderHolder, loadState: LoadState) {
        holder bind loadState
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoaderHolder {
        this.adapterBinding =
            CardsItemProgressBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return LoaderHolder(parent)
    }
}