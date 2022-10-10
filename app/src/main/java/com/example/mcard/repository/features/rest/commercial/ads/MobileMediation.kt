package com.example.mcard.repository.features.rest.commercial.ads

/*
import android.content.Context
import com.example.mcard.R
import com.example.mcard.repository.source.usage.UsageDialogFragment
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.MobileAds
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import kotlinx.coroutines.*

internal object MobileMediation {

     infix fun Context.buildAndShowAd(
         loadingDialog: UsageDialogFragment
     ) {
        loadingDialog.show()

        MobileAds.initialize(this.applicationContext)
        {
            RewardedAd(this.applicationContext).apply {
                setAdUnitId(getString(R.string.YANDEX_REWARDED_UID))
                setRewardedAdEventListener(object : RewardedAdEventListener {
                    override fun onAdLoaded() =
                        this@apply.show()

                    override fun onAdFailedToLoad(adRequestError: AdRequestError) {
                        hideLoadingDialog()
                    }

                    override fun onRewarded(reward: Reward) {
                        hideLoadingDialog()
                    }

                    private fun hideLoadingDialog() =
                        MainScope().launch(Dispatchers.Main) {
                            loadingDialog.hide()
                        }

                    override fun onImpression(impressionData: ImpressionData?) {}
                    override fun onAdShown() {}
                    override fun onAdDismissed() {}
                    override fun onAdClicked() {}
                    override fun onLeftApplication() {}
                    override fun onReturnedToApplication() {}
                })
            }.loadAd(AdRequest.Builder().build())
        }
    }
}*/
