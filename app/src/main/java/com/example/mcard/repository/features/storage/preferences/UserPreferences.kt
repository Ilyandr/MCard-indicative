package com.example.mcard.repository.features.storage.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.mcard.repository.features.optionally.CardsSorting
import java.lang.Exception

internal open class UserPreferences(context: Context) {
    protected val sharedPreferences: SharedPreferences
    protected val editor: SharedPreferences.Editor

    companion object {
        private const val USER_LOGIN = "login"
        private const val USER_PASSWORD = "password"
    }

    init {
        val NAME_DATA = "USER INFO AUTH"
        sharedPreferences = context.getSharedPreferences(
            NAME_DATA, Context.MODE_PRIVATE
        )
        editor = sharedPreferences.edit()
    }

    fun setUserData(login: String?, password: String?): String {
        var info = " "
        if (login != null && password != null) {
            editor.remove(USER_LOGIN).apply()
            editor.remove(USER_PASSWORD).apply()
            editor.putString(USER_LOGIN, login).apply()
            editor.putString(USER_PASSWORD, password).apply()
        } else info = sharedPreferences.getString(
            USER_LOGIN, ""
        ) + " " +
                sharedPreferences.getString(USER_PASSWORD, null)
        return info
    }

    fun existData() = (this.sharedPreferences.contains(USER_LOGIN)
            && this.sharedPreferences.contains(USER_PASSWORD))

    fun check_update_localDB(update: Boolean?): Boolean {
        var updateCopy = update
        val USER_UPDATE_FIREBASE = "UPDATE"

        if (updateCopy != null) {
            try {
                editor.remove(USER_UPDATE_FIREBASE).apply()
            } catch (ignored: Exception) {
            }
            editor.putBoolean(USER_UPDATE_FIREBASE, updateCopy).apply()
        }
        updateCopy = sharedPreferences.getBoolean(USER_UPDATE_FIREBASE, false)
        return updateCopy
    }

    fun iconProfile(set_path: String?): String? {
        var setPathCopy = set_path
        val USER_IMAGE_ICON = "ICON"

        if (setPathCopy != null) {
            try {
                editor.remove(USER_IMAGE_ICON).apply()
            } catch (ignored: Exception) {
            }
            editor.putString(USER_IMAGE_ICON, setPathCopy).apply()
        } else setPathCopy = sharedPreferences.getString(USER_IMAGE_ICON, null)
        return setPathCopy
    }

    fun nameUserProfile(set_name: String?): String? {
        val USER_DATA_NAME = "NAME"

        if (set_name != null) {
            try {
                editor.remove(USER_DATA_NAME).apply()
            } catch (ignored: Exception) {
            }
            editor.putString(USER_DATA_NAME, set_name).apply()
        }
        return sharedPreferences.getString(USER_DATA_NAME, null)
    }

    fun updateUserLocaleNickname(set_name: Boolean?): Boolean {
        var setNameCopy = set_name
        val USER_DATA_NAME = "NAME_LOCALE"

        if (setNameCopy != null) {
            try {
                editor.remove(USER_DATA_NAME).apply()
            } catch (ignored: Exception) {
            }
            editor.putBoolean(USER_DATA_NAME, setNameCopy).apply()
        } else
            setNameCopy = sharedPreferences.getBoolean(
                USER_DATA_NAME, false
            )
        return setNameCopy
    }

    fun sortedCardInfo(sortType: Int? = null): Int {
        if (sortType != null) {
            try {
                editor.remove("SORTING_TYPE").apply()
            } catch (ignored: Exception) {
            }
            editor.putInt("SORTING_TYPE", sortType).apply()
        }

        return sharedPreferences.getInt(
            "SORTING_TYPE", CardsSorting.DATE_SORT
        )
    }

    fun pushDataProfile(ask: String?): String {
        val UPDATE_PHOTO_PROFILE_FB = "PROFILE UPDATE"

        if (ask != null) {
            try {
                editor.remove(UPDATE_PHOTO_PROFILE_FB).apply()
            } catch (ignored: Exception) {
            }
            editor.putString(UPDATE_PHOTO_PROFILE_FB, ask).apply()
        }
        return sharedPreferences.getString(UPDATE_PHOTO_PROFILE_FB, "")!!
    }

    fun accountId(id: String?): String? {
        val ACCOUNT_ID = "ID"

        if (id != null) {
            try {
                editor.remove(ACCOUNT_ID).apply()
            } catch (ignored: Exception) {
            }
            editor.putString(ACCOUNT_ID, id).apply()
        }
        return sharedPreferences.getString(ACCOUNT_ID, "---")
    }

    fun haveAccountSubscribe(have: Boolean?): Boolean {
        val SUBSCRIBE_DATA = "SUBSCRIBE"

        if (have != null) {
            try {
                editor.remove(SUBSCRIBE_DATA).apply()
            } catch (ignored: Exception) {
            }
            editor.putBoolean(SUBSCRIBE_DATA, have).apply()
        }
        return sharedPreferences.getBoolean(SUBSCRIBE_DATA, false)
    }

    fun themeApp(isLight: Boolean? = null): Boolean {
        val THEME = "theme"

        if (isLight != null) {
            try {
                editor.remove(THEME).apply()
            } catch (ignored: Exception) {
            }
            editor.putBoolean(THEME, isLight).apply()
        }
        return sharedPreferences.getBoolean(
            THEME, false
        )
    }

    fun syncUserCardsData(flag: Boolean? = null) =
        flag?.apply {
            try {
                editor.remove("data_sync").apply()
            } finally {
                editor.putBoolean(
                    "data_sync", this
                ).apply()
            }
        } ?: sharedPreferences.getBoolean(
            "data_sync", false
        )

    fun removeAllData() {
        editor.clear().apply()
    }
}