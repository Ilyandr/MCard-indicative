@file:Suppress("DEPRECATION")

package com.example.mcard.presentation.views.observers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

internal class ObservableList<T>(
    val mutableList: MutableList<T>,
) : MutableList<T> by mutableList, Observable() {

    private lateinit var actionChange: (newListData: MutableList<T>) -> Unit
    private lateinit var locationChange: (newListData: MutableList<T>) -> Unit

    override infix fun add(element: T): Boolean {
        if (mutableList.contains(element))
            mutableList.indexOf(element).apply {
                mutableList[this] = element
            }
        else
            mutableList.add(element).run {
                notifyData(true)
            }

        return true
    }

    override infix fun addAll(elements: Collection<T>) = run {
        mutableList.clear()
        if (mutableList.addAll(elements)) {
            this notifyData true
            true
        } else false
    }

    override fun set(index: Int, element: T): T =
        mutableList.set(
            index, element
        ).apply {
            this@ObservableList notifyData true
        }

    infix fun notifyData(data: List<T>) =
        MainScope().launch(Dispatchers.Main) {
            if (::actionChange.isInitialized)
                this@ObservableList
                    .actionChange
                    .invoke(
                        data.toMutableList()
                    )
        }

    infix fun notifyData(updateObserver: Boolean) {
        if (updateObserver) {
            setChanged()
            notifyObservers()
        }

        MainScope().launch(Dispatchers.Main) {
            if (::actionChange.isInitialized)
                this@ObservableList
                    .actionChange
                    .invoke(this@ObservableList)
        }
    }

    infix fun notifyByLocation(data: MutableList<T>) {
        if (::locationChange.isInitialized)
            CoroutineScope(Dispatchers.Main).launch {
                locationChange.invoke(data)
            }
    }

    infix fun setLocationChange(
        locationChange: (newListData: MutableList<T>) -> Unit,
    ) {
        this.locationChange = locationChange
    }

    infix fun setActionChange(
        actionChange: (newListData: MutableList<T>) -> Unit,
    ) {
        this.actionChange = actionChange
    }
}