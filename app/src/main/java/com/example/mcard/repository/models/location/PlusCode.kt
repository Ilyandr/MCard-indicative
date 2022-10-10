package com.example.mcard.repository.models.location

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
internal data class PlusCode
@JvmOverloads constructor(
  @field:SerializedName("global_code")
  var globalCode: String? = null

  , @field:SerializedName("compound_code")
  var compoundCode: String? = null
)