package com.example.mcard.presentation.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mcard.databinding.SingleAdapterCardBinding
import com.example.mcard.repository.features.currentScreenPositionIsVertical
import com.example.mcard.repository.features.dataCardAction
import com.example.mcard.repository.features.setVisible
import com.example.mcard.repository.features.utils.CardsDiffUtil
import com.example.mcard.repository.features.utils.DesignCardManager
import com.example.mcard.repository.features.utils.DesignCardManager.staticLoadCardImage
import com.example.mcard.repository.features.utils.DesignCardManager.whiteOrBlackColor
import com.example.mcard.repository.models.storage.CardEntity

internal class GlobalCardsAdapter(
    private val itemClickAction: dataCardAction,
) :
    PagingDataAdapter<CardEntity, GlobalCardsAdapter.GlobalCardsHolder>(
        CardsDiffUtil.cardsDiffCallback
    ) {

    private lateinit var adapterBinding: SingleAdapterCardBinding

    inner class GlobalCardsHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        fun drawItem(singleItem: CardEntity) =
            adapterBinding.run {

                DesignCardManager.setCardSize(
                    relativeContainer,
                    true,
                    percentPruning =
                    if (root.context.currentScreenPositionIsVertical())
                        1f
                    else
                        0.5f
                )

                cardDesign.staticLoadCardImage(
                    singleItem,
                    false,
                    additionalSuccessAction = {
                        cardName setVisible false
                    }
                ) {
                    cardName.run {
                        text = singleItem.name
                        gravity = Gravity.CENTER

                        setTextColor(
                            singleItem.color!!.whiteOrBlackColor()
                        )
                    }
                }
            }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): GlobalCardsHolder {
        this.adapterBinding =
            SingleAdapterCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return GlobalCardsHolder(
            this.adapterBinding.root
        )
    }

    override fun onBindViewHolder(
        holder: GlobalCardsHolder, position: Int,
    ) {
        getItem(position)?.apply {
            holder.drawItem(this)

            holder.itemView.setOnClickListener {
                itemClickAction.invoke(this)
            }
        }
    }
}