package com.example.mcard.SideFunctionality

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.TimeAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.allViews
import androidx.core.view.forEach
import androidx.core.view.setPadding
import com.example.mcard.FunctionalInterfaces.DelegateVoidInterface
import com.example.mcard.R
import com.example.mcard.StorageAppActions.DataInterfaceCard
import com.example.mcard.GeneralInterfaceApp.ThemeAppController
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlin.NullPointerException

@DelicateCoroutinesApi
internal class CustomAppDialog(context: Context) : Dialog(context)
{
    private lateinit var mainLinearLayout: LinearLayout
    private lateinit var btnLinearLayout: LinearLayout
    private var generalMessageView: AppCompatTextView? = null
    private val themeAppController: ThemeAppController

    private var allViewsInDialog: Array<out View?>? = null
    private var defaultTextInCustomDialog: Pair<TextView?, TextView?>? = null
    private var mainDialogAnimation: DialogManagerAnimations? = null
    private var useAdditionalBtn = false
    private var subscribeClass = false
    private var cancelData = true
    var acceptLoading = true

    companion object
    {
        const val DEFAULT_MESSAGE_SIZE = 4.25f

        @JvmStatic
        fun singleDialogItemBuilder(
            context: Context
            , @SuppressLint("ResourceType") resourseMessageID: Int
            , @SuppressLint("ResourceType") resourseDrawableIconID: Int
            , actionAfterClickItem: View.OnClickListener): RelativeLayout
        {
            val relativeLayout = RelativeLayout(context)
            val appTextView = AppCompatTextView(context)
            val iconDrawableBtn = AppCompatImageButton(context)

            ThemeAppController(DataInterfaceCard(context))
                .settingsTextAdapter(
                    appTextView
                    , context.getString(resourseMessageID))

            appTextView.text =
                "\n${context.getString(resourseMessageID)}\n"
            appTextView.textSize = 14f
            appTextView.gravity = Gravity.CENTER
            appTextView.setOnClickListener(actionAfterClickItem)
            appTextView.background =
                context.getDrawable(R.drawable.main_select_btn)

            appTextView.setTextColor(
                Color.parseColor(ThemeAppController.BASIC_APP_COLOR))
            appTextView.typeface =
                ResourcesCompat.getFont(context, R.font.bandera_small_font)

            iconDrawableBtn.setImageResource(resourseDrawableIconID)
            iconDrawableBtn.setBackgroundColor(Color.TRANSPARENT)
            iconDrawableBtn.imageTintList =
                ColorStateList.valueOf(
                    Color.parseColor(
                        ThemeAppController.BASIC_APP_COLOR))
            iconDrawableBtn.setPadding(45)

            relativeLayout.addView(appTextView)
            relativeLayout.addView(iconDrawableBtn)
            relativeLayout.gravity = Gravity.CENTER
            return relativeLayout
        }
    }

    init
    {
        this.themeAppController =
            ThemeAppController(
                DataInterfaceCard(context))
    }

