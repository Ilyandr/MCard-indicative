package com.example.mcard.repository.features.rest.firebase

import com.google.firebase.database.DatabaseReference
import com.example.mcard.repository.models.storage.CardEntity
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.example.mcard.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.mcard.repository.features.connection.NetworkListener.Companion.statusNetwork
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.ACCOUNT_ID
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.GLOBAL_DATA_NAME
import com.example.mcard.repository.features.messageAction
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.FIRESTORE_CARDS
import com.example.mcard.repository.features.unitAction
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.NullPointerException

internal class GlobalFirebase(
    private val firebaseController: FirebaseController,
) {
    private val generalGlobalRef: DatabaseReference by lazy {
        firebaseController.firebaseConnection
            .getReference(GLOBAL_DATA_NAME)
    }

    fun publishCard(
        model: CardWithHistoryEntity?,
        faultAction: messageAction,
        successAction: messageAction,
    ) =
        model?.cardEntity?.apply {

            existGlobalCard(
                this,
                faultAction = faultAction,
                removeAction = {
                    model updateLocaleCard true
                    successAction.invoke(it)
                }
            ) {
                this.cardOwner =
                    firebaseController
                        .userPreferences
                        .accountId(null)

                model.cardEntity.run {
                    firebaseController
                        .firebaseFirestore
                        .collection(FIRESTORE_CARDS)
                        .document(uniqueIdentifier)
                        .set(this)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                model updateLocaleCard false
                                successAction.invoke(R.string.msgSuccessPublishCardFirst)
                            } else
                                faultAction.invoke(R.string.unknownError)
                        }
                }
            }
        }

    private infix fun CardWithHistoryEntity.updateLocaleCard(
        isRemoveAction: Boolean,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            cardEntity.cardOwner =
                if (!isRemoveAction)
                    firebaseController.userPreferences.accountId(null)
                else
                    null

            firebaseController.questLocaleDataSource.changeCard(
                this@updateLocaleCard
            )
        }
    }

    private inline fun existGlobalCard(
        cardEntity: CardEntity,
        crossinline faultAction: messageAction,
        crossinline removeAction: messageAction,
        crossinline publishAction: messageAction,
    ) {
        if (!statusNetwork)
            faultAction.invoke(R.string.offlineNetworkMSG)
        else if (firebaseController
                .userPreferences
                .accountId(null) != cardEntity.cardOwner
            && cardEntity.cardOwner != null
        )
            faultAction.invoke(R.string.messageWarningOwnerCard)
        else {
            CoroutineScope(Dispatchers.IO).launch {

                if (!firebaseController
                        .firebaseFirestore
                        .collection(FIRESTORE_CARDS)
                        .document(cardEntity.uniqueIdentifier)
                        .get()
                        .await()
                        .exists()
                )
                    publishAction.invoke(R.string.msgSuccessPublishCardFirst)
                else
                    removeGlobalCard(cardEntity) {
                        removeAction.invoke(R.string.msgSuccessPublishCardSecond)
                    }
            }
        }
    }

    fun removeGlobalCard(
        data: CardEntity,
        faultAction: messageAction? = null,
        action: unitAction,
    ) {
        if (!statusNetwork) {
            if (data.cardOwner == null)
                action.invoke()
            else
                faultAction?.invoke(
                    R.string.errorCancelDeleteCard
                )
        } else if (firebaseController
                .userPreferences
                .accountId(null) == data.cardOwner
        )
            firebaseController
                .firebaseFirestore
                .collection(FIRESTORE_CARDS)
                .document(data.uniqueIdentifier)
                .delete()
                .addOnSuccessListener {
                    action.invoke()
                }
        else
            action.invoke()
    }

    companion object {
        infix fun FirebaseController.avaibleAccountListener(
            action: unitAction,
        ) {
            try {
                FirebaseDatabase
                    .getInstance()
                    .getReference(context.getString(R.string.TableNameFirebase))
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.hasChild(ACCOUNT_ID))
                                action.invoke()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            action.invoke()
                        }
                    })
            } catch (accountDeleted: NullPointerException) {
                action.invoke()
            }
        }
    }
}