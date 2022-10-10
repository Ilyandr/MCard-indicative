package com.example.mcard.repository.features

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.navOptions
import com.example.mcard.BasicApplication
import com.example.mcard.R
import com.example.mcard.presentation.controllers.HostActivity
import com.example.mcard.repository.features.rest.firebase.PersonalFirebase
import com.example.mcard.repository.models.other.DataAccountEntity
import com.example.mcard.repository.models.other.SpannableData
import com.example.mcard.repository.models.storage.BarcodeModel
import com.example.mcard.repository.models.storage.CardEntity
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ScheduledExecutorService


internal typealias unitAction =
            () -> Unit

internal typealias changeDataAction =
            (oldData: String?, newData: String?) -> Unit

internal typealias dataPaymentAction =
            (price: Double) -> Unit

internal typealias dataConfirmPaymentAction =
            (dialog: BottomSheetDialog, price: Double) -> Unit

internal typealias messageAction =
            (messageId: Int) -> Unit

internal typealias userDataAction =
            (data: DataAccountEntity) -> Unit

internal typealias textAction =
            (data: String) -> Unit

internal typealias booleanAction =
            (data: Boolean) -> Unit

internal typealias dataTextProcessorAction =
            (dataId: Short, data: String) -> Unit

internal typealias dataBarcodeAction =
            (data: BarcodeModel) -> Unit

internal typealias fullDataCardAction =
            (data: CardWithHistoryEntity?) -> Unit

internal typealias dataCardAction =
            (data: CardEntity) -> Unit

internal typealias dataAdapterAction =
            (position: Int, data: CardWithHistoryEntity?) -> Unit

internal typealias requireDataCardAction =
            (data: CardWithHistoryEntity) -> Unit

internal typealias dataCardImageAction =
            (data: Drawable) -> Unit

internal typealias dataNumberAction =
            (data: Int) -> Unit

internal typealias dataCardImageActionFault =
            () -> ColorDrawable

internal typealias shareCardsListAction =
            (MutableList<CardWithHistoryEntity>) -> Unit

internal typealias dataPathAction =
            (Uri) -> Unit

internal typealias dataChangeInfoAction =
            (id: Int, cardModel: CardWithHistoryEntity, changedData: String) -> CardWithHistoryEntity?

internal typealias permissionAction = (
    faultState: unitAction, successAction: unitAction,
) -> ActivityResultLauncher<Array<String>>


internal fun View.navigateTo(
    destinationId: Int,
    args: Bundle? = null,
) =
    MainScope().launch(Dispatchers.Main) {
        findNavController(this@navigateTo)
            .navigate(
                destinationId,
                args,
                navOptions = navOptions {
                    anim {
                        this.enter = R.anim.nav_default_enter_anim
                        this.exit = R.anim.nav_default_exit_anim
                        this.popEnter = R.anim.nav_default_pop_enter_anim
                        this.popExit = R.anim.nav_default_pop_exit_anim
                    }
                }
            )
    }

internal fun NavController.navigateTo(
    destinationId: Int,
    args: Bundle? = null,
) =
    MainScope().launch(Dispatchers.Main) {
        navigate(
            destinationId,
            args,
            navOptions = navOptions {
                anim {
                    this.enter = R.anim.nav_default_enter_anim
                    this.exit = R.anim.nav_default_exit_anim
                    this.popEnter = R.anim.nav_default_pop_enter_anim
                    this.popExit = R.anim.nav_default_pop_exit_anim
                }
            }
        )
    }

internal fun FragmentActivity.getModuleAppComponent() =
    (this.application as BasicApplication)
        .modulesComponent

internal infix fun FragmentActivity.changeVisibleBottomNavBar(
    isVisible: Boolean,
) =
    (this as HostActivity)::changeVisibleBottomNavBar.invoke(isVisible)

internal fun FragmentActivity.navigateToHome() =
    (this as HostActivity)::navigateToHome.invoke()

internal inline infix fun <reified T> Bundle?.extractParcelable(
    key: String,
): T? =
    if (Build.VERSION.SDK_INT >= 33)
        this?.getParcelable(key, T::class.java)
    else
        @Suppress("DEPRECATION")
        this?.getParcelable(key) as? T

internal inline fun <reified T> Parcel?.extractParcelable(): T? =
    if (Build.VERSION.SDK_INT >= 33)
        this?.readParcelable(T::class.java.classLoader, T::class.java)
    else
        @Suppress("DEPRECATION")
        this?.readParcelable(T::class.java.classLoader)

internal fun FragmentActivity.requireDrawerLayout() =
    (this as? HostActivity)?.run {
        ::requireDrawerImpl.invoke()
    }

internal fun Context.currentScreenPositionIsVertical() =
    this.resources.configuration.orientation ==
            Configuration.ORIENTATION_PORTRAIT

internal infix fun View.setVisible(isVisible: Boolean) =
    apply {
        visibility =
            if (isVisible)
                View.VISIBLE
            else
                View.GONE
    }

internal fun getDisplaySize() =
    Resources.getSystem().displayMetrics.run {
        Pair(
            this.widthPixels, this.heightPixels
        )
    }

internal fun Bitmap.getColorRGB(): Int =
    Bitmap.createScaledBitmap(
        this, 1, 1, true
    )?.let { newBitmapChange ->
        val color = newBitmapChange.getPixel(0, 0)
        newBitmapChange.recycle()

        return Color.rgb(
            Color.red(color), Color.green(color), Color.blue(color)
        )
    } ?: Color.BLACK

@SuppressLint("SimpleDateFormat")
internal fun setUniqueIdentifier() =
    (SimpleDateFormat("ddMMyyyySSSSssZ")
        .format(Calendar.getInstance().time)
            + PersonalFirebase
        .generationPersonalUID()
        .hashCode() / 2)

internal fun SimpleDateFormat.getCurrentTime() =
    format(Date())

internal fun getTimeNow() =
    Date().time

internal inline fun Fragment.checkPermissions(
    crossinline faultState: unitAction,
    crossinline successAction: unitAction,
) = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { result ->
    MainScope().launch(Dispatchers.Main) {
        if (result.values.all { it })
            successAction.invoke()
        else
            faultState.invoke()
    }
}

internal inline infix fun Fragment.requireImageFromGalleryResult(
    crossinline successAction: dataPathAction,
) = registerForActivityResult(
    ActivityResultContracts.GetContent()
) {
    it?.apply {
        successAction.invoke(this)
    }
}

internal fun ScheduledExecutorService.requireShutdown() {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            this@requireShutdown.shutdownNow()
        } catch (ignored: Exception) {
        }
    }
}

internal fun String.sewLinkToString(
    activity: FragmentActivity,
    vararg spannableData: SpannableData,
) =
    SpannableString(this).apply {

        spannableData.forEach { singleData ->

            setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        activity.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    activity.getString(
                                        singleData.linkId
                                    )
                                )
                            )
                        )
                    }
                },
                singleData.startParentIndex,
                singleData.endParentIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            @Suppress("DEPRECATION")
            setSpan(
                ForegroundColorSpan(
                    activity.resources.getColor(
                        R.color.button_color
                    )
                ),
                singleData.startParentIndex,
                singleData.endParentIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

internal object SupportLiveData {
    infix fun <T : Any?> MutableLiveData<T>.default(initialState: T) =
        apply {
            CoroutineScope(Dispatchers.Main).launch {
                value = initialState
            }
        }

    infix fun <T : Any?> MutableLiveData<T>.set(newState: T) =
        apply {
            CoroutineScope(Dispatchers.Main).launch {
                value = newState
            }
        }
}

internal const val TRANSACTION_KEY = "key"
