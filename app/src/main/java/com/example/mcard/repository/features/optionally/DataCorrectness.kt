package com.example.mcard.repository.features.optionally

internal object DataCorrectness {

    const val MAX_BOUND_PASSWORD: Short = 30
    const val MAX_BOUND_ACCOUNT_ID: Short = 16
    const val MAX_BOUND_CARD_INFO: Short = 24

    const val MIN_BOUND_CARD_INFO: Short = 1
    const val MIN_BOUND_PASSWORD: Short = 6
    const val MIN_BOUND_ACCOUNT_ID: Short = 3

    fun String?.checkCorrectPassword(): Boolean {
        if (this.isNullOrEmpty())
            return false

        if (this.length >= MIN_BOUND_PASSWORD) {
            for (i in this.indices)
                if ((Character.UnicodeBlock.of(this[i])
                            == Character.UnicodeBlock.CYRILLIC)
                    || this[i].getCharCorrections()
                )
                    return false
        } else
            return false

        return true
    }

    fun String.checkRequestCardText(): Boolean {
        if (this.isEmpty() || this.length > 20)
            return false
        return true
    }

    fun String?.checkCorrectLogin(): Boolean {
        if (this.isNullOrEmpty())
            return false

        if ((this.length >= 6)
            && (this.length <= 30)
            && (this.contains("@"))
        ) {
            for (i in this.indices)
                if ((Character.UnicodeBlock.of(this[i])
                            == Character.UnicodeBlock.CYRILLIC)
                    || this[i].getCharCorrections()
                )
                    return false
        } else return false

        return true
    }

    fun String.removeSymbolsTab() =
        this.replace(
            " ", "", true
        )

    fun Char.getCharCorrections() =
        ((this == ('-'))
                || (this == ('"'))
                || (this == ('`'))
                || (this == ('!'))
                || (this == ('?'))
                || (this == (':'))
                || (this == (';'))
                || (this == (','))
                || (this == ('/'))
                || (this == ('|'))
                || (this == ('('))
                || (this == (')'))
                || (this == ('{'))
                || (this == ('}'))
                || (this == ('['))
                || (this == (']'))
                || (this == ('+')))
}