package com.viewlift.authentication.presentation.viewmodel

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavOptions
import com.apollographql.apollo3.api.Optional
import com.viewlift.authentication.domain.usecase.*
import com.viewlift.authentication.presentation.authUtils.AuthCommonUtils
import com.viewlift.authentication.presentation.authUtils.isValidNumber
import com.viewlift.authentication.presentation.events.LoginEvent
import com.viewlift.authentication.presentation.intent.VerifyOTPIntent
import com.viewlift.authentication.presentation.services.mobileOtp.MySMSBroadcastReceiver
import com.viewlift.authentication.presentation.uicomponents.defaultCountry
import com.viewlift.authentication.presentation.uistate.VerifyOTPScreenUiState
import com.viewlift.common.label.BootstrapLabels
import com.viewlift.common.utils.CommonUtils
import com.viewlift.core.base.BaseViewModel
import com.viewlift.core.data.AppDataRepository
import com.viewlift.core.navigation.NavigationCommand
import com.viewlift.core.navigation.NavigationDestination
import com.viewlift.core.navigation.NavigationManager
import com.viewlift.core.utils.NavMap
import com.viewlift.network.data.remote.model.request.SendOTPParams
import com.viewlift.network.data.remote.model.request.VerifyOTPParams
import com.viewlift.network.domain.repository.BootstrapRepository
import com.viewlift.network.type.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VerifyOTPViewModel @Inject constructor(
    val verifyOtpUseCase: VerifyOtpUseCase,
    val sendIdentitySignOtpUseCase: SendIdentitySignOtpUseCase,
    val bootstrapRepository: BootstrapRepository,
    var appDataRepository: AppDataRepository,
    val navigationManager: NavigationManager,
    savedStateHandle: SavedStateHandle,
    socialLoginUiState: VerifyOTPScreenUiState
) : BaseViewModel<VerifyOTPScreenUiState, VerifyOTPScreenUiState.PartialState, LoginEvent, VerifyOTPIntent>(
    savedStateHandle,
    socialLoginUiState
) {

    var timerStartTime : Long = 0L
    var defaultTime : Int = 30 // 30 seconds

    val startTimerCountDown : MutableStateFlow<Boolean> = MutableStateFlow(true)
    val timerValue : MutableStateFlow<Int> = MutableStateFlow(30)

    var navigationKey: String = ""
    val otpState: MutableStateFlow<String> = MutableStateFlow("")

    var receiveOTP = object : MySMSBroadcastReceiver.OTPReceiveListener {
        override fun onOTPReceived(otp: String) {
            otpState.value = otp
        }

        override fun onOTPTimeOut() {
            Log.d("MySMSBroadcastReceiver", "OTP received inside authviewmodel timeout")
        }
    }

    private fun verifyOtp(key: String, deviceId: String): Flow<VerifyOTPScreenUiState.PartialState> = flow {
        val deviceName = Optional.present(AuthCommonUtils.getDeviceName())

        val params = SignInByPhoneOrEmailInput(
            key = Optional.present(key),
            otpValue = otpState.value,
            emailConsent = Optional.absent(),
            deviceId = Optional.present(deviceId),
            deviceName = deviceName,
            platform = Optional.present(EntitlementDevice.android_phone)
        )
        verifyOtpUseCase(VerifyOTPParams(bootstrapRepository.getCachedMain()?.internalName ?: "", params))
            .onStart {
                emit(VerifyOTPScreenUiState.PartialState.Loading)
            }
            .collect { result ->
                result.onSuccess {
                    if(it?.authorizationToken!=null){
                        emit(VerifyOTPScreenUiState.PartialState.Fetched(it))
                    } else {
                        // empty the fields
                        otpState.value = ""
                        emit(VerifyOTPScreenUiState.PartialState.Error(Throwable("Please retry")))
                    }
                }
                result.onFailure {
                    emit(VerifyOTPScreenUiState.PartialState.Error(it))
                    otpState.value = ""
                }
            }
    }

    private fun resendOtp(
        navigationData: String
    ): Flow<VerifyOTPScreenUiState.PartialState> = flow {

        var phoneNumber: Optional<String?> = Optional.absent()
        var email: Optional<String?> = Optional.absent()
        if (isValidNumber(navigationData.trim())) {
            // Mobile
            phoneNumber = Optional.present("${defaultCountry?.dial_code}${navigationData}")
        } else {
            // Email
            email = Optional.present(navigationData)
        }

        val deviceName = Optional.present(AuthCommonUtils.getDeviceName())

        val init = InitiatePasswordlessSignInput(
            phoneNumber,  // Phone Number
            email, // Email
            Optional.absent(),
            deviceName,
            Optional.present(EntitlementDevice.android_phone)
        )

        sendIdentitySignOtpUseCase(SendOTPParams(bootstrapRepository.getCachedMain()?.internalName ?: "", init)).onStart {
            emit(VerifyOTPScreenUiState.PartialState.Loading)
        }.collect { result ->
            result.onSuccess {
                if (!it?.identityInitiateSignOtp?.key.isNullOrEmpty()) {
                    emit(VerifyOTPScreenUiState.PartialState.TokenReSend(it))
                    startTimerCountDown.value = true
                } else {
                    emit(VerifyOTPScreenUiState.PartialState.Error(Throwable("Otp Cannot be sent")))
                }
            }
            result.onFailure { error ->
                emit(VerifyOTPScreenUiState.PartialState.Error(error))
                Timber.d("Error - > " + error.message)
                Timber.d(error.stackTraceToString())
            }
        }
    }

    fun navigateToTVEScreen() {
        viewModelScope.launch {
            val isUserSubscribed = appDataRepository.isUserSubscribedValue() ?: false

            val userName = appDataRepository.getUserName()
            val emailId = appDataRepository.getEmailId()
            val phoneNumber = appDataRepository.getPhoneNumber()

            val navDestination = when {
                !isUserSubscribed -> {
                    NavigationDestination.Page.route.replace(oldValue = "{KEY_PAGE_PATH}", newValue = NavMap.pageMap[1].replace("/","*"))
                }
                phoneNumber.isNullOrEmpty() || userName.isNullOrEmpty() || emailId.isNullOrEmpty() -> {
                    NavigationDestination.Page.route.replace(oldValue = "{KEY_PAGE_PATH}", newValue = NavMap.pageMap[2].replace("/","*"))
                }
                else -> {
                    NavigationDestination.Home.route
                }
            }

            navigationManager.navigate(object : NavigationCommand {
                override val destination = navDestination
                override val configuration: NavOptions = NavOptions.Builder().setPopUpTo(
                    NavigationDestination.Page.route,
                    inclusive = true,
                    saveState = false
                ).build()
            })

        }
    }

    override fun mapIntents(intent: VerifyOTPIntent): Flow<VerifyOTPScreenUiState.PartialState> =
        when (intent) {
            is VerifyOTPIntent.GetVerificationCode -> verifyOtp(intent.verificationCode, intent.deviceId)
            is VerifyOTPIntent.GetReVerificationCode -> resendOtp(
                intent.navigationData
            )
        }

    override fun reduceUiState(
        previousState: VerifyOTPScreenUiState,
        partialState: VerifyOTPScreenUiState.PartialState
    ): VerifyOTPScreenUiState = when (partialState) {
        is VerifyOTPScreenUiState.PartialState.Loading -> previousState.copy(
            isLoading = true,
            isError = false
        )
        is VerifyOTPScreenUiState.PartialState.TokenReSend -> previousState.copy(
            isLoading = false,
            tokenResend = partialState.status,
            isError = false
        )
        is VerifyOTPScreenUiState.PartialState.Fetched -> previousState.copy(
            isLoading = false,
            signInTokenResponse = partialState.signInTokenResponse,
            isError = false
        )
        is VerifyOTPScreenUiState.PartialState.Error -> previousState.copy(
            isLoading = false,
            isError = true,
            error = partialState.throwable,
            apolloApiError = CommonUtils.getApolloError(partialState.throwable)
        )
    }

    fun startTimer(): CountDownTimer {
        val timer: CountDownTimer = object : CountDownTimer(defaultTime * 1000L, 1000L) {

            override fun onTick(millisUntilFinished: Long) {
                val current = timerValue.value ?: 0
                if (current <= 0) {
                    onFinish()
                } else {
                    timerValue.value = current - 1
                }
            }

            override fun onFinish() {
                timerValue.value = defaultTime
                startTimerCountDown.value = false
            }
        }
        return timer
    }

    init {
        timerStartTime = System.currentTimeMillis()
    }
}