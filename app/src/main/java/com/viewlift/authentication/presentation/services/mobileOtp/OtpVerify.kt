package com.viewlift.authentication.presentation.services.mobileOtp

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.phone.SmsRetriever

fun startSMSListener(
    context: Context,
    sendOtpToUI: MySMSBroadcastReceiver.OTPReceiveListener
) {
    try {
        val smsReceiver = MySMSBroadcastReceiver()
        smsReceiver!!.initOTPListener(object : MySMSBroadcastReceiver.OTPReceiveListener {
            override fun onOTPReceived(otp: String) {
                sendOtpToUI.onOTPReceived(otp)
                Log.d("MySMSBroadcastReceiver", "OTP received : $otp")
                Log.d("MySMSBroadcastReceiver", "Broadcast receiver unregistered")
                context.unregisterReceiver(smsReceiver)
            }

            override fun onOTPTimeOut() {
                sendOtpToUI.onOTPTimeOut()
            }

        })

//        unregister.observe(context as LifecycleOwner) {
//            if (it) {
//                Log.d("MySMSBroadcastReceiver", "Broadcast receiver unregistered via lifecycle")
//                context.unregisterReceiver(smsReceiver)
//            }
//        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        context.registerReceiver(smsReceiver, intentFilter)

        val client = SmsRetriever.getClient(context)

        val task = client.startSmsRetriever()
        task.addOnSuccessListener {
            // API successfully started
            // Show something like: Waiting for the OTP
            Log.d("MySMSBroadcastReceiver", "SMS Retriever API Started ")
        }

        task.addOnFailureListener {
            // Fail to start API
            Log.e("MySMSBroadcastReceiver", it.localizedMessage)

        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}