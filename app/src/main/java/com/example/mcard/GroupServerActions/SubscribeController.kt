package com.example.mcard.GroupServerActions

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.AppCompatTextView
import com.example.mcard.BasicAppActivity
import com.example.mcard.CommercialAction.SubscribeManagerActivity
import com.example.mcard.CommercialAction.YandexADS.MediationNetworkEntity
import com.example.mcard.CommercialAction.YandexADS.RewardedMobileMediationManager.buildAndShowAd
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView
import com.example.mcard.GeneralInterfaceApp.MasterDesignCard
import com.example.mcard.R
import com.example.mcard.SideFunctionality.CustomAppDialog
import com.example.mcard.StorageAppActions.SharedPreferencesManager
import com.example.mcard.UserLocation.GeolocationFindStarter
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KFunction1

@DelicateCoroutinesApi
internal class SubscribeController(context: Context)
    : BasicFireBaseManager(context)
{
    val accountUID: FirebaseUser?
    = Objects.requireNonNull(FirebaseAuth.getInstance().currentUser)

    internal var actualProgress: Int = 0

    companion object
    {
        private const val DATE_FORMAT = "dd.MM.yyyy"
        private const val DELIMITER = "-"

        const val SUBSCRIBE_DATA = "SUBSCRIBE ACCOUNT INFO"
        const val FIND_GEO_SCORE = "FIND GEOLOCATION"
        const val SUBSCRIBE_NONE = "-"
        const val FIND_GEO_LIMIT_NOSUB = 6
        const val FIND_GEO_LIMIT_SUB = 30

        const val MODE_POST = 1
        const val MODE_GET = 2
        const val MODE_UPDATE_DATE = 3

        @JvmStatic
        @SuppressLint("SimpleDateFormat")
        fun realDate() = SimpleDateFormat(DATE_FORMAT)
            .format(Calendar.getInstance().time).toString()

        fun String.getNormalDate() =
            this.split(DELIMITER)[0]

        @JvmStatic
        fun String.toDate() = SimpleDateFormat(DATE_FORMAT
            , Locale.getDefault()).parse(this)!!.time

        @JvmStatic
        infix fun DatabaseReference.generalSubscribeController(context: Context) =
            this.child(SUBSCRIBE_DATA)
                .addListenerForSingleValueEvent(object: ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        val subscribeData =
                            snapshot.getValue(String::class.java)

                        SharedPreferencesManager(context)
                            .haveAccountSubscribe(
                                subscribeData != DELIMITER)

                            if (subscribeData != DELIMITER
                                && subscribeData != null
                                && (realDate().toDate()
                                        - subscribeData.split(DELIMITER)[0].toDate()) /
                                (24 * 60 * 60 * 1000)
                                > subscribeData.split(DELIMITER)[1].toInt())
                            {
                                this@generalSubscribeController
                                    .child(SUBSCRIBE_DATA)
                                    .setValue(DELIMITER)

                                SharedPreferencesManager(context)
                                    .haveAccountSubscribe(
                                        subscribeData != DELIMITER)
                            }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }

    fun comparingDates(dateServer: String, dateLocale: String) =
    sharedPreferencesManager.haveAccountSubscribe( dateLocale.toDate()
            - dateServer.getNormalDate().toDate()
            <= dateServer.getTimeSubscribe())

    private fun String.getTimeSubscribe() =
        this.split(DELIMITER)[1].toInt()

    private fun setValueSubscribe(
        localeDate: String
        , serverDate: String
        , subscribeDaysCountNow: Int = 0) =
        (localeDate + DELIMITER +
                (serverDate
                .split("мес")[0]
                .replace(" ", "")
                .toInt() * 31
                + subscribeDaysCountNow))

    internal fun outputSubInfo(
        loadingDialog: CustomAppDialog? = null
      , dataView: AppCompatTextView
      , seekBarPay: AppCompatSeekBar? = null
      , textSubInfo: TextView)
    {
        val accountUID = this.accountUID ?: return

        generalUserReference.child(accountUID.uid)
            .child(SUBSCRIBE_DATA)
            .addListenerForSingleValueEvent(object : ValueEventListener
            {
                @SuppressLint("SimpleDateFormat", "SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    loadingDialog?.showLoadingDialog(false)
                    if (snapshot.value.toString() == DELIMITER)
                    {
                        dataView.text =
                            context.getString(R.string.dontHaveSubscribe)
                        return
                    }

                    val instance = Calendar.getInstance()
                    instance.time = Date((
                            snapshot.value as String)
                        .getNormalDate()
                        .toDate())
                    instance.add(Calendar.DAY_OF_MONTH
                        , (snapshot.value as String)
                            .getTimeSubscribe())

                    dataView.text = "Состояние подписки: активна до " +
                            SimpleDateFormat(DATE_FORMAT)
                                .format(instance.time)

                    seekBarPay?.let {
                        val countSubNow = (snapshot.value as String)
                            .split("-")[1].toInt() / 31

                        this@SubscribeController.actualProgress = countSubNow

                        if (countSubNow == 12)
                        {
                            seekBarPay.progress = seekBarPay.max
                            textSubInfo.text = "Максимальный срок подписки."
                            SubscribeManagerActivity.finalPrice = 0
                        }
                        else
                            seekBarPay.max = 12 - this@SubscribeController.actualProgress - 3
                    }
                }
                override fun onCancelled(error: DatabaseError) { return }
            })
    }

    fun checkAddPersonalDesign(): Boolean
    {
        val files = File(
            context.cacheDir.toString())
            .listFiles() ?: return false
        var countCards: Short = 0

        for (i in files.indices)
            if (files[i].name.contains(".jpg")
                && !files[i].name
                    .contains(MasterDesignCard.DOWNLOAD_FILE_POINT))
                        countCards++

        return ((sharedPreferencesManager
            .haveAccountSubscribe(null))
                || (countCards <= 2))
    }

    fun checkFindGEO(WORK_MODE: Int, listView: CustomHeaderListView?)
    {
       val countFind = generalUserReference
           .child(firebaseAuth.uid ?: return)
           .child(FIND_GEO_SCORE)

        val valueListenerCountFind: ValueEventListener = object : ValueEventListener
        {
            override fun onDataChange(getValueFind: DataSnapshot)
            {
                val geoFindData = Gson().fromJson(
                    getValueFind.value.toString()
                    , GeoFindEntity::class.java)

                val subscribeHave: Deferred<Boolean> =
                    GlobalScope.async(Dispatchers.IO) { sharedPreferencesManager
                        .haveAccountSubscribe(null) }

                when(WORK_MODE)
                {
                    MODE_GET ->
                        GlobalScope.launch(Dispatchers.Main)
                        {
                            if (geoFindData.countUseFunction != 0)
                                GeolocationFindStarter.safeLaunchGeoFind(
                                    context as BasicAppActivity)
                            else
                            {
                                listView?.swipeRefreshLayout?.isRefreshing = false
                                this@SubscribeController.context buildAndShowAd MediationNetworkEntity(
                                    info = R.string.warningFindGEO)
                                {
                                    GeolocationFindStarter.safeLaunchGeoFind(
                                        context as BasicAppActivity)
                                }
                            }
                        }.start()

                    MODE_POST ->
                    {
                        Toast.makeText(
                            context
                            , R.string.ResultFindGEOFinal
                            , Toast.LENGTH_LONG)
                            .show()

                        if (geoFindData.countUseFunction != 0)
                            countFind.setValue(
                                GeoFindEntity(
                                    (geoFindData.countUseFunction - 1), realDate()))
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context
                                        , "${context.getText(R.string.ResultFindGEO)}" +
                                                ": ${geoFindData.countUseFunction - 1} раз"
                                        , Toast.LENGTH_LONG)
                                        .show()
                        }
                    }

                    MODE_UPDATE_DATE ->
                        GlobalScope.launch(Dispatchers.IO)
                        {
                            if (realDate().toDate() - geoFindData.dateLastUseFunction.toDate() >= 1)
                                countFind.setValue(GeoFindEntity(
                                    if (subscribeHave.await()) FIND_GEO_LIMIT_SUB
                                    else FIND_GEO_LIMIT_NOSUB
                                    , realDate()))
                            else
                                countFind.setValue(
                                    GeoFindEntity(
                                        geoFindData.countUseFunction, realDate()))
                        }.start()
                }
            }
            override fun onCancelled(error: DatabaseError) { }
        }

        countFind.addListenerForSingleValueEvent(
            valueListenerCountFind)
    }

    fun checkSubscribe(inputMode: Int
        , dataInfo: String? = null
        , subInfo: String? = null
        , updateSubscribe: Boolean? = null
        , outputSubInfo: Runnable? = null)
    {
        val accountUID = this.accountUID ?: return
        if (inputMode == MODE_GET)
        {
            val getSubscribeInfo = generalUserReference
                .child(accountUID.uid)
                .child(SUBSCRIBE_DATA)

            val haveSubscribeEventListener: ValueEventListener = object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    val subInfoGET = snapshot.value.toString()

                    if (subInfoGET != SUBSCRIBE_NONE)
                        comparingDates(subInfoGET, realDate())
                    else
                        sharedPreferencesManager.haveAccountSubscribe(false)
                    removeThisListener()
                }

                override fun onCancelled(error: DatabaseError)
                { removeThisListener() }

                private fun removeThisListener() =
                    getSubscribeInfo.removeEventListener(this)
            }

            getSubscribeInfo.addListenerForSingleValueEvent(
                haveSubscribeEventListener)
        }
        else if (inputMode == MODE_POST)
        {
            generalUserReference.child(accountUID.uid)
                .child(SUBSCRIBE_DATA)
                .addListenerForSingleValueEvent(object: ValueEventListener
                {
                    override fun onDataChange(serverData: DataSnapshot)
                    {
                        generalUserReference.child(accountUID.uid)
                            .child(SUBSCRIBE_DATA)
                            .setValue(setValueSubscribe(dataInfo!!
                                , subInfo!!.toString()
                                , (if (!serverData.getValue(String::class.java).equals(DELIMITER))
                                    serverData.getValue(String::class.java)!!.split(DELIMITER)[1].toInt()
                                else 0)))
                            .addOnCompleteListener { task: Task<Void?> ->
                                if (task.isSuccessful)
                                {
                                    sharedPreferencesManager.haveAccountSubscribe(true)

                                    generalUserReference.child(accountUID.uid)
                                        .child(FIND_GEO_SCORE)
                                        .setValue(GeoFindEntity(FIND_GEO_LIMIT_SUB, realDate()))
                                        .addOnSuccessListener {
                                            outputSubInfo?.run()
                                            Toast.makeText(context
                                                , if (!updateSubscribe!!) "Подписка MCard+ на" +
                                                        subInfo + "успешно оформлена!" else "Подписка MCard+ успешно продлена"
                                                , Toast.LENGTH_LONG).show()
                                        }
                                }
                            }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    fun updateSubscribe(newDateCount: Int
      , callFunctionConfirm: KFunction1<Boolean, Unit>)
    {
        val accountUID = this.accountUID ?: return

        generalUserReference.child(accountUID.uid)
            .child(SUBSCRIBE_DATA)
            .addListenerForSingleValueEvent(object : ValueEventListener
            {
                override fun onDataChange(snapshot: DataSnapshot)
                {
                    if (snapshot.value.toString() != DELIMITER)
                    {
                        val oldCountDateSub = snapshot.getValue(String::class.java)!!
                            .split(DELIMITER)[1]
                            .toInt() / 31

                        if (oldCountDateSub < newDateCount)
                        {
                            SubscribeManagerActivity.finalPrice =
                                (newDateCount - oldCountDateSub)
                            callFunctionConfirm(true)
                        }
                        else Toast.makeText(context
                            , context.getString(R.string.errorSmallDateSub)
                            , Toast.LENGTH_LONG).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) { return }
            })
    }
}