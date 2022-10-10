package com.example.mcard.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mcard.databinding.SheetItemBinding
import com.example.mcard.repository.features.unitAction
import com.example.mcard.repository.models.other.SheetModel

internal class SheetViewAdapter(
     val basicDataList: List<SheetModel>,
    private val cancelAction: unitAction? = null,
) : RecyclerView.Adapter<SheetViewAdapter.SheetViewHolder>(){

    private lateinit var adapterBinding: SheetItemBinding

    inner class SheetViewHolder(
        view: View,
    ) : RecyclerView.ViewHolder(view) {

        fun drawItem(data: SheetModel) {

            adapterBinding.apply {

                if (data.singleChecked)
                    checkableView.visibility = View.VISIBLE

                data.iconId?.apply {
                    iconView.setImageResource(this)
                    iconView.visibility = View.VISIBLE
                }

                dataButton.setText(data.textId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SheetItemBinding.inflate(
            LayoutInflater.from(
                parent.context
            )
        ).run {
            adapterBinding = this
            SheetViewHolder(
                adapterBinding.root
            )
        }

    override fun getItemCount() =
        this.basicDataList.size

    override fun onBindViewHolder(
        holder: SheetViewHolder, position: Int,
    ) =
        this.basicDataList[position].let { model ->
            holder.drawItem(model)

            holder.itemView.setOnClickListener {

                if (model.isCheckable) {
                    basicDataList.forEach {
                        it.singleChecked = false
                    }

                    basicDataList[position].singleChecked = true
                    notifyItemRangeChanged(
                        0, basicDataList.size
                    )
                }

                cancelAction?.invoke()
                model.action.invoke()
            }
        }
}