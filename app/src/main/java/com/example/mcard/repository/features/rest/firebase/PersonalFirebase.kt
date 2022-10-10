package com.example.mcard.repository.features.rest.firebase

import android.content.Context
import android.net.Uri
import com.example.mcard.repository.features.connection.NetworkListener.Companion.statusNetwork
import com.example.mcard.R
import android.os.Build
import coil.ImageLoader
import coil.request.ImageRequest
import com.google.firebase.storage.FirebaseStorage
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.LINK_DESIGN_CARDS
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.PERSONAL_USERS
import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.USER_ROLE
import com.example.mcard.repository.features.dataCardImageAction
import com.example.mcard.repository.features.optionally.CardAdaptation.comparisonData
import com.example.mcard.repository.features.unitAction
import com.google.firebase.database.*

internal class PersonalFirebase(
    private val firebaseController: FirebaseController,
) {
    fun checkNewUser() {

        firebaseController.generalUserReference
            .child(firebaseController.firebaseAuth.uid ?: return)
            .child(PERSONAL_USERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val myUID = generationPersonalUID()

                    if (!snapshot.hasChild(myUID)
                        || !snapshot.child(myUID).hasChild(USER_ROLE)
                    )
                        firebaseController.generalUserReference
                            .child(firebaseController.firebaseAuth.uid ?: return)
                            .child(PERSONAL_USERS)
                            .child(myUID)
                            .child(USER_ROLE)
                            .setValue(
                                if (snapshot.childrenCount == 0L)
                                    firebaseController.context.getString(R.string.roleAdmin)
                                else if (snapshot.childrenCount > 3)
                                    firebaseController.context.getString(
                                        R.string.roleAdditional
                                    )
                                else
                                    firebaseController.context.getString(R.string.roleSmallAdmin)
                            )
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    companion object {
        fun generationPersonalUID(): String {
            return (Build.BRAND
                    + Build.BRAND.hashCode() / 2 + ""
                    + System.getProperty("os.version") + ""
                    + (Build.DEVICE.hashCode() % 2 - 5) + ""
                    + (Build.MODEL.hashCode() * 3 + 7)
                    + (Build.DISPLAY.hashCode() + Build.ID.hashCode()))
                .replace("-".toRegex(), "").replace("\\.".toRegex(), "")
        }

        inline fun Context.requestCardImage(
            cardName: String,
            crossinline saveActionFault: unitAction,
            crossinline saveActionSuccess: dataCardImageAction,
        ) {
            if (!statusNetwork)
                saveActionFault.invoke()

            FirebaseStorage
                .getInstance()
                .getReferenceFromUrl(LINK_DESIGN_CARDS)
                .apply {
                    try {
                        listAll().addOnSuccessListener { listResult ->
                            for (singleListResult in listResult.items)
                                if (comparisonData(
                                        singleListResult.name, cardName
                                    )
                                )
                                    this@apply.child(singleListResult.name)
                                        .downloadUrl
                                        .addOnSuccessListener { request: Uri ->
                                            ImageLoader(this@requestCardImage).enqueue(
                                                ImageRequest.Builder(this@requestCardImage)
                                                    .data(request)
                                                    .target {
                                                        saveActionSuccess.invoke(it)
                                                    }.build()
                                            )
                                        }
                                else
                                    saveActionFault.invoke()
                        }
                    } catch (ex: NullPointerException) {
                        saveActionFault.invoke()
                    }
                }
        }
    }
}