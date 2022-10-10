package com.example.mcard.presentation.adapters

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.example.mcard.R
import com.example.mcard.databinding.SingleHistoryAdapterBinding
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.repository.features.utils.StoriesDiffUtil
import com.example.mcard.repository.models.storage.BarcodeModel.CREATOR.SPLIT_SYMBOLS
import com.example.mcard.repository.models.storage.HistoryEntity
import com.example.mcard.repository.models.storage.HistoryEntity.CREATOR.getBigHistoryAddress
import com.example.mcard.repository.source.usage.UsageDialogFragment


internal class StoriesAdapter(
    private val animationSelect: Animation,
) : RecyclerView.Adapter<StoriesAdapter.StoriesViewHolder>() {

    private lateinit var adapterBinding: SingleHistoryAdapterBinding

    private val dataDiffer: AsyncListDiffer<HistoryEntity> by lazy {
        AsyncListDiffer(
            this, StoriesDiffUtil()
        )
    }

    inner class StoriesViewHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        private val dialogEmptyDataNavigation: UsageDialogFragment? by lazy {
            CustomDialogBuilder(itemView.context)
                .setTitle(R.string.infoGlobalTitle)
                .setMessage(R.string.warningSelectNavigation)
                .setPositiveAction { }
                .build()
        }

        fun drawItem(singleItem: HistoryEntity?) {

            adapterBinding.apply binding@{

                dateUseTextView.text = singleItem?.timeAdd

                if (singleItem?.shopAddress?.contains(SPLIT_SYMBOLS) == true) {
                    getBigHistoryAddress(
                        singleItem.shopAddress
                    ).apply address@{

                        descriptionTextView.text =
                            this?.second

                        Pair(
                            this?.first, singleItem.shopName
                        ).apply {
                            navigationContainer.setOnClickListener {
                                it.startAnimation(animationSelect)
                                addressAction()
                            }

                            loactionDescription.setOnClickListener {
                                it.startAnimation(animationSelect)
                                addressAction()
                            }

                            iconLocation.setOnClickListener {
                                it.startAnimation(animationSelect)
                                addressAction()
                            }
                        }
                    }
                } else {
                    descriptionTextView.setText(
                        R.string.msgErrorGetAddress
                    )

                    navigationContainer.setOnClickListener {
                        it.startAnimation(animationSelect)
                        dialogEmptyDataNavigation?.show()
                    }

                    loactionDescription.setOnClickListener {
                        it.startAnimation(animationSelect)
                        dialogEmptyDataNavigation?.show()
                    }

                    iconLocation.setOnClickListener {
                        it.startAnimation(animationSelect)
                        dialogEmptyDataNavigation?.show()
                    }
                }
            }
        }

        private fun Pair<String?, String?>.addressAction() {
            this.apply {
                adapterBinding.root.context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "http://maps.google.com/maps?q=loc:$first ($second)"
                        )
                    )
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoriesViewHolder {
        this.adapterBinding =
            SingleHistoryAdapterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return StoriesViewHolder(
            this.adapterBinding.root
        )
    }

    override fun onBindViewHolder(holder: StoriesViewHolder, position: Int) =
        holder.drawItem(
            this.dataDiffer.currentList[position]
        )

    infix fun submitList(data: List<HistoryEntity>) {
        dataDiffer.submitList(data)
        notifyDataSetChanged()
    }

    override fun getItemCount() =
        this.dataDiffer.currentList.size
}