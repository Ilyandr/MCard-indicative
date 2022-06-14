package com.example.mcard.FragmentsAdditionally

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.mcard.GeneralInterfaceApp.CustomAppViews.CustomHeaderListView
import com.example.mcard.GroupServerActions.BasicFireBaseManager
import com.example.mcard.GroupServerActions.SyncGlobalCardsManager
import com.example.mcard.GlobalListeners.NetworkListener
import com.example.mcard.R
import com.example.mcard.SideFunctionality.GeneralStructApp
import com.example.mcard.GeneralInterfaceApp.ThemeAppController
import com.example.mcard.StorageAppActions.DataInterfaceCard
import com.example.mcard.StorageAppActions.SharedPreferencesManager
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
internal class OptionsAppSyncFragment(
 private val changeListViewCustom: CustomHeaderListView)
 : Fragment(), View.OnClickListener, GeneralStructApp
{
    private lateinit var viewX: View
    private lateinit var btnComplete: AppCompatButton
    private lateinit var btnSync: AppCompatTextView
    private lateinit var btnBack: AppCompatImageButton
    private lateinit var synchronizedSwitch: SwitchCompat
    private lateinit var animSelected: Animation
    private lateinit var dataChange: AppCompatTextView
    private lateinit var scoreChange: AppCompatTextView
    private lateinit var sharedpreferencesManager: SharedPreferencesManager
    private lateinit var basicFireBaseManager: BasicFireBaseManager
    private lateinit var syncGlobalCardsManager: SyncGlobalCardsManager

    override fun onCreateView(inflater: LayoutInflater
        , container: ViewGroup?
        , savedInstanceState: Bundle?): View
    {
        this.viewX = inflater.inflate(R.layout.fragment_synchronization, container, false)

        findObjects()
        drawableView()
        basicWork()

        return this.viewX
    }

    override fun findObjects()
    {
        this.btnComplete = viewX.findViewById(R.id.btn_complete)
        this.btnSync = viewX.findViewById(R.id.sync_now)
        this.dataChange = viewX.findViewById(R.id.data_last_change)
        this.scoreChange = viewX.findViewById(R.id.score_change)
        this.synchronizedSwitch = viewX.findViewById(R.id.sync_switch)
        this.btnBack = viewX.findViewById(R.id.btn_back)

        this.animSelected = AnimationUtils.loadAnimation(context, R.anim.select_object)
        this.sharedpreferencesManager = SharedPreferencesManager(requireContext())
        this.basicFireBaseManager = BasicFireBaseManager(requireContext())
        this.syncGlobalCardsManager = SyncGlobalCardsManager(
            requireContext(), this.changeListViewCustom)
    }

    override fun drawableView()
    {
        val themeAppController =
            ThemeAppController(
                DataInterfaceCard(requireContext()))

        themeAppController.changeDesignIconBar(
            viewX.findViewById(R.id.bar_icon))
        themeAppController.changeDesignDefaultView(
            viewX.findViewById(R.id.main_linear_reception))

        synchronizedSwitch.isChecked =
            sharedpreferencesManager.synchronization_mode(null)
        themeAppController.settingsText(
            viewX.findViewById(R.id.name_fragment)
            , "Синхронизация")

        themeAppController.setOptionsButtons(
            btnBack
            , btnSync
            , btnComplete
            , scoreChange
            , dataChange
            , viewX.findViewById(R.id.informerView))
    }

    override fun basicWork()
    {
        this.dataChange.text = "${requireContext().getString(R.string.lastSync)}${sharedpreferencesManager.last_syncServer(null)}"
        this.scoreChange.text = (requireContext().getString(R.string.updateCards)
                + " " + sharedpreferencesManager.score_addCard(null))

        this.btnComplete.setOnClickListener(this)
        this.btnBack.setOnClickListener(this)
        this.btnSync.setOnClickListener(this)

        this.synchronizedSwitch.setOnCheckedChangeListener { _: CompoundButton?, newBooleanValue: Boolean ->
            this.sharedpreferencesManager.synchronization_mode(newBooleanValue)
            Toast.makeText(requireContext()
                , requireContext().getString(R.string.changeSyncMode)
                , Toast.LENGTH_LONG).show()
        }
    }

    private fun start_syncCard()
    {
        if (NetworkListener.getStatusNetwork())
        this.syncGlobalCardsManager
            .basicSyncController(SyncGlobalCardsManager.MODE_SINGLE)

        else Toast.makeText(requireContext()
            , requireContext().getString(R.string.offlineNetworkMSG)
            , Toast.LENGTH_SHORT)
            .show()
    }

    override fun onClick(inputView: View)
    {
        inputView.startAnimation(this.animSelected)
        if (inputView.id == R.id.sync_now) start_syncCard()
        else requireActivity().onBackPressed()
    }
}