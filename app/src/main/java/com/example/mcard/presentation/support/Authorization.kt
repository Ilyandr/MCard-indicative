package com.example.mcard.presentation.support

import androidx.activity.addCallback
import com.example.mcard.domain.viewModels.authorization.ReceptionViewModel
import com.example.mcard.domain.viewModels.authorization.RegistrationViewModel
import com.example.mcard.domain.viewModels.authorization.RestoreViewModel
import com.example.mcard.presentation.controllers.authorization.ReceptionFragment
import com.example.mcard.presentation.controllers.authorization.RegistrationFragment
import com.example.mcard.presentation.controllers.authorization.RestoreFragment
import com.example.mcard.presentation.views.custom.CustomDialogBuilder
import com.example.mcard.R
import com.example.mcard.repository.features.connection.setOnClickListenerWithConnection
import com.example.mcard.repository.features.navigateTo
import com.example.mcard.repository.features.requireDrawerLayout
import com.example.mcard.databinding.ActivityReceptionBinding
import com.example.mcard.databinding.FragmentRegisterBinding
import com.example.mcard.databinding.FragmentRestoreBinding
import com.example.mcard.presentation.views.other.initViewToolBar

internal fun FragmentRegisterBinding.setAuthorizationActions(
    viewModel: RegistrationViewModel,
    view: RegistrationFragment,
) {
    barLayout.root.initViewToolBar()
    barLayout.root.setTitle(R.string.registerTitle)


    this.btnRegistration.setOnClickListenerWithConnection(
        view.animSelected
    ) {
        viewModel.launchRegistrationAction(
            this.accountIdEditText.text.toString(),
            this.loginEditText.text.toString(),
            this.passwordEditText.text.toString()
        )
    }
}

internal fun ActivityReceptionBinding.setRegistrationActions(
    fragment: ReceptionFragment,
    viewModel: ReceptionViewModel,
) {
    fragment.requireActivity().let { activity ->

        fragment.requireActivity()
            .requireDrawerLayout()
            ?.lockDrawer()

        activity.onBackPressedDispatcher
            .addCallback(fragment) {
                activity.finish()
            }

        btnOfflineEntrance.setOnClickListener {
            it.startAnimation(fragment.animSelected)
            CustomDialogBuilder(activity)
                .setTitle(R.string.titleDialogReception)
                .setMessage(R.string.infoDialogOfflineEntrance)
                .setNegativeAction { }
                .setPositiveAction {
                    viewModel.loggedNoneAuthAction()
                }.build().show()
        }

        btnRegister.setOnClickListener {
            it.startAnimation(fragment.animSelected)
            this.root.navigateTo(R.id.launchRegisterFragment)
        }

        restoreAccount.setOnClickListener {
            it.startAnimation(fragment.animSelected)
            this.root.navigateTo(R.id.launchRestoreAccount)
        }

        btnStartApp.setOnClickListenerWithConnection(
            fragment.animSelected
        ) {
            viewModel.loggedAuthAction(
                nameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }
}

internal fun FragmentRestoreBinding.setRestoreActions(
    viewModel: RestoreViewModel,
    view: RestoreFragment,
) {
    barLayout.root.initViewToolBar()
    barLayout.root.setTitle(R.string.restoreTitle)

    this.btnRestore.setOnClickListenerWithConnection(
        view.animationSelected
    ) {
        viewModel.launchRestoreAction(
            loginEditText.text.toString(),
        )
    }
}