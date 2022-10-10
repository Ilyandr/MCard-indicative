package com.example.mcard.repository.features.utils

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import coil.load
import coil.request.CachePolicy
import com.example.mcard.databinding.IncludeBarcodeLayoutBinding
import com.example.mcard.databinding.ManuallyCardAddInputDataBinding
import com.example.mcard.repository.features.currentScreenPositionIsVertical
import com.example.mcard.repository.features.optionally.CardAdaptation.comparisonData
import com.example.mcard.repository.features.optionally.CardAdaptation.transliterateCardName
import com.example.mcard.repository.features.rest.firebase.PersonalFirebase.Companion.requestCardImage
import com.example.mcard.repository.features.unitAction
import com.example.mcard.repository.features.utils.CardBarcodeManager.setCardBarcode
import com.example.mcard.repository.models.storage.BarcodeModel
import com.example.mcard.repository.models.storage.BarcodeModel.CREATOR.serialaziableEntityBarcode
import com.example.mcard.repository.models.storage.CardEntity
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.material.imageview.ShapeableImageView
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

internal object DesignCardManager {

    val cacheParams: ViewGroup.LayoutParams? = null
    fun Int.whiteOrBlackColor() =
        if ((Color.blue(this) >= 128)
            || (Color.green(this) >= 128)
            || (Color.green(this) >= 128)
        )
            Color.parseColor("#000000")
        else Color.parseColor("#FFFFFF")

    fun setCardSize(
        formCard: View,
        withItemParams: Boolean = false,
        percentPruning: Float = 1f,
        paddingHorizontal: Int? = null,
        scaleSizePercent: Float? = null,
    ) =
        formCard.doOnLayout { itemView ->

            CoroutineScope(Dispatchers.IO).launch {

                formCard.context.currentScreenPositionIsVertical().run isVerticalOrientation@{

                    val params = formCard.layoutParams
                    val scaleSizeOption = scaleSizePercent ?: 1f

                    params.width =
                        ((((if (withItemParams)
                            itemView.width
                        else
                            Resources.getSystem().displayMetrics.widthPixels) *
                                (if (this@isVerticalOrientation)
                                    0.95
                                else
                                    0.46
                                        )
                                ) * percentPruning).toInt() - (paddingHorizontal ?: 0))

                    params.height =
                        (params.width * scaleSizeOption / 1.5f).toInt()


                    CoroutineScope(Dispatchers.Main).launch {
                        formCard.layoutParams = params
                    }
                }
            }
        }

    fun ShapeableImageView.staticLoadCardImage(
        cardEntity: CardEntity,
        withCaching: Boolean,
        additionalSuccessAction: unitAction? = null,
        additionalFaultAction: unitAction? = null,
    ) {
        (context requireCardImageFile cardEntity.name.toString()).apply {
            if (exists())
                load(this) {
                    additionalSuccessAction?.invoke()
                    memoryCachePolicy(CachePolicy.DISABLED)
                    diskCachePolicy(CachePolicy.DISABLED)
                    crossfade(true)
                }
            else {
                context.requestCardImage(
                    this.name,
                    saveActionFault = {
                        setBackgroundColor(cardEntity.color!!)
                        additionalFaultAction?.invoke()
                    }
                ) {
                    if (withCaching)
                        it.saveDrawable(this) {
                            this@staticLoadCardImage.staticLoadCardImage(
                                cardEntity,
                                false,
                                additionalSuccessAction
                            )
                        }
                    else {
                        this@staticLoadCardImage dynamicLoadCardImage it
                        additionalSuccessAction?.invoke()
                    }
                }
            }
        }
    }

