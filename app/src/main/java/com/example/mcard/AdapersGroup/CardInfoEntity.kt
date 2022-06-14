package com.example.mcard.AdapersGroup

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class CardInfoEntity(
 @SerializedName("number") var number: String? = null
, @SerializedName("name") var name: String? = null
, @SerializedName("barcode") var barcode: String? = null
, @SerializedName("color") var color: Int? = null
, @SerializedName("cardOwner") var cardOwner: String? = null
, @SerializedName("dateAddCard") var dateAddCard: Long? = null
, @SerializedName("uniqueIdentifier") val uniqueIdentifier: String? = null): Parcelable
{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString())

    override fun toString() =
        "CardInfoEntity(number=$number" +
                ", name=$name," +
                " barcode=$barcode" +
                ", color=$color" +
                ", cardOwner=$cardOwner" +
                ", dateAddCard=$dateAddCard" +
                ", uniqueIdentifier='access denied')"

    override fun describeContents() = 0
    override fun writeToParcel(p0: Parcel?, p1: Int) {}

    companion object CREATOR : Parcelable.Creator<CardInfoEntity>
    {
        override fun createFromParcel(parcel: Parcel) =
            CardInfoEntity(parcel)

        override fun newArray(size: Int): Array<CardInfoEntity?> =
            arrayOfNulls(size)
    }

    @Keep
    internal data class BarcodeEntity(
        var barcodeDataString: String? = null
        , var barcodeDataType: Int? = null)
    {
        companion object
        {
            const val BARCODE_SPLIT_INFO = "<-->"

            @JvmStatic
            fun serialaziableEntityBarcode(barcode: String?) = BarcodeEntity(
                barcode?.split(BARCODE_SPLIT_INFO)?.get(0)
                , barcode?.split(BARCODE_SPLIT_INFO)?.get(1)?.toInt())
        }

        override fun toString() =
            "${this.barcodeDataString}$BARCODE_SPLIT_INFO${this.barcodeDataType}"
    }
}
