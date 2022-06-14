package com.example.mcard.FunctionalInterfaces

@Suppress("DEPRECATION")
internal interface CorrectInputTextAuth
{
    companion object
    {
        const val MAX_BOUND_PASSWORD: Short = 30
        const val MAX_BOUND_ACCOUNT_ID: Short = 16
        const val MAX_BOUND_CARD_INFO: Short = 24

        const val MIN_BOUND_CARD_INFO: Short = 1
        const val MIN_BOUND_PASSWORD: Short = 6
        const val MIN_BOUND_ACCOUNT_ID: Short = 3
    }

    @JvmDefault
    fun String.checkCorrectPassword(minBound: Short, maxBound: Short): Boolean
    {
        if (this.isEmpty()) return false

        if ((this.length >= minBound)
            && (this.length <= maxBound))
        {
            if (minBound == minBound) return true

            for (i in this.indices)
                if ((Character.UnicodeBlock.of(this[i])
                            == Character.UnicodeBlock.CYRILLIC)
                    || this[i].getCharCorrections())
                    return false
        } else return false

        return true
    }

    @JvmDefault
    fun String.checkRequestCardText(): Boolean
    {
        if (this.isEmpty() || this.length > 20)
            return false
        return true
    }

    @JvmDefault
    fun String.checkCorrectLogin(): Boolean
    {
        if (this.isEmpty()) return false

        if ((this.length >= 6)
            && (this.length <= 30)
            && (this.contains("@")))
        {
            for (i in this.indices)
                if ((Character.UnicodeBlock.of(this[i])
                            == Character.UnicodeBlock.CYRILLIC)
                    || this[i].getCharCorrections())
                    return false
        } else return false

        return true
    }

    @JvmDefault
    fun String.removeSymbolsTab() = this.replace(" "
        , ""
        , true)

    private fun Char.getCharCorrections() = ((this == ('-'))
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