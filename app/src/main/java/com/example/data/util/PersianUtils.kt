package com.example.data.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Utility functions for Persian digits formatting, search normalization, and text processing.
 */
object PersianUtils {

    /**
     * Converts any ASCII digits in the string to standard Persian digits (۰-۹).
     */
    fun toPersianDigits(input: String): String {
        val builder = StringBuilder(input.length)
        for (char in input) {
            when (char) {
                '0' -> builder.append('۰')
                '1' -> builder.append('۱')
                '2' -> builder.append('۲')
                '3' -> builder.append('۳')
                '4' -> builder.append('۴')
                '5' -> builder.append('۵')
                '6' -> builder.append('۶')
                '7' -> builder.append('۷')
                '8' -> builder.append('۸')
                '9' -> builder.append('۹')
                else -> builder.append(char)
            }
        }
        return builder.toString()
    }

    fun toPersianDigits(number: Number): String {
        return toPersianDigits(number.toString())
    }

    fun formatDistance(meters: Double): String {
        val df = DecimalFormat("#.#", DecimalFormatSymbols(Locale.US))
        return "${toPersianDigits(df.format(meters))} متر"
    }

    /**
     * Converts Eastern Arabic and Persian digits to Latin ASCII digits (0-9).
     */
    fun toLatinDigits(input: String): String {
        val builder = StringBuilder(input.length)
        for (char in input) {
            when (char) {
                '۰', '٠' -> builder.append('0')
                '۱', '١' -> builder.append('1')
                '۲', '٢' -> builder.append('2')
                '۳', '٣' -> builder.append('3')
                '۴', '٤' -> builder.append('4')
                '۵', '٥' -> builder.append('5')
                '۶', '٦' -> builder.append('6')
                '۷', '٧' -> builder.append('7')
                '۸', '٨' -> builder.append('8')
                '۹', '٩' -> builder.append('9')
                else -> builder.append(char)
            }
        }
        return builder.toString()
    }

    /**
     * Normalizes a search query or valve identifier:
     * - Maps Persian/Arabic digits to Latin digits
     * - Uppercases letters (e.g. 'v' -> 'V')
     * - Strips spaces and hyphens
     */
    fun normalizeValveQuery(query: String): String {
        return toLatinDigits(query)
            .replace("\\s+".toRegex(), "")
            .replace("-", "")
            .uppercase(Locale.ROOT)
    }

    /**
     * Checks if a valve name matches a user search term ignoring case and digit script.
     */
    fun matchesValve(valveName: String, query: String): Boolean {
        val normalizedQuery = normalizeValveQuery(query)
        if (normalizedQuery.isEmpty()) return true
        val normalizedValve = normalizeValveQuery(valveName)
        return normalizedValve.contains(normalizedQuery)
    }
}
