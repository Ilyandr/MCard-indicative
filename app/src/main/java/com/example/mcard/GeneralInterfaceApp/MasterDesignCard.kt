@file:JvmName("")
@file:JvmMultifileClass
@file:Suppress("DEPRECATION")

package com.example.mcard.GeneralInterfaceApp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.provider.MediaStore
import android.view.Display
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.*
import androidx.annotation.UiThread
import androidx.appcompat.widget.AppCompatButton
import androidx.core.net.toUri
import com.example.mcard.AdapersGroup.BasicCardManagerAdapter.percentageSimilarityResult
import com.example.mcard.AdapersGroup.BasicCardManagerAdapter.transliterateCardName
import com.example.mcard.AdapersGroup.CardInfoEntity
import com.example.mcard.BasicAppActivity
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomRealtiveLayout
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.RoundRectCornerImageView
import com.example.mcard.GroupServerActions.GlobalDataFBManager
import com.example.mcard.SideFunctionality.CustomAppDialog
import com.example.mcard.StorageAppActions.SQLiteChanges.CardManagerDB
import com.example.mcard.StorageAppActions.DataInterfaceCard
import com.example.mcard.UserActionsCard.*
import com.example.mcard.StorageAppActions.SharedPreferencesManager
import com.google.android.gms.common.util.IOUtils
import com.google.android.gms.vision.barcode.Barcode
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import id.zelory.compressor.Compressor
import kotlinx.coroutines.*
import java.io.*
import java.net.URL
import kotlin.math.roundToInt

@DelicateCoroutinesApi
internal class MasterDesignCard(private val context: Context)
{
    private var dataCard: MutableList<TextView>? = null
    private var cardManagerDB: CardManagerDB? = null
    private var updateListViewCustom: CustomHeaderListView? = null
    private var dangerParallelSaving: Boolean = false

    constructor(context: Context
                , dataCard: MutableList<TextView>)
            : this(context)
    { this.dataCard = dataCard }

    constructor(context: Context
                , cardManagerDB: CardManagerDB
                , updateListViewCustom: CustomHeaderListView)
            : this(context)
    {
        this.cardManagerDB = cardManagerDB
        this.updateListViewCustom = updateListViewCustom
    }

    companion object
    {
        const val MODE_GET_PARAM_WIDTH = -1
        const val MODE_GET_PARAM_HEIGHT = 2

        const val MODE_DESIGN_CHECK_CUSTOM = 1007
        const val MODE_DESIGN_CHECK_DOWNLOAD = -998
        const val MODE_DESIGN_CHECK_ALL = 9
        const val DOWNLOAD_FILE_POINT = "_DOWNLOAD---CARD_MCard_app"

        @JvmStatic
        fun removeCacheApp(fileDir: File)
        {
            val allFiles: Array<out File> =
                fileDir.listFiles() ?: return
            for (i in allFiles.indices)
                allFiles[i].delete()
        }

        @JvmStatic
        fun Int.getOpposedColor() = Color.rgb(
            255 - Color.red(this)
                , 255 - Color.green(this)
                , 255 - Color.blue(this))

        @JvmStatic
        fun Int.whiteOrBlackColor() = if ((Color.blue(this) >= 128)
            || (Color.green(this) >= 128)
            || (Color.green(this) >= 128))
                Color.parseColor("#000000")
        else Color.parseColor("#FFFFFF")

        @JvmStatic
         fun lowAbstractImageOptions(
           loadingDialog: CustomAppDialog
           , context: Context
           , fileData: String
           , imageView: RoundRectCornerImageView?
           , globalCardFragment: GlobalDataFBManager?
           , sharedPreferencesManager: SharedPreferencesManager?)
        {
            val loadNewData: Deferred<Uri> = GlobalScope.async(Dispatchers.IO)
            {
                val compressImageDataPath = Compressor.compress(
                    context, File(fileData))

                globalCardFragment?.loadPhotoProfileToFB(
                    loadingDialog, compressImageDataPath.toUri())

                sharedPreferencesManager?.path_userIconProfile(
                    compressImageDataPath.path)
                compressImageDataPath.toUri()
            }

            GlobalScope.launch(Dispatchers.Main) {
               imageView?.setImageURI(
                   loadNewData.await())
                loadingDialog.showLoadingDialog(false)
            }.start()
        }
    }