    private infix fun ShapeableImageView.dynamicLoadCardImage(
        data: Drawable,
    ) =
        load(data) {
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.DISABLED)
            crossfade(true)
        }

    infix fun Context.requireCardImageFile(cardName: String) =
        (transliterateCardName(cardName) + ".jpg").run cardName@{

            File(applicationInfo.dataDir)
                .listFiles()
                ?.find {
                    comparisonData(
                        it.name, this@cardName
                    )
                } ?: File(
                applicationInfo.dataDir, this
            )
        }

    fun Context.renameCardName(oldName: String, newName: String) {
        requireCardImageFile(oldName).run {

            if (exists())
                renameTo(
                    File(
                        applicationInfo.dataDir,
                        "$newName.jpg"
                    )
                )
        }
    }

    infix fun Context.removeCardCache(name: String?) {
        name?.let {
            requireCardImageFile(it).run {
                if (exists())
                    delete()
            }
        }
    }

    fun Context.removeCacheApp() =
        File(
            applicationInfo.dataDir
        ).listFiles()?.forEach {
            it.delete()
        }


    fun Drawable.saveDrawable(file: File, savedAction: unitAction) =
        MainScope().async(Dispatchers.IO) {

            (this@saveDrawable as? BitmapDrawable)?.apply {
                val outStream = FileOutputStream(file)

                bitmap.compress(
                    Bitmap.CompressFormat.JPEG, 90, outStream
                )

                outStream.flush()
                outStream.close()
            }
        }.let {
            MainScope().launch(Dispatchers.Main) {
                it.await()?.apply {
                    savedAction.invoke()
                }
            }
        }

    infix fun Uri.requireDrawable(
        context: Context,
    ) =
        context.contentResolver?.openInputStream(this)
            ?.run {
                Drawable.createFromStream(
                    this, this@requireDrawable.toString()
                ).apply {
                    close()
                }
            }

    fun manageCardData(
        viewBindingCardData: ManuallyCardAddInputDataBinding,
        viewBindingBarcodeData: IncludeBarcodeLayoutBinding,
        cardEntity: CardEntity,
    ) {
        viewBindingCardData.inputName.setText(
            cardEntity.name
        )

        viewBindingCardData.inputNumber.setText(
            cardEntity.number
        )

        viewBindingCardData.inputColor.imageTintList =
            ColorStateList.valueOf(
                cardEntity.color!!
            )

        viewBindingBarcodeData changeCardBarcode
                cardEntity.barcode
    }

    infix fun IncludeBarcodeLayoutBinding.changeCardBarcode(
        barcode: String?,
    ) {
        barcodeContainer.doOnLayout { container ->
            barcode?.apply {
                serialaziableEntityBarcode(
                    this
                ).setCardBarcode(
                    barcodeImageView,
                    Pair(
                        container.width,
                        container.height
                    )
                )

                barcodeImageView.visibility =
                    View.VISIBLE
                barcodeNoneTextView.visibility =
                    View.INVISIBLE

                barcodeDataTextView.text =
                    serialaziableEntityBarcode(
                        barcode
                    ).barcodeDataString
            } ?: run {

                barcodeImageView.visibility =
                    View.INVISIBLE
                barcodeNoneTextView.visibility =
                    View.VISIBLE
            }
        }
    }
}

internal object CardBarcodeManager {
    @JvmStatic
    fun BarcodeModel.setCardBarcode(
        barcodeView: AppCompatImageView,
        parentLayoutParams: Pair<Int, Int>?,
    ) =
        barcodeDataString?.let { dataString ->

            barcodeDataType?.let { typeInfo ->

                barcodeView.doOnPreDraw {
                    if (typeInfo == Barcode.QR_CODE) {

                        barcodeView.scaleX = 1.4f
                        barcodeView.scaleY = 1.4f
                    } else {
                        barcodeView.scaleX = 1f
                        barcodeView.scaleY = 1f
                    }

                    barcodeView.load(
                        parentLayoutParams?.second?.let { height ->

                            parentLayoutParams.first.let { width ->

                                BarcodeEncoder().encodeBitmap(
                                    dataString,
                                    barcodeFormat(typeInfo),
                                    if (it.context.currentScreenPositionIsVertical())
                                        width
                                    else
                                        width / 2,
                                    height
                                ).replaceColor(
                                    Color.WHITE,
                                    Color.parseColor(
                                        "#00000000"
                                    ),
                                    Color.parseColor(
                                        "#4F91FF"
                                    )
                                )
                            }
                        }
                    ) {
                        this.crossfade(true)
                    }
                }
            }
        }

    fun Bitmap.replaceColor(
        fromColor: Int, targetColor: Int, featureColor: Int,
    ): Bitmap {

        val width = width
        val height = height
        val pixels = IntArray(width * height)

        getPixels(
            pixels, 0, width, 0, 0, width, height
        )

        for (x in pixels.indices)
            pixels[x] =
                if (pixels[x] == fromColor)
                    targetColor
                else
                    featureColor

        return Bitmap.createBitmap(
            width, height, config
        ).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }

    private infix fun barcodeFormat(barcodeTypeID: Int) =
        when (barcodeTypeID) {
            Barcode.PDF417 -> BarcodeFormat.PDF_417
            Barcode.EAN_13 -> BarcodeFormat.EAN_13
            Barcode.DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
            Barcode.AZTEC -> BarcodeFormat.AZTEC
            Barcode.QR_CODE -> BarcodeFormat.QR_CODE
            Barcode.CODE_128 -> BarcodeFormat.CODE_128
            Barcode.UPC_A -> BarcodeFormat.UPC_A
            Barcode.EAN_8 -> BarcodeFormat.EAN_8
            Barcode.CODE_39 -> BarcodeFormat.CODE_39
            Barcode.CODE_93 -> BarcodeFormat.CODE_93
            else -> BarcodeFormat.ITF
        }
}