package com.example.mcard.repository.features.optionally

import android.text.Editable

import android.text.TextWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


internal interface TextChangedListener : TextWatcher {

    override fun beforeTextChanged(
        s: CharSequence, start: Int, count: Int, after: Int,
    ) {
    }

    override fun onTextChanged(
        s: CharSequence, start: Int, before: Int, count: Int,
    ) {
    }

    override fun afterTextChanged(s: Editable) {
        CoroutineScope(Dispatchers.Main).launch {
            onTextChanged(s.toString())
        }
    }

    fun onTextChanged(data: String)
}