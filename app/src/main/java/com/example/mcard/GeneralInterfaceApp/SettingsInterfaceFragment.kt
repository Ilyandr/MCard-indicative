package com.example.mcard.GeneralInterfaceApp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.widget.*
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import com.example.mcard.BasicAppActivity
import com.example.mcard.FunctionalInterfaces.AnimationsController
import com.example.mcard.R
import com.example.mcard.StorageAppActions.DataInterfaceCard
import com.example.mcard.SideFunctionality.CustomAppDialog
import com.example.mcard.SideFunctionality.GeneralStructApp
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomRealtiveLayout
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.RoundRectCornerImageView
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
@SuppressLint("SetTextI18n")
internal class SettingsInterfaceFragment : Fragment()
 , GeneralStructApp
 , View.OnClickListener
 , CompoundButton.OnCheckedChangeListener
 , AnimationsController
{
    private lateinit var viewX: View
    private lateinit var designCardIM: RoundRectCornerImageView
    private lateinit var generalLinearLayout: LinearLayout
    private lateinit var generalBarLayout: LinearLayout

    private lateinit var btnBack: AppCompatImageButton
    private lateinit var btnChangeSizeCard: AppCompatTextView
    private lateinit var btnSettingsText: AppCompatTextView
    private lateinit var btnChangeRoundBorder: AppCompatTextView
    private lateinit var btnControlAnimation: AppCompatTextView
    private lateinit var btnDefaultSettings: AppCompatTextView

    private lateinit var userControlAnimationSC: SwitchCompat
    private lateinit var animSelected: Animation
    private lateinit var dataInterfaceCard: DataInterfaceCard
    private lateinit var controllerThemeApp: ThemeAppController

    private lateinit var cardNameTV: AppCompatTextView
    private lateinit var cardNumberTV: AppCompatTextView
    private lateinit var cardSize: CustomRealtiveLayout
    private var flagChange = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        this.viewX = inflater.inflate(R.layout.fragment_settings_interface, container, false)

        findObjects()
        drawableView()
        basicWork()

        return this.viewX
    }

    override fun findObjects()
    {
        this.generalLinearLayout = viewX.findViewById(R.id.main_linear_reception)
        this.generalBarLayout = viewX.findViewById(R.id.bar_icon)

        this.designCardIM = viewX.findViewById(R.id.designCard)
        this.btnBack = viewX.findViewById(R.id.btn_back)
        this.btnSettingsText = viewX.findViewById(R.id.textSettingsBtn)
        this.btnChangeSizeCard = viewX.findViewById(R.id.change_card_size)
        this.btnChangeRoundBorder = viewX.findViewById(R.id.btnChangeRoundBorder)
        this.btnControlAnimation = viewX.findViewById(R.id.btnControlAnimation)
        this.btnDefaultSettings = viewX.findViewById(R.id.btnDefaultSettings)

        this.cardNameTV = viewX.findViewById(R.id.name_card)
        this.cardNumberTV = viewX.findViewById(R.id.number_card)
        this.userControlAnimationSC = viewX.findViewById(R.id.userControlAnimation)
        this.cardSize = viewX.findViewById(R.id.cardSize)

        this.animSelected =
            AnimationUtils.loadAnimation(
                requireContext(), R.anim.select_object)
        this.dataInterfaceCard = DataInterfaceCard(requireContext())
    }

    override fun drawableView()
    {
        this.controllerThemeApp =
            ThemeAppController(
                this.dataInterfaceCard)
        setDefaultCard()

        controllerThemeApp.settingsText(
            viewX.findViewById(R.id.name_fragment)
            , "Интерфейс карты")
        controllerThemeApp.changeDesignIconBar(
            this.generalBarLayout)
        controllerThemeApp.changeDesignDefaultView(
            this.generalLinearLayout)

        controllerThemeApp.setOptionsButtons(btnBack)
        controllerThemeApp.setOptionsButtons(btnChangeSizeCard
            , btnSettingsText
            , btnChangeRoundBorder
            , btnControlAnimation)
    }

    override fun basicWork()
    {
        this.userControlAnimationSC.isChecked =
            this.dataInterfaceCard.actionAnimSelectCard()

        this.btnBack.setOnClickListener(this)
        this.btnSettingsText.setOnClickListener(this)
        this.btnChangeSizeCard.setOnClickListener(this)
        this.btnChangeRoundBorder.setOnClickListener(this)
        this.btnControlAnimation.setOnClickListener(this)
        this.btnDefaultSettings.setOnClickListener(this)
        this.userControlAnimationSC.setOnCheckedChangeListener(this)
    }

    @SuppressLint("ResourceType")
    private fun setDefaultCard(setDefault: Boolean = false)
    {
        val setColor = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        setColor.setPixel(0, 0, Color.parseColor("#BD0202"))

        this.designCardIM.setBorderRadius(this.dataInterfaceCard.actionRoundBorderCard())
        this.designCardIM.setImageBitmap(setColor)

        this.cardNameTV.text = requireContext().getString(R.string.defaultNameCard)
        this.cardNumberTV.text = requireContext().getString(R.string.defaultNumberCard)

        val textSize = this.dataInterfaceCard.actionTextSize()
        val textGravity = this.dataInterfaceCard.actionTextGravity()

        val dataSize: Pair<Int, Int>? = if (setDefault)
            this.dataInterfaceCard.actionDefaultCardSize(getMode = true)
                else this.dataInterfaceCard.actionCardSize()

        if (dataSize != null)
        setCardSizeChild(dataSize.first, dataSize.second)

        this.cardSize.changeShadowCard(this.dataInterfaceCard.actionRoundBorderCard(), true)
        this.cardNameTV.textSize = textSize
        this.cardNumberTV.textSize = textSize

        updateTextColor(outputInfo = false)
        this.cardNameTV.gravity = textGravity
        this.cardNumberTV.gravity = textGravity
    }

    override fun onClick(selectButton: View)
    {
        selectButton.startAnimation(
            this.animSelected)

        when(selectButton.id)
        {
            R.id.change_card_size -> cardSize()
            R.id.textSettingsBtn -> generalTextSettings()
            R.id.btnChangeRoundBorder -> changeBorderCard()
            R.id.btnDefaultSettings -> returnDefaultSettings()
            R.id.btnControlAnimation ->
            {
                userControlAnimationSC.isChecked = !userControlAnimationSC.isChecked
            }
           else -> requireActivity().onBackPressed()
        }
    }

    private fun generalTextSettings()
    {
        CustomAppDialog(requireContext())
            .buildEntityDialog(true)
            .setTitle("Настройки текста")
            .setViews( CustomAppDialog.singleDialogItemBuilder(
                requireContext()
                , R.string.dialogSettingsCard0
                , R.drawable.change_text_gravity)
            {
                it.startAnimation(animSelected)
                textGravity(it)
            }, CustomAppDialog.singleDialogItemBuilder(
                    requireContext()
                    , R.string.dialogSettingsCard1
                    , R.drawable.edit_color_icon)
                {
                    it.startAnimation(animSelected)
                    changeTextColor()
                }, CustomAppDialog.singleDialogItemBuilder(
                requireContext()
                , R.string.dialogSettingsCard2
                , R.drawable.edit_text_size)
            {
                it.startAnimation(animSelected)
                changeTextSize()
            }).setContainerParams(8, 8, 12, true)
            .setNegativeButton("Отмена", null)
            .show()
    }

    @SuppressLint("RtlHardcoded")
    private fun textGravity(viewOpen: View)
    {
        val popup = PopupMenu(
            requireContext(), viewOpen)

        popup.menu.add(Menu.NONE, 0, Menu.NONE, "Центральное расположение")
        popup.menu.add(Menu.NONE, 1, Menu.NONE, "Правое расположение")
        popup.menu.add(Menu.NONE, 2, Menu.NONE, "Левое расположение")
        popup.show()

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId)
            {
                0 ->
                {
                   this.dataInterfaceCard.actionTextGravity(Gravity.CENTER)
                   cardNameTV.gravity = Gravity.CENTER
                   cardNumberTV.gravity = Gravity.CENTER
                }
                1 ->
                {
                    this.dataInterfaceCard.actionTextGravity(Gravity.RIGHT)
                    cardNameTV.gravity = Gravity.RIGHT
                    cardNumberTV.gravity = Gravity.RIGHT
                }
                2 ->
                {
                    this.dataInterfaceCard.actionTextGravity(Gravity.LEFT)
                    cardNameTV.gravity = Gravity.LEFT
                    cardNumberTV.gravity = Gravity.LEFT
                }
            }

            Toast.makeText(requireContext()
                , requireContext().getString(R.string.succsessGravityText)
                , Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun cardSize()
    {
        this.dataInterfaceCard.actionDefaultCardSize(
            this.cardSize.height, this.cardSize.width, getMode = false)
        this.flagChange = !this.flagChange

        val widthInfo = AppCompatSeekBar(requireContext())
        val heightInfo = AppCompatSeekBar(requireContext())
        val heightInfoText = AppCompatTextView(requireContext())
        val widthInfoText = AppCompatTextView(requireContext())

        val defaultDataSize = Pair(
            this.cardSize.height, this.cardSize.width)
        val deafaultSize = this.dataInterfaceCard
            .actionDefaultCardSize(getMode = true)!!

        widthInfo.max = deafaultSize.second + 40
        widthInfo.progress = deafaultSize.second

        heightInfo.max = deafaultSize.first + 40
        heightInfo.progress = deafaultSize.first

        widthInfoText.textSize = 16f
        heightInfoText.textSize = 16f

        widthInfoText.gravity = Gravity.CENTER
        heightInfoText.gravity = Gravity.CENTER

        widthInfoText.setTextColor(Color.WHITE)
        heightInfoText.setTextColor(Color.WHITE)

        widthInfoText.text = "Ширина карты: ${cardSize.width}"
        heightInfoText.text = "Длина карты: ${cardSize.height}"

        widthInfo.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {
                if (progress < deafaultSize.second - 40)
                {
                    seekBar!!.progress = deafaultSize.second - 40
                    widthInfoText.text = "Минимальная длина карты"
                    return
                }

                setCardSizeChild(heightInfo.progress, progress)
                widthInfoText.text = "Длина карты: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?)
            {
                if (seekBar!!.progress < deafaultSize.second - 40)
                    seekBar.progress = deafaultSize.second - 40
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        heightInfo.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {
                if (progress < deafaultSize.first - 40)
                {
                    seekBar!!.progress = deafaultSize.first - 40
                    heightInfoText.text = "Минимальная ширина карты"
                    return
                }

                setCardSizeChild(progress, widthInfo.progress)
                heightInfoText.text = "Ширина карты: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?)
            {
                if (seekBar!!.progress < deafaultSize.first - 40)
                    seekBar.progress = deafaultSize.first - 40
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        CustomAppDialog(requireContext())
            .buildEntityDialog(animation = true)
            .setTitle(textInfo = "Изменение размеров карты")
            .setViews(widthInfo, widthInfoText, heightInfo, heightInfoText)
            .setSizeMainContainer(3.75f)
            .setPositiveButton("Сохранить")
            {
                this.flagChange = !this.flagChange
                this.dataInterfaceCard.actionCardSize(heightInfo.progress, widthInfo.progress)
                this.dataInterfaceCard.actionCardCoord(this.cardSize.x, this.cardSize.y, getMode = false)
                Toast.makeText(requireContext()
                    , requireContext().getString(R.string.successCardSize)
                    , Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена")
            {
                if (this.flagChange)
                {
                    this.flagChange = !this.flagChange
                    setCardSizeChild(
                        defaultDataSize.first, defaultDataSize.second)
                }
            }.setActionForCancer { if (this.flagChange)
            {
                this.flagChange = !this.flagChange
                setCardSizeChild(defaultDataSize.first, defaultDataSize.second)
            }}.show()
    }

    private fun setCardSizeChild(width: Int, height: Int)
    {
        val newParams = LinearLayout.LayoutParams(height, width)
        newParams.setMargins(20)
        newParams.gravity = Gravity.CENTER

        this.cardSize.layoutParams = newParams
        this.designCardIM.invalidate()
    }

    private fun changeTextColor()
    {
        CustomAppDialog(requireContext())
            .buildEntityDialog(true)
            .setTitle("Изменение цвета текста")
            .setMessage(R.string.changeColorCardInfo, 4f)
            .setPositiveButton("Простой")
            {
                dataInterfaceCard.actionTextColor(false)
                updateTextColor(outputInfo = false)
            }
            .setAdditionalButton("Инверсия")
            {
                dataInterfaceCard.actionTextColor(true)
                updateTextColor(outputInfo = true)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun changeTextSize()
    {
        this.flagChange = !this.flagChange

        val inputInfoSize = AppCompatTextView(requireContext())
        val inputSize = AppCompatSeekBar(requireContext())

        inputInfoSize.textSize = 16f
        inputInfoSize.setTextColor(Color.WHITE)
        inputInfoSize.gravity = Gravity.CENTER
        inputInfoSize.text = "Размер текста: 19"

        inputSize.max = 30
        inputSize.progress = dataInterfaceCard
            .actionTextSize().toInt()

        inputSize.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {
                if (progress < 15)
                {
                    seekBar!!.progress = 15
                    inputInfoSize.text = "Минимальный размер текста"
                    return
                }
                inputInfoSize.text = "Размер текста: $progress"
                this@SettingsInterfaceFragment.cardNameTV.textSize = progress.toFloat()
                this@SettingsInterfaceFragment.cardNumberTV.textSize = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?)
            { if (seekBar!!.progress < 15) seekBar.progress = 15 }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        CustomAppDialog(requireContext())
            .buildEntityDialog(true)
            .setTitle("Изменение размера текста")
            .setViews(inputSize, inputInfoSize)
            .setSizeMainContainer(2.5f)
            .setPositiveButton("Сохранить")
            {
                if (inputSize.progress == this.cardNameTV.textSize.toInt())
                    return@setPositiveButton

                this.flagChange = !this.flagChange
                this.dataInterfaceCard.actionTextSize(
                    inputSize.progress.toFloat())

                Toast.makeText(requireContext()
                    , requireContext().getString(R.string.succsessChangeTextSize)
                    , Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Назад", null)
            .setActionForCancer {
                if (this.flagChange)
                {
                    this.flagChange = !this.flagChange
                    val textSize = this.dataInterfaceCard.actionTextSize()

                    this.cardNameTV.textSize = textSize
                    this.cardNumberTV.textSize = textSize
                }}.show()
    }

    private fun changeBorderCard()
    {
        this.flagChange = !this.flagChange
        val seekBarChange = AppCompatSeekBar(requireContext())
        val textInfoChange = AppCompatTextView(requireContext())

        textInfoChange.gravity = Gravity.CENTER
        textInfoChange.textSize = 16f
        textInfoChange.setTextColor(Color.WHITE)
        textInfoChange.text = "Степень закругления:" +
                " ${dataInterfaceCard.actionRoundBorderCard()}"

        seekBarChange.max = 80
        seekBarChange.progress = dataInterfaceCard
            .actionRoundBorderCard().toInt()

        seekBarChange.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener
        {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {
                textInfoChange.text = "Степень закругления: $progress"
                changeBorderNow(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

            private fun changeBorderNow(progress: Int)
            {
                this@SettingsInterfaceFragment.designCardIM.setBorderRadius(progress.toFloat())
                this@SettingsInterfaceFragment.designCardIM.invalidate()
                this@SettingsInterfaceFragment.cardSize.changeShadowCard(progress.toFloat())
            }
        })

        CustomAppDialog(requireContext())
            .buildEntityDialog(true)
            .setTitle("Изменение закругления границ")
            .setViews(seekBarChange, textInfoChange)
            .setSizeMainContainer(2.5f)
            .setPositiveButton("Применить")
            {
                this.flagChange = !this.flagChange
                this.dataInterfaceCard.actionRoundBorderCard(
                    seekBarChange.progress.toFloat())

                Toast.makeText(requireContext()
                    , requireContext().getString(R.string.succsessChangeBorderCard)
                    , Toast.LENGTH_SHORT).show()
            }
            .setActionForCancer { if (this.flagChange)
            {
                this.designCardIM.setBorderRadius(20f)
                this.designCardIM.invalidate()
                this.flagChange = !this.flagChange
            }}
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun returnDefaultSettings()
    {
        CustomAppDialog(requireContext())
            .buildEntityDialog(true)
            .setTitle("Подтверждение действия")
            .setMessage(R.string.askDefaultSettings, CustomAppDialog.DEFAULT_MESSAGE_SIZE)
            .setPositiveButton("Да")
            {
                this.dataInterfaceCard.actionDefaultSettings(itsAppInterface = false)
                setDefaultCard(setDefault = true)

                Toast.makeText(requireContext()
                    , requireContext().getString(R.string.succsessDefaultSettings)
                    , Toast.LENGTH_LONG)
                    .show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, mode: Boolean)
    {
        if (!setAnimationsCard())
        {
           Toast.makeText(requireContext()
               , requireContext().getString(R.string.warningSetAnimCards)
               , Toast.LENGTH_LONG).show()

            buttonView!!.isChecked = !mode
            return
        }

        this.dataInterfaceCard.actionAnimSelectCard(mode)
        BasicAppActivity.animationsWithCards = mode

        Toast.makeText(requireContext()
            , "Анимация выбора карт успешно ${if (mode) "включена" else "выключена"}"
            , Toast.LENGTH_LONG).show()
    }

    private fun updateTextColor(outputInfo: Boolean)
    {
        val textColor =
            Color.parseColor(if (this.dataInterfaceCard.actionTextColor()) "#02bdbd" else "#FFFFFF")

        this.cardNameTV.setTextColor(textColor)
        this.cardNumberTV.setTextColor(textColor)

        if (outputInfo)
        Toast.makeText(requireContext()
            , requireContext().getString(R.string.successUpdateTextColor)
            , Toast.LENGTH_SHORT).show()
    }

    override fun setAnimationsCard() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}