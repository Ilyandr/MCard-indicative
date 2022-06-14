package com.example.mcard.CommercialAction.YandexADS

import android.content.Context
import android.widget.Toast
import com.example.mcard.GlobalListeners.NetworkListener.Companion.getStatusNetwork
import com.example.mcard.R
import com.example.mcard.SideFunctionality.CustomAppDialog
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DelicateCoroutinesApi
internal object RewardedMobileMediationManager
{
    @JvmStatic
    infix fun Context.buildAndShowAd(inputDataADS: MediationNetworkEntity) =
        CustomAppDialog(this)
            .buildEntityDialog(true)
            .setTitle(getString(R.string.titleInfoActionADS))
            .offExitActionPermission(false)
            .setMessage(inputDataADS.info, CustomAppDialog.DEFAULT_MESSAGE_SIZE)
            .setAdditionalButton()
            .setNegativeButton("Назад")
            {
                inputDataADS
                    .actionForADS
                    ?.second
                    ?.run()
            }
            .setPositiveButton("Далее")
            {
                if (!getStatusNetwork())
                {
                    showErrorToastADS(this)
                    return@setPositiveButton
                }

                val loadingDialogInfo = CustomAppDialog(this)
                loadingDialogInfo.showLoadingDialog(
                    true, getString(R.string.titleLoadingDialogADS))

                MobileAds.initialize(this.applicationContext)
                {
                    RewardedAd(this.applicationContext).apply {
                        setAdUnitId(getString(R.string.YANDEX_REWARDED_UID))
                        setRewardedAdEventListener(object: RewardedAdEventListener
                        {
                            override fun onAdLoaded() =
                                this@apply.show()

                            override fun onAdFailedToLoad(adRequestError: AdRequestError)
                            {
                                hideLoadingDialog()
                                showErrorToastADS(this@buildAndShowAd)
                            }

                            override fun onRewarded(reward: Reward) =
                                this@buildAndShowAd actionForSuccess
                                        {  inputDataADS.actionForADS?.let {
                                                it.first?.run()
                                            } ?: inputDataADS.actionSuccessADS?.run()
                                            this@apply.destroy()
                                            hideLoadingDialog()
                                        }

                            private fun hideLoadingDialog() =
                                GlobalScope.launch(Dispatchers.Main)
                                {
                                    loadingDialogInfo.showLoadingDialog(false)
                                    loadingDialogInfo.cancel()
                                }.start()

                            override fun onImpression(impressionData: ImpressionData?) { }
                            override fun onAdShown() { }
                            override fun onAdDismissed() {}
                            override fun onAdClicked() {}
                            override fun onLeftApplication() {}
                            override fun onReturnedToApplication() {   }
                        })
                    }.loadAd(AdRequest.Builder().build())
                }
            }.show()

    private infix fun Context.actionForSuccess(actionSuccess: Runnable)
    {
        GlobalScope.launch(Dispatchers.Main)
        {
            Toast.makeText(this@actionForSuccess
                , R.string.successWatchADS
                , Toast.LENGTH_LONG)
                .show()
            actionSuccess.run()
        }.start()
    }

    @JvmStatic
    fun showErrorToastADS(context: Context) =
        Toast.makeText(context
            , R.string.errorShowADS
            , Toast.LENGTH_LONG)
            .show()
}