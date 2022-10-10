package com.example.mcard.presentation.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mcard.databinding.SingleAdapterCardBinding
import com.example.mcard.repository.features.dataAdapterAction
import com.example.mcard.repository.features.setVisible
import com.example.mcard.repository.features.utils.CardsDiffUtil
import com.example.mcard.repository.features.utils.DesignCardManager
import com.example.mcard.repository.features.utils.DesignCardManager.staticLoadCardImage
import com.example.mcard.repository.features.utils.DesignCardManager.whiteOrBlackColor
import com.example.mcard.repository.models.storage.CardEntity
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal class LocaleCardsAdapter(
    private val itemClickAction: dataAdapterAction,
) : RecyclerView.Adapter<LocaleCardsAdapter.LocaleCardsHolder>() {

    private lateinit var adapterBinding: SingleAdapterCardBinding

    private val basicDataList: MutableList<CardWithHistoryEntity> by lazy {
        mutableListOf()
    }


    inner class LocaleCardsHolder(
        itemView: View,
    ) : RecyclerView.ViewHolder(itemView) {

        fun drawItem(singleItem: CardEntity) =
            adapterBinding.run {
                DesignCardManager.setCardSize(
                    relativeContainer
                )

                cardDesign.staticLoadCardImage(
                    singleItem, true, {
                        cardName setVisible false
                    }
                ) {
                    cardName.run {
                        text = singleItem.name
                        gravity = Gravity.CENTER_HORIZONTAL

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
    ): LocaleCardsHolder {
        this.adapterBinding =
            SingleAdapterCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return LocaleCardsHolder(
            this.adapterBinding.root
        )
    }

    override fun getItemCount() =
        this.basicDataList.size

    override fun onBindViewHolder(
        holder: LocaleCardsHolder, position: Int,
    ) {
        basicDataList[position].apply {
            holder.drawItem(this.cardEntity)

            holder.itemView.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    itemClickAction.invoke(position, this@apply)
                }
            }
        }
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    infix fun setСomposedData(
        newData: List<CardWithHistoryEntity>,
    ) =
        DiffUtil.calculateDiff(
            CardsDiffUtil(
                basicDataList, newData
            )
        ).run {

            basicDataList.clear()
            basicDataList.addAll(newData)
            dispatchUpdatesTo(this@LocaleCardsAdapter)
            return@run newData
        }

    fun removeAllData() {
        this.basicDataList.clear()
        notifyDataSetChanged()
    }
}