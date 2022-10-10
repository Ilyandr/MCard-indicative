package com.example.mcard.presentation.adapters.options

import android.annotation.SuppressLint
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amyu.stack_card_layout_manager.StackCardLayoutManager
import com.example.mcard.repository.features.currentScreenPositionIsVertical
import com.mig35.carousellayoutmanager.CarouselLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal inline fun <reified T : RecyclerView.Adapter<*>> RecyclerView.recyclerCardViewOptions(
    adapter: T, useCarousel: Boolean?,
): T = adapter.apply {

    layoutManager =
        context.currentScreenPositionIsVertical().run {

            if (useCarousel == null)
                LinearLayoutManager(
                    context, LinearLayoutManager.HORIZONTAL, false
                )
            else if (useCarousel) {
                if (this)
                    StackCardLayoutManager(200)
                else
                    CarouselLayoutManager(
                        CarouselLayoutManager.HORIZONTAL, false
                    )
            } else
                GridLayoutManager(
                    context,
                    2,
                    if (this)
                        GridLayoutManager.VERTICAL
                    else
                        GridLayoutManager.HORIZONTAL,
                    false
                ).apply {
                    addItemDecoration(
                        ItemOffsetDecoration(12)
                    )
                }
        }

    this@recyclerCardViewOptions.adapter = adapter
    setHasFixedSize(true)
}


@SuppressLint("NotifyDataSetChanged")
internal fun RecyclerView.configRecyclerCardViewChanged(
    useCarousel: Boolean,
    scrollPosition: Int = 0,
) {
    CoroutineScope(Dispatchers.Main).launch {

        layoutManager =
            context.currentScreenPositionIsVertical().run {
                if (useCarousel) {
                    if (this)
                        StackCardLayoutManager(200)
                    else
                        CarouselLayoutManager(
                            CarouselLayoutManager.HORIZONTAL, false
                        ).apply {
                            scrollToPosition(scrollPosition)
                        }
                } else
                    GridLayoutManager(
                        context,
                        2,
                        if (this)
                            GridLayoutManager.VERTICAL
                        else
                            GridLayoutManager.HORIZONTAL,
                        false
                    )
            }

        recycledViewPool.clear()
        swapAdapter(
            adapter, false
        )
        setHasFixedSize(true)
    }
}