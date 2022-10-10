package com.example.mcard.repository.features.rest.firebase

import com.example.mcard.repository.features.rest.firebase.FirebaseController.Companion.USERS_INFO
import com.example.mcard.repository.models.storage.CardWithHistoryEntity
import com.example.mcard.repository.features.storage.database.source.QuestLocaleDataSource
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.example.mcard.repository.features.unitAction
import com.example.mcard.repository.models.storage.CardEntity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import javax.inject.Inject

internal class CaretakerCardsFirebase(
    private val firebaseController: FirebaseController,
) {
    @Inject
    lateinit var questLocaleDataSource: QuestLocaleDataSource

    @Inject
    lateinit var userPreferences: UserPreferences

    init {
        firebaseController.getApplicationModule() inject this
    }

    fun launchCaretaker(
        emptyListAction: unitAction,
        updateListAction: unitAction,
    ) = firebaseController
        .firebaseAuth.uid
        ?.let { uid ->
            firebaseController.generalUserReference.child(uid).child(USERS_INFO)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        MainScope().launch(Dispatchers.Main) {

                            mutableListOf<CardWithHistoryEntity>().let { listFireBase ->

                                withContext(MainScope().coroutineContext + Dispatchers.IO) {

                                    for (singleSnapshot in snapshot.children)
                                        if (singleSnapshot.value.toString() != "null")
                                            singleSnapshot.getTransformationModel().run {
                                                if (this.cardEntity.name != null)
                                                    listFireBase.add(
                                                        this
                                                    )
                                            }
                                }.apply {
                                    if (listFireBase.size == 0)
                                        emptyListAction.invoke()

                                    if (userPreferences.syncUserCardsData()) {
                                        syncByLocale()
                                        userPreferences.syncUserCardsData(false)
                                    }

                                    withContext(Dispatchers.IO) {
                                        listFireBase.forEach {
                                            questLocaleDataSource.insertCard(it)
                                        }

                                        firebaseController
                                            .questLocaleDataSource
                                            .loadCards()
                                            .forEach {
                                                if (!listFireBase.contains(it))
                                                    questLocaleDataSource.deleteCard(
                                                        it
                                                    )
                                            }

                                        updateListAction.invoke()
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

    private fun DataSnapshot.getTransformationModel() =
        if (this.hasChild("cardEntity"))
            getValue(
                CardWithHistoryEntity::class.java
            )!!
        else
            CardWithHistoryEntity(
                cardEntity = getValue(
                    CardEntity::class.java
                )!!
            )

    suspend fun syncByLocale() =
        CoroutineScope(Dispatchers.IO).async {
            firebaseController
                .questLocaleDataSource
                .loadCards()
                .apply {
                    firebaseController.generalUserReference.child(
                        firebaseController.firebaseAuth.uid ?: return@apply
                    ).child(USERS_INFO)
                        .setValue(firebaseController.questLocaleDataSource.loadCards())
                }
        }.await()
}