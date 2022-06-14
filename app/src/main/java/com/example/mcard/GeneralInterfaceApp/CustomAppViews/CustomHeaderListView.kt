package com.example.mcard.GeneralInterfaceApp.CustomAppViews

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ListView
import androidx.appcompat.widget.AppCompatTextView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mcard.AdapersGroup.BasicCardManagerAdapter
import com.example.mcard.AdapersGroup.CardInfoEntity
import com.example.mcard.SideFunctionality.ListViewComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
internal class CustomHeaderListView : ListView
{
    private var headerTextView: AppCompatTextView? = null
    private var listViewComponent: ListViewComponent? = null

    var swipeRefreshLayout: SwipeRefreshLayout? = null
    var completionResponseMap: MutableMap<String?, String?>? = null

    companion object
    { private const val ADDRESS_EMPTY = "\n-\n" }

    constructor(context: Context?)
            : super(context)
    constructor(context: Context?, attrs: AttributeSet?)
            : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(context, attrs, defStyleAttr, defStyleRes)

    fun checkEmptyList(isCardList: Boolean) = GlobalScope.launch(Dispatchers.Main)
    {
        if (this@CustomHeaderListView.count != 0)
            this@CustomHeaderListView.headerTextView?.let {
                super.removeHeaderView(it)
            }
        else
            this@CustomHeaderListView.headerTextView?.let {
                super.addHeaderView(it)
            } ?: run {
                this@CustomHeaderListView.headerTextView =
                    AppCompatTextView(context)
                headerTextView!!.text =
                    if (isCardList) "Список карт пуст"
                    else "Список пуст"
                headerTextView!!.gravity = Gravity.CENTER
                headerTextView!!.setTextColor(
                    Color.parseColor("#ffffff"))
                headerTextView!!.textSize = 15f
                headerTextView!!.typeface = Typeface.SERIF

                scaleX = 1f
                super.addHeaderView(headerTextView, null, false)
            }
    }.start()

    fun setAnimationAction(
        swipeRefreshLayout: SwipeRefreshLayout
        , listViewComponent: ListViewComponent)
    {
        this.swipeRefreshLayout = swipeRefreshLayout
        this.listViewComponent = listViewComponent
    }

    fun offLoadingAnimations(updateList: Boolean) =
        this.swipeRefreshLayout?.let {
            if (updateList)
            {
                this.removeAllViewsInLayout()
                this.scaleX = 0f
                listViewComponent?.createItemInList(this)
            }
            it.isRefreshing = false
        }

    fun getAddressShop(singleCardEntity: CardInfoEntity): String =
        this.completionResponseMap?.let {
            for (singleShopData: (Map.Entry<String?, String?>) in it.entries)
                if (BasicCardManagerAdapter.percentageSimilarityResult(
                        singleCardEntity.name, singleShopData.key) >= 75)
                    return@let singleShopData.value
            ADDRESS_EMPTY
        } ?: ADDRESS_EMPTY
}