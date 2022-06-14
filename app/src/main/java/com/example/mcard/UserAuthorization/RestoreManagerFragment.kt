package com.example.mcard.UserAuthorization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import com.example.mcard.GroupServerActions.BasicFireBaseManager
import com.example.mcard.GlobalListeners.NetworkListener
import com.example.mcard.R
import com.example.mcard.SideFunctionality.CustomAppDialog
import com.example.mcard.SideFunctionality.GeneralStructApp
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
internal class RestoreManagerFragment : Fragment(), GeneralStructApp
{
    private lateinit var viewX: View
    private lateinit var inputAccountID: AppCompatEditText
    private lateinit var inputAccountMail: AppCompatEditText
    private lateinit var btnComplete: AppCompatButton

    private lateinit var basicFireBaseManager: BasicFireBaseManager
    private lateinit var customAppDialog: CustomAppDialog

    override fun onCreateView(inflater: LayoutInflater
     , container: ViewGroup?
     , savedInstanceState: Bundle?): View
    {
        this.viewX = inflater.inflate(R.layout.fragment_restore_manager
            , container
            , false)

        findObjects()
        drawableView()
        basicWork()

        return this.viewX
    }

    override fun findObjects()
    {
        this.inputAccountID = viewX.findViewById(R.id.inputAccountName)
        this.inputAccountMail = viewX.findViewById(R.id.inputLogin)
        this.btnComplete = viewX.findViewById(R.id.btnComplete)

        this.basicFireBaseManager = BasicFireBaseManager(
            null
                , null
                , null
                , requireContext())
        this.customAppDialog =
            CustomAppDialog(requireContext())
    }

    override fun drawableView()
    {
        this.inputAccountMail.setCompoundDrawablesWithIntrinsicBounds(null
            , null
            , AppCompatResources.getDrawable(requireContext(), R.drawable.login_icon)
            , null)

        this.inputAccountID.setCompoundDrawablesWithIntrinsicBounds(null
            , null
            , AppCompatResources.getDrawable(requireContext(), R.drawable.icon_account_id)
            , null)
    }

    override fun basicWork()
    {
       this.btnComplete.setOnClickListener {
           it.startAnimation(
               AnimationUtils.loadAnimation(
                   requireContext(), R.anim.select_object))

           if (NetworkListener.getStatusNetwork())
           {
               if (this.inputAccountID.text.toString().isEmpty()
                   || this.inputAccountMail.text.toString().isEmpty())
                       return@setOnClickListener

               this.customAppDialog.showLoadingDialog(
                   true, "Проверка данных..")

               this.basicFireBaseManager.restoreAccount(
                   this.customAppDialog
                   , this.inputAccountID.text.toString()
                   , this.inputAccountMail.text.toString())

               this.inputAccountID.setText("")
               this.inputAccountMail.setText("")
           }
           else
               Toast.makeText(requireContext()
               , requireContext().getString(R.string.ErrorNetwork)
               , Toast.LENGTH_SHORT)
               .show()
       }
    }
}