package com.samples.ocr

import java.util.regex.Matcher
import java.util.regex.Pattern

class RegexChecker {

    private var pattern: Pattern? = null

    private var matcher: Matcher? = null

    fun checkForRegex(text: String, validationFor: String): Boolean? {

        val unIndentedText = text.replace(" ", "")

        pattern = Pattern.compile(validationFor)

        matcher = pattern!!.matcher(unIndentedText)

        return matcher!!.matches()

    }

    companion object {
        val AADHAR_NUMBER_PATTERN = "^[2-9]{1}[0-9]{11}$"

        val PAN_NUMBER = "^([a-zA-Z]){5}([0-9]){4}([a-zA-Z]){1}?"

    }
}