package com.example.mcard.repository.features.rest.firebase

import android.content.Context
import com.example.mcard.BasicApplication
import com.example.mcard.repository.features.storage.database.source.QuestLocaleDataSource
import com.example.mcard.repository.features.storage.preferences.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

internal class FirebaseController(
    internal val context: Context,
) {
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firebaseConnection: FirebaseDatabase

    @Inject
    lateinit var firebaseFirestore: FirebaseFirestore

    @Inject
    lateinit var generalUserReference: DatabaseReference

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var questLocaleDataSource: QuestLocaleDataSource

    @Inject
    lateinit var globalManagerDB: GlobalFirebase

    @Inject
    lateinit var personalFirebase: PersonalFirebase

    fun getApplicationModule() =
        (context as BasicApplication)
            .modulesComponent

    companion object {
        const val MY_SUBSCRIBERS = "SUBSCRIBERS"
        const val ACCOUNT_ID = "ACCOUNT ID"
        const val SCORE_PUBLIC_CARD = "SCORE PUBLIC CARDS"
        const val DATA_CARD = "DATA CARDS"
        const val USERS_INFO = "USERS INFO"
        const val DATA_REGISTER = "DATA CREATE ACCOUNT"
        const val CARD_OFFER = "CARD_OFFER"
        const val FIRESTORE_CARDS = "global_cards"

        const val GLOBAL_DATA_NAME = "GENERAL GLOBAL DATA"
        const val GLOBAL_LIST_CARD = "CARD LIST"
        const val CARDS = "CARDS"
        const val GLOBAL_LIST_ALL_USERS = "ALL USERS"
        const val STATUS_ACCOUNT_ONLINE = "ACTIVE USERS NOW"
        const val LINK_FIREBASE_STORAGE = "gs://mcarddb-3d9fd.appspot.com/"

        const val DATE_FORMAT = "dd.MM.yyyy"
        const val DELIMITER = "-"
        const val SUBSCRIBE_DATA = "SUBSCRIBE ACCOUNT INFO"
        const val FIND_GEO_SCORE = "FIND GEOLOCATION"
        const val SUBSCRIBE_NONE = "-"

        const val PERSONAL_USERS = "USER INFO"
        const val FRIENDS_LIST = "MY FRIENDS"
        const val BLACK_LIST = "BLACK LIST"
        const val PERSONAL_USER_NAME = "nickname"
        const val USER_ROLE = "ROLE"
        const val LINK_DESIGN_CARDS = "${LINK_FIREBASE_STORAGE}storage card design/"
    }
}