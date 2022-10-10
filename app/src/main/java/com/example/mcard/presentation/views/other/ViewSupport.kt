package com.example.mcard.presentation.views.other

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import com.example.mcard.presentation.controllers.HostActivity
import com.example.mcard.repository.features.*
import com.example.mcard.repository.features.currentScreenPositionIsVertical
import com.example.mcard.repository.features.utils.DesignCardManager.renameCardName
import com.example.mcard.repository.features.optionally.TextChangedListener
import com.example.mcard.repository.features.unitAction
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal fun MaterialToolbar.initViewToolBar(
    menuId: Int? = null,
    vararg buttonActions: Pair<Int, unitAction>,
) =
    MainScope().launch(Dispatchers.Main) {

        menuId?.let {
            inflateMenu(it)
        }

        defaultNavigationOnClickListener()

        setOnMenuItemClickListener listener@{ selectedItem ->
            buttonActions.forEach { singleButtonAction ->
                if (singleButtonAction.first == selectedItem.itemId)
                    singleButtonAction.second.invoke()
            }
            return@listener true
        }
    }


internal fun View.setPaddingByBottomNavBar(
    usePaddingBottom: Boolean = false,
) = doOnLayout {
    (context as? HostActivity)?.let { hostContext ->
        hostContext.requireHeightBottomNavBar { height ->
            try {
                if (!hostContext.currentScreenPositionIsVertical())
                    setPadding(
                        32,
                        8,
                        32,
                        height * 2
                    )
                else
                    setPadding(
                        24,
                        24,
                        24,
                        if (usePaddingBottom) height else 0
                    )
            } catch (ignored: Exception) {
            }
        }
    }
}

internal inline fun EditText.setCardDataChangeListener(
    confirmView: View,
    cardModel: CardWithHistoryEntity,
    noinline additionalAction: textAction? = null,
    crossinline action: dataChangeInfoAction,
) {
    var initData = text.toString()

    confirmView.setOnClickListener {
        text.toString().run {
            if (action.invoke(
                    it.id, cardModel, this
                ) != null
            ) {
                context.renameCardName(
                    initData, this
                )

                initData = this
                additionalAction?.invoke(this)
            } else
                this@setCardDataChangeListener.setText(initData)

            this@setCardDataChangeListener.isCursorVisible = false
            confirmView.visibility = View.INVISIBLE
        }
    }

    setOnClickListener {
        this@setCardDataChangeListener.isCursorVisible = true
    }

    addTextChangedListener(object : TextChangedListener {
        override fun onTextChanged(data: String) {

            confirmView.visibility =
                if (initData != data)
                    View.VISIBLE
                else
                    View.INVISIBLE
        }
    })
}

internal infix fun View.setKeyboardFocus(
    isShow: Boolean,
) =
    (context.getSystemService(
        Context.INPUT_METHOD_SERVICE
    ) as InputMethodManager).run {
        if (isShow) {
            requestFocus()

            showSoftInput(
                this@setKeyboardFocus,
                InputMethodManager.SHOW_IMPLICIT
            )
        } else {
            clearFocus()

            hideSoftInputFromWindow(
                windowToken, 0
            )
        }
    }

internal fun View.setNestedScrollingByOrientation() =
    ViewCompat.setNestedScrollingEnabled(
        this,
        this.context.currentScreenPositionIsVertical()
    )

internal fun MaterialToolbar.defaultNavigationOnClickListener() {
    setNavigationOnClickListener {
        (context as? HostActivity)?.actionBackStackCurrentFragment()
    }
}
