package com.example.mcard.presentation.controllers.basic.diverse

import android.view.MenuItem
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.FragmentActivity
import com.example.mcard.R
import com.example.mcard.databinding.IncludeDrawerMenuBinding
import com.example.mcard.presentation.controllers.HostActivity
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.example.mcard.repository.features.getModuleAppComponent
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.features.rest.firebase.AdditionAuthFirebase
import com.example.mcard.repository.features.utils.ApplicationTheme.applyTheme
import com.example.mcard.repository.source.usage.DrawerUsageSource
import com.google.android.material.navigation.NavigationView
import javax.inject.Inject

internal class DrawerController(
    private val drawerView: DrawerLayout,
    private val drawerMenuBinding: IncludeDrawerMenuBinding,
    private val slideView: View? = null,
) : NavigationView.OnNavigationItemSelectedListener,
    DrawerListener,
    DrawerUsageSource {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var additionAuthFirebase: AdditionAuthFirebase

    private val hostNavController by lazy {
        (drawerView.context as? HostActivity)?.navController
    }

    private val context = drawerView.context

    init {
        (context as? FragmentActivity)
            ?.getModuleAppComponent()
            ?.inject(this)

        drawerMenuBinding.modeButton.setImageResource(
            if (!userPreferences.themeApp())
                R.drawable.ic_mode_light
            else
                R.drawable.ic_mode_night
        )
    }

    override fun onNavigationItemSelected(inputSelectedItem: MenuItem): Boolean {
        this.drawerView.close()

        hostNavController?.navigateTo(

            when (inputSelectedItem.itemId) {

                R.id.btnSettings ->
                    R.id.settingsFragment

                R.id.btnPaymentAction ->
                    R.id.additionallyFragment

                R.id.btnSupport -> {
                    CustomDialogBuilder(context)
                        .setTitle(R.string.left_menu_support)
                        .setMessage(R.string.supportFragmentUnvaible)
                        .build()
                        .show()
                    return false
                }

                else -> return false
            }
        )
        return false
    }

    override fun onDrawerOpened(drawerView: View) {

        drawerMenuBinding.modeButton.setOnClickListener {

            userPreferences.themeApp().run {

                userPreferences.themeApp(!this)

                applyTheme(
                    !this, drawerMenuBinding.modeButton
                )
            }
        }

        drawerMenuBinding.exitButton.setOnClickListener {

            CustomDialogBuilder(context)
                .setTitle(R.string.exitAccountTitle)
                .setMessage(R.string.exitAccountMessage)
                .setNegativeAction { }
                .setPositiveAction {
                    additionAuthFirebase.exitAccount {
                        (context as? HostActivity)
                            ?.navController
                            ?.navigateTo(R.id.launchFragment)
                    }
                }.build().show()
        }
    }

    override fun onDrawerClosed(drawerView: View) {}

    override fun onDrawerStateChanged(newState: Int) {}

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        slideView?.translationX =
            drawerView.width * slideOffset
    }

    override fun lockDrawer() {
        drawerView.setDrawerLockMode(
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )
    }

    override fun unlockDrawer() {
        drawerView.setDrawerLockMode(
            DrawerLayout.LOCK_MODE_UNLOCKED
        )
    }

    override fun show() {
        this.drawerView.open()
    }

    override fun hide() {
        this.drawerView.close()
    }
}