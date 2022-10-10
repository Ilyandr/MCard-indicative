package com.example.mcard.repository.models.storage

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep

@Keep
internal data class BarcodeModel(
    var barcodeDataString: String? = null,
    var barcodeDataType: Int? = null,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun toString() =
        "${this.barcodeDataString}$SPLIT_SYMBOLS${this.barcodeDataType}"

    override fun describeContents() = 0

    override fun writeToParcel(p0: Parcel, p1: Int) {}

    companion object CREATOR : Parcelable.Creator<BarcodeModel> {
        const val SPLIT_SYMBOLS = "<-->"

        override fun createFromParcel(parcel: Parcel) =
            BarcodeModel(parcel)

        override fun newArray(size: Int): Array<BarcodeModel?> =
            arrayOfNulls(size)


        @JvmStatic
        infix fun serialaziableEntityBarcode(barcode: String?) =
            BarcodeModel(
                barcode?.split(
                    SPLIT_SYMBOLS
                )?.get(0),
                barcode?.split(
                    SPLIT_SYMBOLS
                )?.get(1)?.toInt()
            )
    }
}