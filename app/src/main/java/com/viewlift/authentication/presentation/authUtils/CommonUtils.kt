package com.viewlift.authentication.presentation.authUtils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color.parseColor
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.regex.Pattern


const val UPDATE_USER_NAME = "Name"
const val UPDATE_EMAIL = "Email Address"

fun isEmailValid(email: String?): Boolean {
    return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email!!).matches()
}


val LoginBottomShape = RoundedCornerShape(
    topStart = 30.dp,
    topEnd = 30.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

val BottomSheetShape = RoundedCornerShape(
    topStart = 30.dp,
    topEnd = 30.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)



val EditTextShape = RoundedCornerShape(
    topStart = 8.dp,
    topEnd = 8.dp,
    bottomStart = 8.dp,
    bottomEnd = 8.dp
)


val CheckboxShape = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = 5.dp,
    bottomEnd = 0.dp
)

fun <T1, T2, T3, T4, T5, T6, R> combineUpdateValidation(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    transform: suspend (T1, T2, T3, T4, T5, T6) -> R
): Flow<R> = combine(
    combine(flow, flow2, flow3, ::Triple),
    combine(flow4, flow5, flow6, ::Triple)
) { t1, t2 ->
    transform(
        t1.first,
        t1.second,
        t1.third,
        t2.first,
        t2.second,
        t2.third
    )
}


fun checkIfPermissionGranted(context: Context, permission: String): Boolean {
    return (ContextCompat.checkSelfPermission(context, permission)
            == PackageManager.PERMISSION_GRANTED)
}

fun shouldShowPermissionRationale(context: Context, permission: String): Boolean {

    val activity = context as Activity?
    if (activity == null)
        Log.d("TAG", "Activity is null")

    return ActivityCompat.shouldShowRequestPermissionRationale(
        activity!!,
        permission
    )
}

fun isValidNumber(toCheck: String?): Boolean {
    return toCheck?.toDoubleOrNull() != null
}


fun isValidInt(toCheck: String?): Boolean {
    return toCheck?.toIntOrNull() != null
}



fun isValidMobile(phone: String?): Boolean {
    return if(!phone.isNullOrEmpty()){

        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            phone.length in 5..15
        } else false
    } else {
        false
    }
}


fun getCurrentLocale(context: Context): Locale? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        context.resources.configuration.locales.get(0)
    } else {
        context.resources.configuration.locale
    }
}

//fun Color.Companion.parse(colorString: String): Color =
//    Color(color = android.graphics.Color.parseColor(colorString))


val String.parse
    get() = Color(parseColor(this))