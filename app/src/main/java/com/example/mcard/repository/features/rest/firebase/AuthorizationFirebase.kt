package com.example.mcard.repository.features.rest.firebase

import android.annotation.SuppressLint
import com.example.mcard.domain.models.authorization.ReceptionModel
import com.example.mcard.domain.models.authorization.RegistrationModel
import com.example.mcard.R
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.ACCOUNT_ID
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.DATA_CARD
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.DATA_REGISTER
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.GLOBAL_DATA_NAME
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.GLOBAL_LIST_ALL_USERS
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.PERSONAL_USERS
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.SCORE_PUBLIC_CARD
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.STATUS_ACCOUNT_ONLINE
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.SUBSCRIBE_DATA
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.SUBSCRIBE_NONE
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.USERS_INFO
import com.example.mcard.repository.models.other.DataAccountEntity
import com.example.mcard.repository.features.messageAction
import com.example.mcard.repository.features.userDataAction
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

internal class AuthorizationFirebase(
    private val firebaseController: FirebaseController,
) {
    fun registrationAction(
        authData: RegistrationModel.LoadingState,
        successAction: userDataAction,
        faultAction: messageAction,
    ) {
        firebaseController.firebaseConnection
            .getReference(GLOBAL_DATA_NAME)
            .child(GLOBAL_LIST_ALL_USERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                @SuppressLint("SimpleDateFormat")
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (!snapshot.hasChild(authData.accountName!!))
                        firebaseController.firebaseAuth.createUserWithEmailAndPassword(
                            authData.login!!,
                            authData.password!!
                        ).addOnSuccessListener {

                            val inputUserData =
                                DataAccountEntity(authData.login, authData.password)

                            val successListenerAuth =
                                OnSuccessListener { _: Void? ->

                                    firebaseController.generalUserReference.child(
                                        firebaseController.firebaseAuth.uid
                                            ?: return@OnSuccessListener
                                    )
                                        .child(USERS_INFO)
                                        .child(DATA_CARD)
                                        .setValue("null")
                                        .addOnSuccessListener successListener@{

                                            firebaseController.generalUserReference
                                                .child(
                                                    firebaseController.firebaseAuth.uid
                                                        ?: return@successListener
                                                )
                                                .child(SUBSCRIBE_DATA)
                                                .setValue(SUBSCRIBE_NONE)
                                                .addOnSuccessListener {

                                                    firebaseController.generalUserReference.child(
                                                        firebaseController.firebaseAuth.uid!!
                                                    )
                                                        .child(STATUS_ACCOUNT_ONLINE)
                                                        .setValue(0)
                                                        .addOnSuccessListener {

                                                            firebaseController.generalUserReference.child(
                                                                firebaseController.firebaseAuth.uid!!
                                                            )
                                                                .child(DATA_REGISTER)
                                                                .setValue(
                                                                    SimpleDateFormat("dd.MM.yyyy")
                                                                        .format(Calendar.getInstance().time)
                                                                )
                                                                .addOnSuccessListener {
                                                                    firebaseController.generalUserReference.child(
                                                                        firebaseController.firebaseAuth.uid!!
                                                                    )
                                                                        .child(SCORE_PUBLIC_CARD)
                                                                        .setValue(0)
                                                                        .addOnSuccessListener {

                                                                            firebaseController.personalFirebase.checkNewUser()
                                                                            firebaseController.userPreferences.accountId(
                                                                                authData.accountName
                                                                            )

                                                                            firebaseController.generalUserReference.child(
                                                                                firebaseController.firebaseAuth.uid!!
                                                                            )
                                                                                .child(
                                                                                    ACCOUNT_ID
                                                                                )
                                                                                .setValue(
                                                                                    authData.accountName
                                                                                )
                                                                                .addOnSuccessListener {

                                                                                    firebaseController.firebaseConnection.getReference(
                                                                                        GLOBAL_DATA_NAME
                                                                                    )
                                                                                        .child(
                                                                                            GLOBAL_LIST_ALL_USERS
                                                                                        )
                                                                                        .child(
                                                                                            authData.accountName
                                                                                        )
                                                                                        .setValue(
                                                                                            firebaseController.firebaseAuth.uid
                                                                                        )
                                                                                        .addOnSuccessListener {

                                                                                            loadAccountId()

                                                                                            successAction.invoke(
                                                                                                inputUserData
                                                                                            )
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                            firebaseController.generalUserReference
                                .child(
                                    firebaseController.firebaseAuth.uid
                                        ?: return@addOnSuccessListener
                                )
                                .setValue(inputUserData)
                                .addOnSuccessListener(successListenerAuth)
                        }.addOnFailureListener {
                            faultAction.invoke(
                                R.string.errorRegistration
                            )
                        }
                    else
                        faultAction.invoke(
                            R.string.ErrorAccName
                        )
                }

                override fun onCancelled(error: DatabaseError) =
                    faultAction.invoke(
                        R.string.errorRegistration
                    )
            })
    }

    fun entranceAction(
        authData: ReceptionModel.LoadingState,
        successAction: userDataAction,
        faultAction: messageAction,
    ): Task<AuthResult> =
        firebaseController.firebaseAuth
            .signInWithEmailAndPassword(
                authData.login!!, authData.password!!
            ).addOnSuccessListener {

                firebaseController.generalUserReference
                    .child(firebaseController.firebaseAuth.uid ?: return@addOnSuccessListener)
                    .addListenerForSingleValueEvent(
                        object : ValueEventListener {

                            override fun onDataChange(snapshot: DataSnapshot) {

                                snapshot.child(
                                    PERSONAL_USERS
                                ).childrenCount.let { childrenCount ->

                                    val data = DataAccountEntity(
                                        authData.login,
                                        authData.password
                                    )

                                    if (childrenCount < 5)
                                        successCheck(
                                            firebaseController.personalFirebase, data
                                        )
                                    else {
                                        faultAction.invoke(
                                            R.string.warningAuthSub
                                        )
                                        firebaseController.firebaseAuth.signOut()
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) =
                                faultAction.invoke(
                                    R.string.errorAuth
                                )

                            @Synchronized
                            private fun successCheck(
                                syncPersonalCardsManager: PersonalFirebase,
                                data: DataAccountEntity,
                            ) {
                                syncPersonalCardsManager.checkNewUser()
                                setSubscribeAccountData()
                                loadAccountId()
                                successAction.invoke(data)
                            }
                        })
            }.addOnFailureListener {
                faultAction.invoke(
                    R.string.warningIncorrectAuth
                )
            }

    private fun setSubscribeAccountData(): Task<DataSnapshot>? =
        firebaseController.firebaseAuth.uid?.let {
            firebaseController.generalUserReference
                .child(it)
                .child(SUBSCRIBE_DATA)
                .get()
                .addOnSuccessListener { resultSubscribeTask: DataSnapshot ->

                    firebaseController.userPreferences.haveAccountSubscribe(
                        resultSubscribeTask
                            .getValue(String::class.java) != SUBSCRIBE_NONE
                    )
                }
        }

    private fun loadAccountId() =
        firebaseController.firebaseAuth.uid?.let {
            firebaseController
                .generalUserReference
                .child(it)
                .child(ACCOUNT_ID)
                .get()
                .addOnSuccessListener { result ->

                    firebaseController
                        .userPreferences
                        .accountId(
                            result.getValue(
                                String::class.java
                            )
                        )
                }
        }

    fun restoreAccount(
        data: String,
        completeAction: messageAction,
    ) {
        firebaseController.firebaseAuth
            .sendPasswordResetEmail(data)
            .addOnCompleteListener { task: Task<Void?> ->

                completeAction.invoke(
                    if (task.isSuccessful)
                        R.string.restorePasswordAccountInfo
                    else
                        R.string.errorRestoreAccountEmail
                )
            }
    }
}