    fun buildEntityDialog(animation: Boolean): CustomAppDialog
    {
        this.setContentView(R.layout.dialog_default_form)
        this.mainLinearLayout = findViewById(R.id.mainLinearLayout)
        this.btnLinearLayout = findViewById(R.id.btnLinearLayout)

        this.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT))

        this.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT
            , ViewGroup.LayoutParams.WRAP_CONTENT)

        if (animation)
        {
            this.mainDialogAnimation = DialogManagerAnimations(
                this.findViewById(R.id.gradientPreloaderView)
                , subscribeClass)

            this.setOnCancelListener {
                mainDialogAnimation!!.stopAnimation()
                this.hide()
            }
            this.mainDialogAnimation?.startAnimation()
        }
        else
        {
            val customInterfaceColor =
                DataInterfaceCard(context)
                    .actionGeneralInterfaceAppColor(null)

            if (customInterfaceColor.first != Color.TRANSPARENT)
                this.findViewById<LinearLayout>(R.id.gradientPreloaderView)
                    .backgroundTintList =
                    (ColorStateList.valueOf(
                        customInterfaceColor.first))
        }
        return this
    }

    fun offExitActionPermission(offForButtons: Boolean): CustomAppDialog
    {
        this.setCancelable(false)
        if (offForButtons)
            this.cancelData = false
        return this
    }

    fun setInfoSubscribeClass(): CustomAppDialog
    {
        this.subscribeClass = true
        return this
    }

     infix fun setTitle(textInfo: String): CustomAppDialog
     {
        val tvInfoViewNowRecognition =
            this.findViewById<AppCompatTextView>(R.id.primaryText)
         this.defaultTextInCustomDialog =
             Pair(tvInfoViewNowRecognition, null)

         tvInfoViewNowRecognition?.typeface =
             ResourcesCompat.getFont(
                 context, R.font.bandera_middle_font)

         tvInfoViewNowRecognition?.text = textInfo
         return this
    }

    fun setViews(vararg allViews: View?): CustomAppDialog
    {
        if (this.generalMessageView == null)
          this.mainLinearLayout.removeAllViews()

        allViews.forEach { mainLinearLayout.addView(it) }
        setSizeMainContainer((
                mainLinearLayout.childCount * 2 + 0.75).toFloat())
        return this
    }

    fun setMessage(messageID: Int, sizeMessage: Float): CustomAppDialog
    {
        this.generalMessageView = findViewById(
            R.id.generalMessageTV)
        this.defaultTextInCustomDialog = Pair(
            defaultTextInCustomDialog?.first, this.generalMessageView)

        this.generalMessageView?.setText(messageID)
        this.generalMessageView?.setTextColor(Color.WHITE)
        this.generalMessageView?.typeface =
            ResourcesCompat.getFont(
                context, R.font.bandera_small_font)

        this.setSizeMainContainer(sizeMessage)
        return this
    }

    fun setMessage(messageString: String, sizeMessage: Float): CustomAppDialog
    {
        this.generalMessageView = findViewById(
            R.id.generalMessageTV)
        this.defaultTextInCustomDialog = Pair(
            defaultTextInCustomDialog?.first, this.generalMessageView)

        this.generalMessageView?.text = messageString
        this.generalMessageView?.setTextColor(Color.WHITE)
        this.generalMessageView?.typeface =
            ResourcesCompat.getFont(
                context, R.font.bandera_small_font)

        this.setSizeMainContainer(sizeMessage)
        return this
    }

    fun changeSingleView(
        viewInGeneralLayoutID: Int, delegateVoidActionFun: DelegateVoidInterface) =
        this.mainLinearLayout
            .allViews
            .forEach {
                if (it.id == viewInGeneralLayoutID)
                {
                    delegateVoidActionFun.delegateFunction(it)
                    return@forEach
                }
            }

    fun setContainerParams(
        ml: Int, mr: Int, mb: Int, forAllChildView: Boolean = false): CustomAppDialog
    {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT
            , LinearLayout.LayoutParams.WRAP_CONTENT)

        layoutParams.setMargins(
            ml, 0, mr, mb)

        this.mainLinearLayout.gravity =
            Gravity.CENTER
        this.btnLinearLayout.gravity =
            Gravity.CENTER

        if (forAllChildView && this.mainLinearLayout.childCount != 0)
            this.mainLinearLayout.forEach {
                it.layoutParams = layoutParams
            }
        return this
    }

    fun setPositiveButton(textInfo: String
     , listener: View.OnClickListener? = null): CustomAppDialog
    {
        val btnPositive =
            this.findViewById<AppCompatButton>(R.id.btnPositive)

        btnPositive.hint = textInfo
        btnPositive.textSize = 15f
        btnPositive?.typeface =
            ResourcesCompat.getFont(
                context, R.font.bandera_small_font)

        btnPositive.setOnClickListener {
            listener?.onClick(btnPositive)
            if (this.cancelData)
                this.cancel()
        }
        return this
    }

    fun setAdditionalButton(
        textInfo: String? = null
        , listener: View.OnClickListener? = null): CustomAppDialog
    {
        this.useAdditionalBtn = true
        val btnAdditional =
            this.findViewById<AppCompatButton>(R.id.btnAdditionaly)

        btnAdditional.hint = textInfo ?: ""
        btnAdditional.textSize = 15f
        btnAdditional?.typeface =
            ResourcesCompat.getFont(
                context, R.font.bandera_small_font)

        btnAdditional.setOnClickListener {
            listener?.onClick(btnAdditional)
            if (cancelData)
                this.cancel()
        }
        return this
    }

    fun setNegativeButton(textInfo: String
     , listener: View.OnClickListener? = null): CustomAppDialog
    {
        val btnNegative =
            this.findViewById<AppCompatButton>(R.id.btnNegative)

        btnNegative.textSize = 15f
        btnNegative?.typeface =
            ResourcesCompat.getFont(
                context, R.font.bandera_small_font)

        btnNegative.setOnClickListener {
            listener?.onClick(btnNegative)
            if (cancelData)
                this.cancel()
        }
        btnNegative.hint = textInfo
        return this
    }

    fun setSizeMainContainer(gravity: Float): CustomAppDialog
    {
        val layoutParams = LinearLayout.LayoutParams(
            this.btnLinearLayout.layoutParams.width
            , this.btnLinearLayout.layoutParams.height)
        layoutParams.weight = gravity

        this.mainLinearLayout.layoutParams = layoutParams
        return this
    }

    infix fun setActionForCancer(
        actionCancer: () -> Unit): CustomAppDialog
    {
        this.setOnCancelListener {
            this.mainDialogAnimation?.stopAnimation()
            run { actionCancer() }
            this.hide()
        }
        return this
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun showLoadingDialog(
        showOrHide: Boolean, textInfo: String? = null) = if (showOrHide)
    {
        this.acceptLoading = false
        this.setContentView(R.layout.dialog_loading_process)
        this.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT))

        this.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT
            , ViewGroup.LayoutParams.WRAP_CONTENT)

        this.mainDialogAnimation = DialogManagerAnimations(
            this.findViewById(R.id.gradientPreloaderView)
            , subscribeClass)

        this.setOnCancelListener {
            mainDialogAnimation?.stopAnimation()
            this.hide()
        }
        this.mainDialogAnimation?.startAnimation()

        val animLoadText = AnimationUtils.loadAnimation(
            context, R.anim.light_selected_btn)
        animLoadText.repeatCount = 100

        findViewById<AppCompatImageView>(R.id.loading)
            .startAnimation(
                AnimationUtils.loadAnimation(
                context, R.anim.anim_loading))
        findViewById<AppCompatTextView>(R.id.primaryText)
            .startAnimation(animLoadText)

        this.setCancelable(false)
        textInfo?.let { this.setTitle(textInfo = it).show() }
    }
    else
    {
        this.acceptLoading = true
        this.setCancelable(true)
        this.mainDialogAnimation?.stopAnimation()
        this.cancel()
    }

    infix fun invalidateIconsCustomDialog(
        themeAppController: ThemeAppController
    ) = try
        {
            if (this.allViewsInDialog != null
                && this.allViewsInDialog!!.isNotEmpty())
                allViewsInDialog?.forEach {
                    themeAppController.setOptionsButtons(
                        it!! as AppCompatButton)
                }
            true
        } catch (ex: NullPointerException) { false }


    infix fun invalidateTextColorDialog(
        themeAppController: ThemeAppController) = try
        {
            if (!themeAppController
                    .settingsText(this.defaultTextInCustomDialog?.first!!
                        , this.defaultTextInCustomDialog?.first!!.text.toString()))
                this.defaultTextInCustomDialog?.first!!.setTextColor(
                    Color.parseColor(
                        if (subscribeClass) "#2b2410"
                    else "#E6E6FA"))

            @Suppress("UNCHECKED_CAST")
            if (this.allViewsInDialog != null
                && this.allViewsInDialog!!.isNotEmpty())
                this.themeAppController.setOptionsButtons(
                    *this.allViewsInDialog as Array<out View>)
            true
        } catch (ex: NullPointerException) { false }

    fun invalidateBackgroundDialog() =
        this.mainDialogAnimation?.startAnimation()

    override fun show()
    {
        try { super.show() }
        catch (invalidContextData: WindowManager.BadTokenException) {}
    }

    private inner class DialogManagerAnimations(
        private val preloader: LinearLayout
        , private val itsSubscribeInfo: Boolean)
    {
        fun startAnimation()
        {
            val dataInterfaceCardColor = DataInterfaceCard(context)
                .actionGeneralInterfaceAppColor(null)
                .first

            val start: Int =
                if (dataInterfaceCardColor == Color.TRANSPARENT)
                    Color.parseColor("#474747")
                else
                    shadeOfColor(dataInterfaceCardColor, 4, true)

            val mid: Int =
                if (dataInterfaceCardColor == Color.TRANSPARENT)
                    Color.parseColor("#6B6B6C")
                else
                    shadeOfColor(dataInterfaceCardColor, 6, false)

            val end: Int =
                if (dataInterfaceCardColor == Color.TRANSPARENT)
                    Color.parseColor("#404040")
                else
                    shadeOfColor(dataInterfaceCardColor, 2, false)

            val evaluator = ArgbEvaluator()
            preloader.visibility = View.VISIBLE

            val gradient = preloader.background as GradientDrawable
            val animator = TimeAnimator.ofFloat(0.0f, 1.0f)

            animator.duration = 2000
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE

            animator.addUpdateListener { valueAnimator ->
                val fraction = valueAnimator.animatedFraction
                val newStrat = evaluator.evaluate(fraction, start, end) as Int
                val newMid = evaluator.evaluate(fraction, mid, start) as Int
                val newEnd = evaluator.evaluate(fraction, end, mid) as Int
                val newArray = intArrayOf(newStrat, newMid, newEnd)
                gradient.colors = newArray
            }
            animator.start()
        }

        fun stopAnimation() =
            ObjectAnimator.ofFloat(
                preloader
                , "alpha"
                , 0f)
                .setDuration(125)
                .start()

        fun shadeOfColor(inputColor: Int, degree: Int, darkModeOptions: Boolean) =
            Color.rgb(
                (Color.red(inputColor)
                        + (if (darkModeOptions) -(Color.red(inputColor) / degree)
                else (Color.red(inputColor) / degree)))
                , (Color.green(inputColor)
                        + (if (darkModeOptions) -(Color.green(inputColor) / degree)
                else (Color.green(inputColor) / degree)))
                , (Color.blue(inputColor)
                        + (if (darkModeOptions) -(Color.blue(inputColor) / degree)
                else (Color.blue(inputColor) / degree))))
    }
}