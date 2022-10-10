package com.example.mcard.repository.models.storage

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mcard.repository.features.optionally.EmptyConstructor
import com.example.mcard.repository.models.storage.CardEntity.CREATOR.CARDS_TABLE_NAME
import com.google.errorprone.annotations.Keep

@Keep
@EmptyConstructor
@Entity(tableName = CARDS_TABLE_NAME)
data class CardEntity(
    @ColumnInfo(name = "number")
    var number: String? = null,
    @ColumnInfo(name = "name")
    var name: String? = null,
    @ColumnInfo(name = "barcode")
    var barcode: String? = null,
    @ColumnInfo(name = "color")
    var color: Int? = null,
    @ColumnInfo(name = "cardOwner")
    var cardOwner: String? = null,
    @ColumnInfo(name = "dateAddCard")
    var dateAddCard: Long? = null,
    @PrimaryKey
    @ColumnInfo(name = "uniqueIdentifier")
    val uniqueIdentifier: String = "",
) : Parcelable {

    constructor() : this(null)

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString()!!
    )

    override fun describeContents() = 0
    override fun writeToParcel(p0: Parcel, p1: Int) {}

    companion object CREATOR : Parcelable.Creator<CardEntity> {
        override fun createFromParcel(parcel: Parcel) =
            CardEntity(parcel)

        override fun newArray(size: Int): Array<CardEntity?> =
            arrayOfNulls(size)

        const val CARDS_TABLE_NAME = "CARD"
    }
}