    @UiThread
    fun setCardDesignLocale(
        dynamicCardName: String?
        , colorCard: Int
        , formCard: RoundRectCornerImageView
    )
    {
        val haveImageDesign: Deferred<String?> =
            GlobalScope.async(Dispatchers.IO)
        {
            availabilityDesign(
            dynamicCardName ?: return@async null
                , MODE_DESIGN_CHECK_ALL
            )
        }

        val getDataRadiusCard: Deferred<Float> = GlobalScope.async(Dispatchers.IO)
        { DataInterfaceCard(context).actionRoundBorderCard() }

        val completeBitmapImage_LOCALE: Deferred<Bitmap> =
            GlobalScope.async(Dispatchers.IO)
        {
            if (haveImageDesign.await() == null)
                loadDefaultDesign(colorCard)
            else
            {
                try
                {
                    val pathDesignCard = context
                        .cacheDir
                        .toString() + "/" +
                            haveImageDesign.await()

                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true

                    BitmapFactory.decodeFile(
                        pathDesignCard, options)
                    options.inSampleSize =
                        this@MasterDesignCard calculateInSampleSize options
                    options.inJustDecodeBounds = false

                    val transportBitmap = BitmapFactory
                        .decodeFile(pathDesignCard, options)
                    transportBitmap.height > 0
                    transportBitmap
                }
                catch (e: Exception)
                { this@MasterDesignCard loadDefaultDesign colorCard }
            }
        }

        GlobalScope.launch(Dispatchers.Main)
        {
            formCard.setBorderRadius(
                getDataRadiusCard.await())
            formCard.setImageBitmap(
                completeBitmapImage_LOCALE.await())
            colorCardText(completeBitmapImage_LOCALE.await()
                , (haveImageDesign.await() == null))
        }.start()
    }

    private infix fun loadDefaultDesign(
        colorCard: Int) : Bitmap
    {
        val designCard: Bitmap = Bitmap.createBitmap(1
            , 1
            , Bitmap.Config.ARGB_8888)

        designCard.setPixel(0, 0, colorCard)
        return designCard
    }

