package com.example.mcard.presentation.adapters.options

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


internal class ItemOffsetDecoration(
    private val mItemOffset: Int,
) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        super.getItemOffsets(
            outRect, view, parent, state
        )

        outRect.set(
            mItemOffset, mItemOffset, mItemOffset, mItemOffset
        )
    }
}