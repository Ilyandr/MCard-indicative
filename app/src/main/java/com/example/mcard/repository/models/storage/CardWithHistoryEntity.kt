package com.example.mcard.repository.models.storage

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Embedded
import androidx.room.Relation
import com.example.mcard.repository.features.extractParcelable

@Keep
internal data class CardWithHistoryEntity(
    @Embedded
    val cardEntity: CardEntity = CardEntity(),
    @Relation(
        parentColumn = "uniqueIdentifier",
        entityColumn = "usageCardId",
        entity = HistoryEntity::class
    )
    val usageHistory: MutableList<HistoryEntity> = mutableListOf(),
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.extractParcelable<CardEntity>()!!,
        parcel.createTypedArrayList(HistoryEntity)!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {}

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<CardWithHistoryEntity> {
        override fun createFromParcel(parcel: Parcel) =
            CardWithHistoryEntity(parcel)

        override fun newArray(size: Int): Array<CardWithHistoryEntity?> =
            arrayOfNulls(size)
    }
}