    @UiThread
    fun setCardDesignNetwork(uriAddress: String
     , dynamicNameCard: String? = null
     , formCard: RoundRectCornerImageView? = null
     , btn_setResult: AppCompatButton? = null)
    {
        val modifyDynamicCardName = "${transliterateCardName(
            dynamicNameCard ?: return)}$DOWNLOAD_FILE_POINT.jpg"

        val haveDownloadDesign: Deferred<Boolean> =
            GlobalScope.async(Dispatchers.IO)
            {
                availabilityDesign(
                    modifyDynamicCardName.replace(
                        DOWNLOAD_FILE_POINT, "")
                    , MODE_DESIGN_CHECK_ALL
                ) == null
            }

        val getDataRadiusCard: Deferred<Float> = GlobalScope.async(Dispatchers.IO)
        { DataInterfaceCard(context).actionRoundBorderCard() }

        val loadBitmap: Deferred<Bitmap?> = GlobalScope.async(Dispatchers.IO)
        {
            val stream = BufferedInputStream(
                URL(uriAddress).openStream())
            val dataStream = ByteArrayOutputStream()
            val out = BufferedOutputStream(dataStream)

            try
            {
                IOUtils.copyStream(stream, out)
                out.flush()
                val data: ByteArray = dataStream.toByteArray()

                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true

                BitmapFactory.decodeByteArray(data, 0, data.size, options)
                options.inSampleSize =
                    this@MasterDesignCard calculateInSampleSize options

                options.inJustDecodeBounds = false
                BitmapFactory.decodeByteArray(data, 0, data.size, options)
            }
            catch (e: Exception) { null }
            finally
            {
                stream.close()
                out.close()
                dataStream.close()
            }
        }

        val saveBitmap: Deferred<Boolean> =
            GlobalScope.async(Dispatchers.IO)
        {
            if (haveDownloadDesign.await())
            {
                val file = File(
                    context.cacheDir, modifyDynamicCardName)
                val out = FileOutputStream(file)

                try
                {
                    loadBitmap.await()?.compress(
                        Bitmap.CompressFormat.JPEG
                        , 80
                        , out) ?: false
                }
                finally
                {
                    out.flush()
                    out.close()
                }
            } else false
        }

        GlobalScope.launch(Dispatchers.Main)
        {
            if (saveBitmap.await())
            {
                if (formCard != null
                    && btn_setResult != null
                    && updateListViewCustom == null
                    && cardManagerDB == null)
                {
                    formCard.setBorderRadius(
                        getDataRadiusCard.await())
                    formCard.setImageBitmap(
                        loadBitmap.await())

                    btn_setResult.hint =
                        CardAddActivity.TEXT_BTN_FINISH_STAGE_2
                    colorCardText(
                        loadBitmap.await() ?: return@launch)
                }

                cardManagerDB?.let { cardManagerDB ->
                    updateListViewCustom?.let { updateListView ->
                        BasicAppActivity.setListItemCards(context
                            , cardManagerDB
                            , updateListView
                            , null)

                        Toast.makeText(context
                            , "Альтернативный дизайн карты " +
                                    "\"${dynamicNameCard}\" " +
                                    "успешно загружен"
                            , Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }.start()
    }

    @SuppressLint("Range")
    fun loadNewPersonalCardDesign(dynamicCardName: String?
     , responseData: Intent
     , formCard: RoundRectCornerImageView
     , formBarcode: RelativeLayout
     , animationStart: Animation
     , animationFinish: Animation)
    {
        val cursor = responseData
            .data?.let {
                context.contentResolver.query(
                    it, null, null, null, null)
            } ?: return

        cursor.moveToFirst()
        val getPath = cursor.getString(
            cursor.getColumnIndex(
                MediaStore.MediaColumns.DATA))
        cursor.close()

        val loadImageProcess: Deferred<Bitmap> =
            GlobalScope.async(Dispatchers.IO)
        {
            val imageOptions = BitmapFactory.Options()
            imageOptions.inJustDecodeBounds = true

            BitmapFactory.decodeFile(getPath, imageOptions)
            imageOptions.inSampleSize =
                this@MasterDesignCard calculateInSampleSize imageOptions

            imageOptions.inJustDecodeBounds = false
            BitmapFactory.decodeFile(
                getPath, imageOptions)
        }

        val getDataRadiusCard: Deferred<Float> =
            GlobalScope.async(Dispatchers.IO)
        {
            DataInterfaceCard(context)
                .actionRoundBorderCard()
        }

        val saveImage: Deferred<Boolean> =
            GlobalScope.async(Dispatchers.IO)
        {
            val out: OutputStream = FileOutputStream(File(context.cacheDir
                , transliterateCardName(
                    dynamicCardName ?: return@async false)
                        + ".jpg"))

            loadImageProcess.await()
                .compress(
                    Bitmap.CompressFormat.JPEG
                    , 80
                    , out)
        }

        val sideActionDesignBarcode =
            GlobalScope.launch(Dispatchers.Main)
        {
            formBarcode.animation = animationStart
            animationStart.setAnimationListener(object : AnimationListener
            {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation)
                { formBarcode.animation = animationFinish }
            })
        }

        GlobalScope.launch(Dispatchers.Main)
        {
            if (saveImage.await())
            {
                formCard.setBorderRadius(
                    getDataRadiusCard.await())
                formCard.setImageBitmap(
                    loadImageProcess.await())

                sideActionDesignBarcode.start()
                colorCardText(
                    loadImageProcess.await())

                Toast.makeText(context
                    , "Персональный дизайн карты" +
                            " \"${dynamicCardName ?: return@launch}\"" +
                            " успешно загружен"
                    , Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun changeCardName(
        dynamicCardName: String?
        , changeInfo: String)
    {
        val checkCard: Deferred<String?> =
            GlobalScope.async(Dispatchers.IO)
        {
            availabilityDesign(
                dynamicCardName ?: return@async null
                , MODE_DESIGN_CHECK_ALL
            )
        }

        val startProcessRenameFile = GlobalScope.launch(Dispatchers.IO)
        {
            checkCard.await()?.let {
                val newCardDesignPath = File(context.cacheDir
                    , transliterateCardName(
                        changeInfo) +
                            if (it.contains(DOWNLOAD_FILE_POINT)) DOWNLOAD_FILE_POINT else ""
                                    + ".jpg")
                File(context.cacheDir, it).renameTo(
                    newCardDesignPath)
            }
        }

        GlobalScope.launch(Dispatchers.Main)
        {
            startProcessRenameFile.start()
            Toast.makeText(context
                ,"Карта \"${dynamicCardName}\" переименована на \"$changeInfo\""
                , Toast.LENGTH_SHORT)
                .show()
        }.start()
    }

    private infix fun calculateInSampleSize(
        options: BitmapFactory.Options): Int
    {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        val personalHeight =
            calculateNormalSize(MODE_GET_PARAM_HEIGHT)
        val personalWidth =
            calculateNormalSize(MODE_GET_PARAM_WIDTH)

        if (height > personalHeight
            || width > personalWidth)
        {
            val heightRatio = (height.toFloat()
                    / personalHeight.toFloat()).roundToInt()
            val widthRatio = (width.toFloat()
                    / personalWidth.toFloat()).roundToInt()
            inSampleSize = heightRatio.coerceAtMost(widthRatio)
        }
        return inSampleSize
    }

    fun colorCardText(
        bitmap: Bitmap, stockDesign: Boolean = false)
    {
        val dominantСolor: Deferred<Int> =
            GlobalScope.async(Dispatchers.IO)
        {
            val infoAutoColor = DataInterfaceCard(context)
                .actionTextColor()

            if (stockDesign && !infoAutoColor)
                return@async bitmap.getPixel(0, 0).whiteOrBlackColor()
            else if (stockDesign && infoAutoColor)
                return@async bitmap.getPixel(0, 0).getOpposedColor()

            var A = 0
            var R = 0
            var G = 0
            var B = 0

            var pixelColor: Int
            val width = bitmap.width / 2
            val height = bitmap.height / 5

            for (x in 0 until width)
            {
                for (y in 0 until height)
                {
                    pixelColor = bitmap.getPixel(x, y)
                    A += Color.alpha(pixelColor)
                    R += Color.red(pixelColor)
                    G += Color.green(pixelColor)
                    B += Color.blue(pixelColor)
                }
            }

            if (infoAutoColor)
                Color.argb(255 - A
                    , 255 - R
                    , 255 - G
                    , 255 - B)
            else
                Color.argb(
                    A, R, G, B).whiteOrBlackColor()
        }

        GlobalScope.launch(Dispatchers.Main)
        {
            for (i in dataCard!!.indices)
            {
                dataCard!![i].setTextColor(
                    dominantСolor.await())


            }
        }.start()
    }

    infix fun calculateNormalSize(MODE_GET_PARAM: Int): Int
    {
        val size = Point()
        val display: Display = try
        {
            (context as BasicAppActivity)
                .windowManager.defaultDisplay
        }
        catch (e: ClassCastException)
        {
            (context as CardAddActivity)
                .windowManager.defaultDisplay
        }
        display.getSize(size)

        return when (MODE_GET_PARAM)
        {
            MODE_GET_PARAM_HEIGHT ->
                (size.y / 3.5).toInt()
            MODE_GET_PARAM_WIDTH ->
                ( size.x / 1.15).toInt()
            else -> 600
        }
    }

    infix fun setCardSize(formCard: CustomRealtiveLayout)
    {
        val dataInterfaceCard =
            DataInterfaceCard(context)
        val dataSize =
            dataInterfaceCard.actionCardSize()

        val newParams: LinearLayout.LayoutParams

        if (dataSize != null)
        {
            newParams = LinearLayout.LayoutParams(
                dataSize.second, dataSize.first)
            val coordParams = dataInterfaceCard
                .actionCardCoord(getMode = true)!!

            formCard.layoutParams = newParams
            formCard.x = coordParams.first
        }
        else
        {
            formCard.layoutParams.height =
                calculateNormalSize(MODE_GET_PARAM_HEIGHT)
            formCard.layoutParams.width =
                calculateNormalSize(MODE_GET_PARAM_WIDTH)
            formCard.x = 75f
        }
        formCard.changeShadowCard(
            dataInterfaceCard.actionRoundBorderCard())
    }

    fun availabilityDesign(
        dynamicNameCard: String
        , inputEventMode: Int): String?
    {
        val modifyDynamicNameCard =
            transliterateCardName(dynamicNameCard) + ".jpg"
        val files = File(context.cacheDir.toString())
            .listFiles() ?: return null
        var returnFinalName: String? = null

        files.forEach {
            when (inputEventMode)
            {
                MODE_DESIGN_CHECK_DOWNLOAD ->
                {
                    if (it.name.contains(DOWNLOAD_FILE_POINT)
                        && percentageSimilarityResult(
                            modifyDynamicNameCard
                            , it.name.replace(DOWNLOAD_FILE_POINT, "")) >= 75)
                    {
                        returnFinalName = it.name
                        return@forEach
                    }
                }
                MODE_DESIGN_CHECK_CUSTOM ->
                {
                    if (!it.name.contains(DOWNLOAD_FILE_POINT)
                        && percentageSimilarityResult(
                           modifyDynamicNameCard, it.name) >= 75)
                    {
                        returnFinalName = it.name
                        return@forEach
                    }                }
                MODE_DESIGN_CHECK_ALL ->
                {
                    if (percentageSimilarityResult(
                            modifyDynamicNameCard
                            , it.name.replace(DOWNLOAD_FILE_POINT, ""))
                        >= 75)
                    {
                        returnFinalName = it.name
                        return@forEach
                    }
                }
                else -> return@forEach
            }
        }
        return returnFinalName
    }

     internal class CardBarcodeManager
     {
         companion object
         {
             @JvmStatic
             fun setCardBarcode(barcodeEntity: CardInfoEntity.BarcodeEntity?): Bitmap?
             {
                 barcodeEntity?.barcodeDataString?.let { dataString ->
                     barcodeEntity.barcodeDataType?.let { typeInfo ->
                         val bitMatrix = MultiFormatWriter().encode(
                             dataString
                             , barcodeTypeDetector(typeInfo)
                             , 1920
                             , 1080)
                         return BarcodeEncoder()
                             .createBitmap(bitMatrix)
                     }
                 } ?: return null
             }

             private fun barcodeTypeDetector(barcodeTypeID: Int) =
                 when(barcodeTypeID)
                 {
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
     }
}