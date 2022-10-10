package com.example.mcard.repository.models.storage

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mcard.repository.features.optionally.EmptyConstructor
import com.example.mcard.repository.models.storage.BarcodeModel.CREATOR.SPLIT_SYMBOLS
import com.example.mcard.repository.models.storage.HistoryEntity.CREATOR.HISTORY_TABLE_NAME
import com.google.errorprone.annotations.Keep

@Keep
@EmptyConstructor
@Entity(tableName = HISTORY_TABLE_NAME)
internal data class HistoryEntity(
    @PrimaryKey @ColumnInfo(name = "timeAdd")
    val timeAdd: String = "",
    @ColumnInfo(name = "usageCardId")
    val usageCardId: String? = null,
    @ColumnInfo(name = "shopName")
    val shopName: String? = null,
    @ColumnInfo(name = "shopAddress")
    val shopAddress: String? = null,
) : Parcelable {

    constructor() : this("")

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {}

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<HistoryEntity> {
        override fun createFromParcel(parcel: Parcel) =
            HistoryEntity(parcel)

        override fun newArray(size: Int): Array<HistoryEntity?> =
            arrayOfNulls(size)

        infix fun setBigHistoryAddress(data: Pair<String, String>) =
            "${data.first}$SPLIT_SYMBOLS${data.second}"

        infix fun getBigHistoryAddress(dataBigAddress: String?) =
            try {
                dataBigAddress?.split(
                    SPLIT_SYMBOLS
                )?.run {
                    Pair(
                        this[0], this[1]
                    )
                }
            } catch (argEx: IndexOutOfBoundsException) {
                Pair("", "")
            }

        const val HISTORY_TABLE_NAME = "HISTORY_USE"
    }
}