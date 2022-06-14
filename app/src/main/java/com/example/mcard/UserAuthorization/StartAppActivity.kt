package com.example.mcard.UserAuthorization

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.example.mcard.BasicAppActivity
//import com.example.mcard.CommercialAction.YandexADS.YandexMetrica
import com.example.mcard.FragmentsAdditionally.PersonalProfileFragment
import com.example.mcard.FunctionalInterfaces.AnimationsController
import com.example.mcard.GeneralInterfaceApp.ThemeAppController
import com.example.mcard.R
import com.example.mcard.SideFunctionality.GeneralStructApp
import com.example.mcard.StorageAppActions.DataInterfaceCard
import com.example.mcard.StorageAppActions.SharedPreferencesManager
//import com.yandex.metrica.push.YandexMetricaPush
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@DelicateCoroutinesApi
internal class StartAppActivity : AppCompatActivity()
 , GeneralStructApp
 , AnimationsController
{
    private lateinit var animLoadText: Animation
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_app)

        findObjects()
        drawableView()
        basicWork()
    }

    override fun findObjects()
    {
        this.animLoadText = AnimationUtils.loadAnimation(
            this, R.anim.light_selected_btn)
        animLoadText.repeatCount = 100
        this.sharedPreferencesManager = SharedPreferencesManager(this)
    }

    @SuppressLint("SetTextI18n")
    override fun drawableView()
    {
        val themeAppController =
            ThemeAppController(
                DataInterfaceCard(this))

        themeAppController.changeDesignDefaultView(
            findViewById(R.id.main_linear2))

        val userName = PersonalProfileFragment
            .getUserName(baseContext)
        val getView = findViewById<AppCompatTextView>(R.id.hello_text)

        if (!themeAppController.settingsText(getView
            , "Добро пожаловать" +
                    (if (!userName.equals("Пользователь")) ", $userName!" else "!")))
            getView.setTextColor(Color.WHITE)
        getView.startAnimation(animLoadText)

        findViewById<AppCompatImageView>(R.id.loading)
            .startAnimation(AnimationUtils.loadAnimation(
                baseContext, R.anim.anim_loading))

        findViewById<TextView>(R.id.loadingText)
            .startAnimation(animLoadText)
        setAnimationsCard()
    }

    override fun basicWork()
    {
        // this is indicative project
        /*YandexMetrica().onReceive(
            this, Intent(
                YandexMetricaPush.OPEN_DEFAULT_ACTIVITY_ACTION))*/

        Executors.newSingleThreadScheduledExecutor().schedule(
            {
                startActivity(if (this.sharedPreferencesManager.checkCreateUserdata())
                Intent(this, BasicAppActivity::class.java)
                else Intent(this, ReceptionAppActivity::class.java))
            }, 1000, TimeUnit.MILLISECONDS)
    }

    override fun setAnimationsCard(): Boolean
    {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N)
            DataInterfaceCard(this)
                .actionAnimSelectCard(false)
        return true
    }
}