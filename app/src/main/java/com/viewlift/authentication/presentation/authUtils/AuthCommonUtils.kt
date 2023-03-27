package com.viewlift.authentication.presentation.authUtils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import com.viewlift.core.utils.Utils
import java.time.LocalDate
import java.time.Period
import java.util.*

class AuthCommonUtils {

    companion object {

        const val EMAIL_TYPE = "EMAIL"
        const val MOBILE_TYPE = "MOBILE"

        /**
         * Description : Get the device model along with manufacturer
         *
         * @return device name
         */
        fun getDeviceName(): String? {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.lowercase(Locale.getDefault())
                    .startsWith(manufacturer.lowercase(Locale.getDefault()))
            ) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }

        /**
         * Description : Capitalize the first character of given string
         *
         * @param s String to capitalize
         * @return Capitalized string object
         */
        private fun capitalize(s: String?): String? {
            if (s == null || s.isEmpty()) {
                return ""
            }
            val first = s[0]
            return if (Character.isUpperCase(first)) {
                s
            } else {
                first.uppercaseChar().toString() + s.substring(1)
            }
        }

        fun getDeviceId(context: Context): String? {
            return Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        }

        fun getAge(year: Int, month: Int, day: Int): Int {
            //calculating age from dob
            val dob = Calendar.getInstance()
            val today = Calendar.getInstance()
            dob[year, month] = day
            var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
            return age
        }

        fun validateDateInput(value: String, type: String): Boolean {
            when (type) {
                "MONTH" -> {
                    return getValidMonth(value)
                }
                "DAY" -> {
                    return getValidDay(value)
                }
                "YEAR" -> {
                    return getValidYear(value)
                }
                else -> return false
            }
        }

        fun getValidDay(dayOfMonth: String): Boolean {
            val ageRegex = Regex("0?[1-9]|[12][0-9]|3[01]")
            return (dayOfMonth.toString()).matches(ageRegex)
        }


        fun getValidMonth(month: String): Boolean {
            val ageRegex = Regex("0?[1-9]|1[012]")
            return (month.toString()).matches(ageRegex)
        }


        fun getValidYear(year: String): Boolean {
            val ageRegex = Regex("(18|19|20)[0-9][0-9]")
            return (year.toString()).matches(ageRegex)
        }

        fun getValidButton(isEmail: Boolean, emailOrMobileText: String?): Boolean {
            return if (isEmail) {
                // Valid Email
                isEmailValid(emailOrMobileText?.trim())
            } else {
                // Valid Mobile
                isValidMobile(emailOrMobileText?.trim())
            }
        }

    }



}

/**
 * Find the closest Activity in a given Context.
 */
internal fun Context.findActivity(): ComponentActivity {
    var context = this
    while (context is ContextWrapper) {
        if (context is ComponentActivity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}