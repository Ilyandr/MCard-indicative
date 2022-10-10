package com.example.mcard.repository.features.rest.firebase

import com.example.mcard.presentation.views.custom.CustomDialog
import com.example.mcard.R
import kotlin.jvm.Synchronized
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import com.example.mcard.repository.features.connection.NetworkListener.Companion.statusNetwork
import com.example.mcard.repository.features.messageAction
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.GLOBAL_DATA_NAME
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.GLOBAL_LIST_ALL_USERS
import com.example.mcard.repository.features.unitAction
import com.example.mcard.repository.features.utils.DesignCardManager.removeCacheApp
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class AdditionAuthFirebase(
    private val firebaseController: FirebaseController,
) {
    internal fun exitAccount(
        action: unitAction,
    ) =
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                clearLocaleCache()
            }.run {
                firebaseController.firebaseAuth.signOut()
                action.invoke()
            }
        }


    private suspend fun clearLocaleCache() =
        run {
            firebaseController.questLocaleDataSource.removeDatabase()
            firebaseController.userPreferences.removeAllData()
            firebaseController.context.removeCacheApp()
        }

    fun removeAccount(
        faultAction: messageAction,
        successAction: unitAction,
    ) {
        if (!statusNetwork) {
            faultAction.invoke(R.string.offlineNetworkMSG)
            return
        }

        firebaseController
            .userPreferences
            .setUserData(null, null).let { dataUser ->

                firebaseController.firebaseAuth.currentUser
                    ?.reauthenticate(
                        EmailAuthProvider
                            .getCredential(
                                dataUser.split(" ".toRegex()).toTypedArray()[0],
                                dataUser.split(" ".toRegex()).toTypedArray()[1]
                            )
                    )?.addOnSuccessListener {

                        CoroutineScope(Dispatchers.IO).launch {
                            clearLocaleCache()
                        }

                        firebaseController.userPreferences.accountId(null)?.let { it1 ->

                            firebaseController.firebaseConnection
                                .getReference(GLOBAL_DATA_NAME)
                                .child(GLOBAL_LIST_ALL_USERS)
                                .child(it1)
                                .removeValue()
                                .addOnSuccessListener {

                                    firebaseController.firebaseAuth.uid?.let { it2 ->
                                        FirebaseStorage
                                            .getInstance()
                                            .reference
                                            .child(it2)
                                            .delete()
                                    }

                                    firebaseController.firebaseAuth.uid?.let { it2 ->
                                        firebaseController.generalUserReference.child(
                                            it2
                                        )
                                            .removeValue()
                                            .addOnSuccessListener {
                                                FirebaseAuth.getInstance()
                                                    .currentUser
                                                    ?.delete()
                                                    ?.addOnCompleteListener {

                                                        firebaseController.firebaseAuth.signOut()
                                                        successAction.invoke()
                                                    }
                                            }.addOnFailureListener {
                                                faultAction.invoke(
                                                    R.string.errorRemoveAccount
                                                )
                                            }
                                    }
                                }
                        }
                    }?.addOnFailureListener {
                        faultAction.invoke(
                            R.string.msgWarningInputData
                        )
                    }
            }
    }

    fun changePassword(
        passwordOld: String,
        passwordNew: String,
        messageAction: messageAction,
    ) {
        if (statusNetwork) {

            EmailAuthProvider
                .getCredential(
                    firebaseController.userPreferences
                        .setUserData(null, null)
                        .split(" ".toRegex()).toTypedArray()[0], passwordOld
                ).let { authCredential ->

                    firebaseController.firebaseAuth.currentUser
                        ?.reauthenticate(authCredential)
                        ?.addOnCompleteListener { task: Task<Void?> ->

                            if (task.isSuccessful) {

                                FirebaseAuth
                                    .getInstance()
                                    .currentUser
                                    ?.updatePassword(passwordNew)
                                    ?.addOnCompleteListener { task1: Task<Void?> ->

                                        if (task1.isSuccessful) {
                                            firebaseController.userPreferences
                                                .setUserData(
                                                    firebaseController.userPreferences.setUserData(
                                                        null, null
                                                    ).split(" ".toRegex()).toTypedArray()[0],
                                                    passwordNew
                                                )

                                            messageAction.invoke(
                                                R.string.successChangePassword
                                            )
                                        } else
                                            messageAction.invoke(
                                                R.string.unknownError
                                            )
                                    }
                            } else
                                messageAction.invoke(
                                    R.string.msgWarningInputPassword
                                )
                        }
                }

        } else
            messageAction.invoke(
                R.string.offlineNetworkMSG
            )
    }

    fun changeLogin(
        loginOld: String,
        loginNew: String,
        messageAction: messageAction,
    ) {
        if (statusNetwork) {
            EmailAuthProvider.getCredential(
                loginOld,
                firebaseController
                    .userPreferences
                    .setUserData(
                        null, null
                    ).split(" ".toRegex()).toTypedArray()[1]
            ).let { credential ->
                firebaseController.firebaseAuth.currentUser
                    ?.reauthenticate(credential)
                    ?.addOnCompleteListener { task: Task<Void?> ->

                        if (task.isSuccessful) {

                            FirebaseAuth
                                .getInstance()
                                .currentUser
                                ?.updateEmail(loginNew)
                                ?.addOnCompleteListener { task1: Task<Void?> ->
                                    if (task1.isSuccessful) {

                                        firebaseController.userPreferences.setUserData(
                                            loginNew,
                                            firebaseController.userPreferences
                                                .setUserData(null, null)
                                                .split(" ".toRegex()).toTypedArray()[1]
                                        )

                                        messageAction.invoke(
                                            R.string.successChangeLogin
                                        )
                                    } else
                                        messageAction.invoke(
                                            R.string.unknownError
                                        )
                                }
                        } else
                            messageAction.invoke(
                                R.string.msgWarningInputData
                            )
                    }
            }
        } else
            messageAction.invoke(
                R.string.offlineNetworkMSG
            )
    }

    @Synchronized
    fun avaibleAdditionalPayment(
        additionalPaymentBtn: AppCompatButton,
        actionForThisBtn: View.OnClickListener,
        loadingDialog: CustomDialog,
    ) {
        if (!statusNetwork) return

        loadingDialog.show()

        firebaseController.firebaseConnection
            .getReference(firebaseController.context.getString(R.string.paymentAccept))
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    loadingDialog.cancel()
                    if (snapshot.getValue(Boolean::class.java)!!) {
                        additionalPaymentBtn.scaleX = 1f
                        additionalPaymentBtn.setOnClickListener(actionForThisBtn)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}