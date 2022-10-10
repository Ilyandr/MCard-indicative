package com.example.mcard.repository.features.optionally

import java.lang.StringBuilder

internal object CardAdaptation {
    @JvmStatic
    @Synchronized
    infix fun transliterateCardName(
        inputCardName: String,
    ): String {
        var cardName = inputCardName
        val check_regexStroke = StringBuilder()
        var singleSymbol: Char
        var removeSymbol: Char
        cardName += " "

        for (i in 0 until cardName.length - 1) {
            removeSymbol = '-'
            singleSymbol = cardName[i]

            if (i == 0) {
                check_regexStroke.append(singleSymbol.uppercaseChar())
                removeSymbol = singleSymbol
            } else if (i > 1) {
                if (cardName[i - 1] == ' '
                    && cardName[i + 1] != ' '
                    && Character.isLowerCase(singleSymbol)
                ) {
                    if (Character.isLowerCase(singleSymbol))
                        check_regexStroke.append(
                            singleSymbol.uppercaseChar()
                        )
                    removeSymbol = singleSymbol
                } else if (cardName[i - 1] == ' '
                    && cardName[i + 1] != ' '
                    && !Character.isLowerCase(singleSymbol)
                ) {
                    check_regexStroke.append(singleSymbol)
                    removeSymbol = singleSymbol
                }
            }
            if (removeSymbol == '-')
                check_regexStroke.append(
                    singleSymbol.lowercaseChar()
                )
            else
                check_regexStroke.toString().replace(
                    removeSymbol.toString(), ""
                )
        }
        cardName = check_regexStroke
            .toString()
            .replace(" ".toRegex(), "")

        val rus_symbol =
            charArrayOf(
                ' ',
                'а',
                'б',
                'в',
                'г',
                'д',
                'е',
                'ё',
                'ж',
                'з',
                'и',
                'й',
                'к',
                'л',
                'м',
                'н',
                'о',
                'п',
                'р',
                'с',
                'т',
                'у',
                'ф',
                'х',
                'ц',
                'ч',
                'ш',
                'щ',
                'ъ',
                'ы',
                'ь',
                'э',
                'ю',
                'я',
                'А',
                'Б',
                'В',
                'Г',
                'Д',
                'Е',
                'Ё',
                'Ж',
                'З',
                'И',
                'Й',
                'К',
                'Л',
                'М',
                'Н',
                'О',
                'П',
                'Р',
                'С',
                'Т',
                'У',
                'Ф',
                'Х',
                'Ц',
                'Ч',
                'Ш',
                'Щ',
                'Ъ',
                'Ы',
                'Ь',
                'Э',
                'Ю',
                'Я',
                'a',
                'b',
                'c',
                'd',
                'e',
                'f',
                'g',
                'h',
                'i',
                'j',
                'k',
                'l',
                'm',
                'n',
                'o',
                'p',
                'q',
                'r',
                's',
                't',
                'u',
                'v',
                'w',
                'x',
                'y',
                'z',
                'A',
                'B',
                'C',
                'D',
                'E',
                'F',
                'G',
                'H',
                'I',
                'J',
                'K',
                'L',
                'M',
                'N',
                'O',
                'P',
                'Q',
                'R',
                'S',
                'T',
                'U',
                'V',
                'W',
                'X',
                'Y',
                'Z'
            )
        val lat_symbol = arrayOf(
            " ",
            "a",
            "b",
            "v",
            "g",
            "d",
            "e",
            "e",
            "zh",
            "z",
            "i",
            "y",
            "k",
            "l",
            "m",
            "n",
            "o",
            "p",
            "r",
            "s",
            "t",
            "u",
            "f",
            "h",
            "ts",
            "ch",
            "sh",
            "sch",
            "",
            "i",
            "",
            "e",
            "ju",
            "ja",
            "A",
            "B",
            "V",
            "G",
            "D",
            "E",
            "E",
            "Zh",
            "Z",
            "I",
            "Y",
            "K",
            "L",
            "M",
            "N",
            "O",
            "P",
            "R",
            "S",
            "T",
            "U",
            "F",
            "H",
            "Ts",
            "Ch",
            "Sh",
            "Sch",
            "",
            "I",
            "",
            "E",
            "Ju",
            "Ja",
            "a",
            "b",
            "c",
            "d",
            "e",
            "f",
            "g",
            "h",
            "i",
            "j",
            "k",
            "l",
            "m",
            "n",
            "o",
            "p",
            "q",
            "r",
            "s",
            "t",
            "u",
            "v",
            "w",
            "x",
            "y",
            "z",
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "G",
            "H",
            "I",
            "J",
            "K",
            "L",
            "M",
            "N",
            "O",
            "P",
            "Q",
            "R",
            "S",
            "T",
            "U",
            "V",
            "W",
            "X",
            "Y",
            "Z"
        )

        return StringBuilder().apply {
            for (element in cardName)
                for (j in rus_symbol.indices)
                    if (element == rus_symbol[j])
                        append(lat_symbol[j])
        }.toString()
    }

    private fun getLevenshteinDistance(
        correctData: String,
        doubtfulData: String,
    ): Int =
        correctData.length.let { correctLenght ->

            doubtfulData.length.let { doubtfulLenght ->

                Array(correctLenght + 1) {
                    IntArray(doubtfulLenght + 1)
                }.run {

                    for (i in 1..correctLenght)
                        this[i][0] = i

                    for (j in 1..doubtfulLenght)
                        this[0][j] = j


                    var cost: Int
                    for (i in 1..correctLenght) {
                        for (j in 1..doubtfulLenght) {
                            cost =
                                if (correctData[i - 1] == doubtfulData[j - 1])
                                    0
                                else
                                    1

                            this[i][j] =
                                (this[i - 1][j] + 1)
                                    .coerceAtMost(this[i][j - 1] + 1)
                                    .coerceAtMost(this[i - 1][j - 1] + cost)
                        }
                    }

                    this[correctLenght][doubtfulLenght]
                }
            }
        }

    fun comparisonData(
        correctData: String?,
        doubtfulData: String?,
    ): Boolean =
        if (correctData == null || doubtfulData == null)
            false
        else if (correctData == doubtfulData)
            true
        else
            correctData.length.coerceAtLeast(
                doubtfulData.length
            ).run {
                (if (this > 0)
                    (this * 1.0 - getLevenshteinDistance(
                        correctData, doubtfulData
                    )) / this * 1.0
                else
                    1.0) >= 0.77
            }
}