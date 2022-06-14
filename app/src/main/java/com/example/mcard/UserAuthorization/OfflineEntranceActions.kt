@file:Suppress("DEPRECATION")

package com.example.mcard.UserAuthorization

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.mcard.R
import com.example.mcard.SideFunctionality.CustomAppDialog
import com.example.mcard.StorageAppActions.SharedPreferencesManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
internal interface OfflineEntranceActions
{
    @JvmDefault
    fun checkOfflineEntrance(sharedPreferencesManager: SharedPreferencesManager) =
        sharedPreferencesManager.setUserData(
            null, null) != "- -"

    @JvmDefault
    fun viewOffController(vararg views: Any, onClickListener: View.OnClickListener) =
        GlobalScope.launch(Dispatchers.Main)
        {
            val shadowColor = ColorStateList.valueOf(
                Color.parseColor("#807C7B7B"))

            views.forEach { singleView ->
                if (singleView is MenuItem)
                {
                    singleView.isChecked = true
                    singleView.setOnMenuItemClickListener {
                        onClickListener.onClick(null)
                        false
                    }
                }
                else
                {
                    (singleView as View).also {
                        it.backgroundTintList = shadowColor
                        it.setOnClickListener(onClickListener)
                    }
                }
            }
        }.start()

    @JvmDefault
    fun FragmentActivity.showOfferDialogRegistration() =
        GlobalScope.launch(Dispatchers.Main)
        {
            CustomAppDialog(this@showOfferDialogRegistration)
                .buildEntityDialog(true)
                .setTitle(this@showOfferDialogRegistration.getString(R.string.titleDialogReception))
                .setMessage(R.string.waningMessageRegister, 2f)
                .setPositiveButton(this@showOfferDialogRegistration.getString(R.string.registerDialogBtn))
                {
                    if (this@showOfferDialogRegistration
                            .supportFragmentManager
                            .fragments
                            .isNotEmpty())
                        this@showOfferDialogRegistration.onBackPressed()

                    this@showOfferDialogRegistration
                        .supportFragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.start_test, R.anim.finish_test)
                        .replace(R.id.main_linear, RegisterAppFragment())
                        .commit()
                }
                .setNegativeButton("Назад")
                .show()
        }

    @JvmDefault
    fun getClassContext(): AppCompatActivity? = null
}