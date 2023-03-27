package com.viewlift.authentication.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import kotlin.collections.LinkedHashMap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.api.Optional.Companion.present
import com.viewlift.authentication.domain.usecase.*
import com.viewlift.authentication.presentation.authUtils.*
import com.viewlift.authentication.presentation.events.LoginEvent
import com.viewlift.authentication.presentation.intent.AuthLoginIntent
import com.viewlift.authentication.presentation.screens.LoginButtonData
import com.viewlift.authentication.presentation.uistate.ShowEmailOrMobile
import com.viewlift.authentication.presentation.uistate.SocialLoginUiState
import com.viewlift.common.label.*
import com.viewlift.common.utils.CommonUtils
import com.viewlift.core.base.BaseViewModel
import com.viewlift.core.data.AppDataRepository
import com.viewlift.core.navigation.NavigationCommand
import com.viewlift.core.navigation.NavigationManager
import com.viewlift.network.BootStrapQuery
import com.viewlift.network.PageQuery
import com.viewlift.network.data.remote.model.request.AppleSignInParams
import com.viewlift.network.data.remote.model.request.FacebookSignInParams
import com.viewlift.network.data.remote.model.response.CountryCodeResponse
import com.viewlift.network.data.remote.model.request.SendOTPParams
import com.viewlift.network.data.remote.model.request.SignInGoogleParams
import com.viewlift.network.domain.repository.BootstrapRepository
import com.viewlift.network.type.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


/**
 * AuthViewModel
 *
 */

@HiltViewModel
class AuthViewModel @Inject constructor(
//    val bootstrapRepository: BootstrapRepository,
//    var navigationManager: NavigationManager,
//    var appDataRepository: AppDataRepository,
    savedStateHandle: SavedStateHandle,
    val sendIdentitySignOtpUseCase: SendIdentitySignOtpUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val facebookLoginUseCase: FacebookLoginUseCase,
    private val appleLoginUseCase: AppleLoginUseCase,
    private val countryCodeUseCase: CountryCodeUseCase,
    socialLoginUiState: SocialLoginUiState
) : BaseViewModel<SocialLoginUiState, SocialLoginUiState.SocialLoginPartialState, LoginEvent, AuthLoginIntent>(
    savedStateHandle,
    socialLoginUiState
) {

    var countryCode: String? = null

    var emailOrMobileText: String = ""
    val emailOrMobileState: MutableStateFlow<String?> = MutableStateFlow(null)
   // var countryCodeResponses: List<CountryCodeResponse>? = null

    val checkboxValue: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showEmailConsent: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val socialLoginButtons: MutableStateFlow<List<LoginButtonData>?> = MutableStateFlow(null)
    val emailMobileLoginButtons: MutableStateFlow< List<LoginButtonData>> = MutableStateFlow(listOf())
    val isEmailOrMobile = MutableStateFlow<ShowEmailOrMobile>(ShowEmailOrMobile.NoOptions)
    val showError = MutableStateFlow<Pair<Boolean, String>?>(null)
    val googleLogin: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val facebookLoginButtonClick: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val appleLoginButtonClick: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val googleLoginButtonClick: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val fullScreenLoader: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun showEmailOrMobile(option: ShowEmailOrMobile) {
        viewModelScope.launch {
            isEmailOrMobile.emit(option)
        }
    }

    fun ShowError(error: Pair<Boolean, String>?) {
        showError.value = error
    }
 //TODO :Default otp

//    private fun sendOtp(): Flow<SocialLoginUiState.SocialLoginPartialState> = flow {
//        var phoneNumber: Optional<String?> = Optional.absent()
//        var email: Optional<String?> = Optional.absent()
//        if (isValidNumber(emailOrMobileText.trim())) {
//            // Mobile
//            val defaultCountry: CountryCodeResponse? = countryCodeResponses?.find { it.code == countryCode }
//            phoneNumber = Optional.present("${defaultCountry?.dial_code}${emailOrMobileText.trim()}")
//        } else {
//            // Email
//            email = Optional.present(emailOrMobileText.trim())
//        }
//
//        val deviceName = Optional.present(AuthCommonUtils.getDeviceName())
//
//        val init = InitiatePasswordlessSignInput(
//            phoneNumber,  // Phone Number
//            email, // Email
//            Optional.absent(),
//            deviceName,
//            Optional.present(EntitlementDevice.android_phone)
//        )
//
//        sendIdentitySignOtpUseCase(SendOTPParams(bootstrapRepository.getCachedMain()?.internalName ?: "", init)).onStart {
//            emit(SocialLoginUiState.SocialLoginPartialState.Loading)
//        }.collect { result ->
//            result.onSuccess { otpResult ->
//
//                if(!otpResult?.identityInitiateSignOtp?.key.isNullOrEmpty()){
//                    emit(SocialLoginUiState.SocialLoginPartialState.VerificationKey(otpResult?.identityInitiateSignOtp?.key))
//                } else {
//                    emit(SocialLoginUiState.SocialLoginPartialState.Error(Throwable("OTP cannot be sent")))
//                }
//            }
//            result.onFailure { error ->
//                emit(SocialLoginUiState.SocialLoginPartialState.Error(error))
//
//                Timber.d("Error - > " + error.message)
//                Timber.d(error.stackTraceToString())
//            }
//
//        }
//    }

    fun navigateToNextScreen(key: String) {
        var type = ""
        if (isEmailOrMobile.value == ShowEmailOrMobile.EmailOptions) {
            // Email Option Selected
            type = AuthCommonUtils.EMAIL_TYPE
        } else {
            // Mobile Selected
            type = AuthCommonUtils.MOBILE_TYPE
        }

        val data = emailOrMobileText

        val cmd = object : NavigationCommand {
            override val destination = "authVerify/$type/$data/$key"
        }
        navigationManager.navigate(cmd)
    }

    fun setLoginButtonState(pageQueryModule: PageQuery.Module?) {

        viewModelScope.launch {
            pageQueryModule?.let { pageQuery ->
                if (pageQuery.layout != null && pageQuery.layout?.layout != null && pageQuery.layout?.layout?.settings != null) {
                    if (pageQuery.layout!!.layout.settings is LinkedHashMap<*, *>) {
                        (pageQuery.layout!!.layout.settings as LinkedHashMap<*, *>)["options"]?.let { options ->
                            if (options is LinkedHashMap<*, *>) {

                                (options["socialLoginSignup"] as ArrayList<LinkedHashMap<*, *>>)?.let { socialOptions ->

                                    val social: List<LoginButtonData> =
                                        socialOptions.map { options ->
                                            LoginButtonData(
                                                dragDropId = (options as LinkedHashMap<String, Int>)["dragDropId"],
                                                enable = (options as LinkedHashMap<String, Boolean>)["enable"],
                                                title = (options as LinkedHashMap<String, String>)["title"]
                                            )
                                        }
                                    socialLoginButtons.value = social

                                }

                                (options["emailLoginSignup"] as ArrayList<LinkedHashMap<*, *>>)?.let { socialOptions ->

                                    val emailLoginOptions: List<LoginButtonData> =
                                        socialOptions.map { options ->
                                            LoginButtonData(
                                                dragDropId = (options as LinkedHashMap<String, Int>)["dragDropId"],
                                                enable = (options as LinkedHashMap<String, Boolean>)["enable"],
                                                title = (options as LinkedHashMap<String, String>)["title"]
                                            )
                                        }

                                    // title -> Login / Signup
                                    emailMobileLoginButtons.value = emailLoginOptions
                                }
                            }
                        }
                    }

                    LoginScreenColors.activeCTAColorLogin = (pageQuery.layout!!.layout.settings as LinkedHashMap<String, String>)[LoginScreenKeys.activeCTAColor] ?: LoginScreenColors.activeCTAColorLogin
                    LoginScreenColors.disabledCTAColorLogin = (pageQuery.layout!!.layout.settings as LinkedHashMap<String, String>)[LoginScreenKeys.disabledCTAColor] ?: LoginScreenColors.disabledCTAColorLogin
                    LoginScreenColors.inputBgColor = (pageQuery.layout!!.layout.settings as LinkedHashMap<String, String>)[LoginScreenKeys.inputBgColor] ?: LoginScreenColors.inputBgColor
                    LoginScreenColors.moduleBackgroundColorLogin = (pageQuery.layout!!.layout.settings as LinkedHashMap<String, String>)[LoginScreenKeys.moduleBackgroundColor] ?: LoginScreenColors.moduleBackgroundColorLogin
                }


                if(pageQuery.onAuthenticationModule!=null && pageQuery.onAuthenticationModule?.metadataMap!=null){
                    if(pageQuery.onAuthenticationModule!!.metadataMap is LinkedHashMap<*, *>){
                        val metaDataMap: java.util.LinkedHashMap<String?, String?> = (pageQuery.onAuthenticationModule?.metadataMap as LinkedHashMap<String?, String?>)

                        LoginScreenLabels.createAccountTitle= if(!metaDataMap[LoginScreenKeys.createAccountTitle].isNullOrEmpty()) metaDataMap[LoginScreenKeys.createAccountTitle].toString() else LoginScreenLabels.createAccountTitle
                        LoginScreenLabels.createAccountSubTitle= if(!metaDataMap[LoginScreenKeys.createAccountSubTitle].isNullOrEmpty()) metaDataMap[LoginScreenKeys.createAccountSubTitle].toString() else LoginScreenLabels.createAccountSubTitle
                        LoginScreenLabels.appleSignInButtonTitle= if(!metaDataMap[LoginScreenKeys.appleSignInButtonTitle].isNullOrEmpty()) metaDataMap[LoginScreenKeys.appleSignInButtonTitle].toString() else LoginScreenLabels.appleSignInButtonTitle
                        LoginScreenLabels.googleSignInButtonTitle= if(!metaDataMap[LoginScreenKeys.googleSignInButtonTitle].isNullOrEmpty()) metaDataMap[LoginScreenKeys.googleSignInButtonTitle].toString() else LoginScreenLabels.googleSignInButtonTitle
                        LoginScreenLabels.googleError= if(!metaDataMap[LoginScreenKeys.googleError].isNullOrEmpty()) metaDataMap[LoginScreenKeys.googleError].toString() else LoginScreenLabels.googleError
                        LoginScreenLabels.enterCodeToVerifyEmail= if(!metaDataMap[LoginScreenKeys.enterCodeToVerifyEmail].isNullOrEmpty()) metaDataMap[LoginScreenKeys.enterCodeToVerifyEmail].toString() else LoginScreenLabels.enterCodeToVerifyEmail
                        LoginScreenLabels.enterCodeToVerifyMobile= if(!metaDataMap[LoginScreenKeys.enterCodeToVerifyMobile].isNullOrEmpty()) metaDataMap[LoginScreenKeys.enterCodeToVerifyMobile].toString() else LoginScreenLabels.enterCodeToVerifyMobile
                        LoginScreenLabels.getVerificationCodeButtonTitle= if(!metaDataMap[LoginScreenKeys.getVerificationCodeButtonTitle].isNullOrEmpty()) metaDataMap[LoginScreenKeys.getVerificationCodeButtonTitle].toString() else LoginScreenLabels.getVerificationCodeButtonTitle
                        LoginScreenLabels.resendCodeButtonText= if(!metaDataMap[LoginScreenKeys.resendCodeButtonText].isNullOrEmpty()) metaDataMap[LoginScreenKeys.resendCodeButtonText].toString() else LoginScreenLabels.resendCodeButtonText
                        LoginScreenLabels.verifyEmailTitle= if(!metaDataMap[LoginScreenKeys.verifyEmailTitle].isNullOrEmpty()) metaDataMap[LoginScreenKeys.verifyEmailTitle].toString() else LoginScreenLabels.verifyEmailTitle
                        LoginScreenLabels.verifyMobileTitle= if(!metaDataMap[LoginScreenKeys.verifyMobileTitle].isNullOrEmpty()) metaDataMap[LoginScreenKeys.verifyMobileTitle].toString() else LoginScreenLabels.verifyMobileTitle
                        LoginScreenLabels.verifyMobileSubtitle= if(!metaDataMap[LoginScreenKeys.verifyMobileSubtitle].isNullOrEmpty()) metaDataMap[LoginScreenKeys.verifyMobileSubtitle].toString() else LoginScreenLabels.verifyMobileSubtitle
                        LoginScreenLabels.emailTextFieldPlaceHolder= if(!metaDataMap[LoginScreenKeys.emailTextFieldPlaceHolder].isNullOrEmpty()) metaDataMap[LoginScreenKeys.emailTextFieldPlaceHolder].toString() else LoginScreenLabels.emailTextFieldPlaceHolder
                        LoginScreenLabels.verifyEmailSubtitle= if(!metaDataMap[LoginScreenKeys.verifyEmailSubtitle].isNullOrEmpty()) metaDataMap[LoginScreenKeys.verifyEmailSubtitle].toString() else LoginScreenLabels.verifyEmailSubtitle
                        LoginScreenLabels.useDifferentEmailText= if(!metaDataMap[LoginScreenKeys.useDifferentEmailText].isNullOrEmpty()) metaDataMap[LoginScreenKeys.useDifferentEmailText].toString() else LoginScreenLabels.useDifferentEmailText
                        LoginScreenLabels.useDifferentMobileText= if(!metaDataMap[LoginScreenKeys.useDifferentMobileText].isNullOrEmpty()) metaDataMap[LoginScreenKeys.useDifferentMobileText].toString() else LoginScreenLabels.useDifferentMobileText
                        LoginScreenLabels.alreadyPaidText= if(!metaDataMap[LoginScreenKeys.alreadyPaidText].isNullOrEmpty()) metaDataMap[LoginScreenKeys.alreadyPaidText].toString() else LoginScreenLabels.alreadyPaidText
                        LoginScreenLabels.restorePurchaseTitle= if(!metaDataMap[LoginScreenKeys.restorePurchaseTitle].isNullOrEmpty()) metaDataMap[LoginScreenKeys.restorePurchaseTitle].toString() else LoginScreenLabels.restorePurchaseTitle

                        LoginScreenLabels.CROSS_COUNTRY_PHONE = if(!metaDataMap[LoginScreenKeys.CROSS_COUNTRY_PHONE].isNullOrEmpty()) metaDataMap[LoginScreenKeys.CROSS_COUNTRY_PHONE].toString() else LoginScreenLabels.CROSS_COUNTRY_PHONE
                        LoginScreenLabels.DEVICE_LIMIT_EXCEEDED = if(!metaDataMap[LoginScreenKeys.DEVICE_LIMIT_EXCEEDED].isNullOrEmpty()) metaDataMap[LoginScreenKeys.DEVICE_LIMIT_EXCEEDED].toString() else LoginScreenLabels.DEVICE_LIMIT_EXCEEDED
                        LoginScreenLabels.EMAIL_ALREADY_LINKED = if(!metaDataMap[LoginScreenKeys.EMAIL_ALREADY_LINKED].isNullOrEmpty()) metaDataMap[LoginScreenKeys.EMAIL_ALREADY_LINKED].toString() else LoginScreenLabels.EMAIL_ALREADY_LINKED
                        LoginScreenLabels.EMAIL_NOT_EXIST = if(!metaDataMap[LoginScreenKeys.EMAIL_NOT_EXIST].isNullOrEmpty()) metaDataMap[LoginScreenKeys.EMAIL_NOT_EXIST].toString() else LoginScreenLabels.EMAIL_NOT_EXIST
                        LoginScreenLabels.EMAIL_NOT_REGISTERED = if(!metaDataMap[LoginScreenKeys.EMAIL_NOT_REGISTERED].isNullOrEmpty()) metaDataMap[LoginScreenKeys.EMAIL_NOT_REGISTERED].toString() else LoginScreenLabels.EMAIL_NOT_REGISTERED
                        LoginScreenLabels.EMAIL_OR_PASSWORD_INCORRECT = if(!metaDataMap[LoginScreenKeys.EMAIL_OR_PASSWORD_INCORRECT].isNullOrEmpty()) metaDataMap[LoginScreenKeys.EMAIL_OR_PASSWORD_INCORRECT].toString() else LoginScreenLabels.EMAIL_OR_PASSWORD_INCORRECT
                        LoginScreenLabels.EMAIL_VERIFICATION_FAILED = if(!metaDataMap[LoginScreenKeys.EMAIL_VERIFICATION_FAILED].isNullOrEmpty()) metaDataMap[LoginScreenKeys.EMAIL_VERIFICATION_FAILED].toString() else LoginScreenLabels.EMAIL_VERIFICATION_FAILED
                        LoginScreenLabels.INVALID_REQUEST_PARAMS = if(!metaDataMap[LoginScreenKeys.INVALID_REQUEST_PARAMS].isNullOrEmpty()) metaDataMap[LoginScreenKeys.INVALID_REQUEST_PARAMS].toString() else LoginScreenLabels.INVALID_REQUEST_PARAMS
                        LoginScreenLabels.NAME_NOT_VALID = if(!metaDataMap[LoginScreenKeys.NAME_NOT_VALID].isNullOrEmpty()) metaDataMap[LoginScreenKeys.NAME_NOT_VALID].toString() else LoginScreenLabels.NAME_NOT_VALID
                        LoginScreenLabels.OTP_MISMATCH = if(!metaDataMap[LoginScreenKeys.OTP_MISMATCH].isNullOrEmpty()) metaDataMap[LoginScreenKeys.OTP_MISMATCH].toString() else LoginScreenLabels.OTP_MISMATCH
                        LoginScreenLabels.OTP_SENT_FAILED = if(!metaDataMap[LoginScreenKeys.OTP_SENT_FAILED].isNullOrEmpty()) metaDataMap[LoginScreenKeys.OTP_SENT_FAILED].toString() else LoginScreenLabels.OTP_SENT_FAILED
                        LoginScreenLabels.PASSWORD_NOT_VALID = if(!metaDataMap[LoginScreenKeys.PASSWORD_NOT_VALID].isNullOrEmpty()) metaDataMap[LoginScreenKeys.PASSWORD_NOT_VALID].toString() else LoginScreenLabels.PASSWORD_NOT_VALID
                        LoginScreenLabels.PHONE_ALREADY_LINKED = if(!metaDataMap[LoginScreenKeys.PHONE_ALREADY_LINKED].isNullOrEmpty()) metaDataMap[LoginScreenKeys.PHONE_ALREADY_LINKED].toString() else LoginScreenLabels.PHONE_ALREADY_LINKED
                        LoginScreenLabels.PHONE_NOT_LINKED = if(!metaDataMap[LoginScreenKeys.PHONE_NOT_LINKED].isNullOrEmpty()) metaDataMap[LoginScreenKeys.PHONE_NOT_LINKED].toString() else LoginScreenLabels.PHONE_NOT_LINKED
                        LoginScreenLabels.PHONE_NOT_VALID = if(!metaDataMap[LoginScreenKeys.PHONE_NOT_VALID].isNullOrEmpty()) metaDataMap[LoginScreenKeys.PHONE_NOT_VALID].toString() else LoginScreenLabels.PHONE_NOT_VALID
                        LoginScreenLabels.VERIFY_OTP_FAILED = if(!metaDataMap[LoginScreenKeys.VERIFY_OTP_FAILED].isNullOrEmpty()) metaDataMap[LoginScreenKeys.VERIFY_OTP_FAILED].toString() else LoginScreenLabels.VERIFY_OTP_FAILED

                    }

                    bootstrapRepository.getCachedMain()?.emailConsent?.let{emailConsent->
                        BootstrapLabels.emailConsentText = if(!emailConsent.message.isNullOrEmpty()) emailConsent.message!! else BootstrapLabels.emailConsentText
                        checkboxValue.value = emailConsent.isChecked ?: false
                        showEmailConsent.value = emailConsent.enableEmailConsent ?: false
                    }

                    bootstrapRepository.getCachedBrand()?.general?.backgroundColor?.let { //#071331
                        BootstrapColors.generalBackground = it
                    }

                    bootstrapRepository.getCachedBrand()?.general?.textColor?.let { //#071331
                        BootstrapColors.generalTextColor = it
                    }

                    bootstrapRepository.getCachedBrand()?.cta?.secondary?.styleAttributesWithBorderParts?.backgroundColor?.let {
                        BootstrapColors.secondayBackground = it
                    }

                    bootstrapRepository.getCachedBrand()?.cta?.primary?.styleAttributesWithBorderParts?.backgroundColor?.let {
                        BootstrapColors.ctaBGColor = it
                    }
                }
            }
        }
    }

    private fun postGoogleLogin(
        googleToken: String,
        deviceId: String
    ): Flow<SocialLoginUiState.SocialLoginPartialState> = flow {

        val input = SignInGoogleInput(
            googleToken = googleToken,
            emailConsent = present(true),
            deviceId = deviceId,
            deviceName = present(AuthCommonUtils.getDeviceName()),
            platform = present(EntitlementDevice.android_phone)
        )

        googleLoginUseCase(SignInGoogleParams(site = bootstrapRepository.getCachedMain()?.internalName ?: "", input = input))
            .onStart {
                emit(SocialLoginUiState.SocialLoginPartialState.Loading)
            }
            .collect { result ->
                result.onSuccess { page ->
                    emit(SocialLoginUiState.SocialLoginPartialState.Fetched(page))
                }
                    .onFailure {
                        emit(SocialLoginUiState.SocialLoginPartialState.Error(it))
                    }
            }
    }

    private fun getCountryCodes(): Flow<SocialLoginUiState.SocialLoginPartialState> =
        flow {
            countryCodeUseCase().onStart {
//                emit(SocialLoginUiState.SocialLoginPartialState.Loading)
            }.collect { result ->
                result.onSuccess { countryCodes ->
                    countryCodeResponses = countryCodes?.filter { it.image!=null }
//                    emit(SocialLoginUiState.SocialLoginPartialState.GetCountryCode(countryCodes))
                }
                result.onFailure { error ->
//                    emit(SocialLoginUiState.SocialLoginPartialState.Error(error))

                    Timber.d("Error - > " + error.message)
                    Timber.d(error.stackTraceToString())
                }
            }
        }


    private fun postAppleLogin(
        appleToken: String,
        deviceId: String
    ): Flow<SocialLoginUiState.SocialLoginPartialState> =

        flow {
            val input = AppleSignInInput(
                identityToken = appleToken,
                emailConsent = present(true),
                deviceId = deviceId,
                deviceName = AuthCommonUtils.getDeviceName() ?: "",
                platform = EntitlementDevice.android_phone
            )

            appleLoginUseCase(AppleSignInParams(site = bootstrapRepository.getCachedMain()?.internalName ?: "", input = input))
                .onStart {
                    emit(SocialLoginUiState.SocialLoginPartialState.Loading)
                }
                .collect { result ->
                    result.onSuccess { page ->
                        emit(SocialLoginUiState.SocialLoginPartialState.Fetched(page))
                    }
                        .onFailure {
                            emit(SocialLoginUiState.SocialLoginPartialState.Error(it))
                        }
                }
        }

    private fun postFacebookLogin(
        facebookToken: String,
        deviceId: String
    ): Flow<SocialLoginUiState.SocialLoginPartialState> =
        flow {
            val input = SignInFacebookInput(
                facebookToken = facebookToken,
                emailConsent = present(true),
                deviceId = present(deviceId),
                deviceName = present(AuthCommonUtils.getDeviceName()),
                platform = present(EntitlementDevice.android_phone)
            )

            facebookLoginUseCase(FacebookSignInParams(site = bootstrapRepository.getCachedMain()?.internalName ?: "", input = input))
                .onStart {
                    emit(SocialLoginUiState.SocialLoginPartialState.Loading)
                }
                .collect { result ->
                    result.onSuccess { page ->
                        emit(SocialLoginUiState.SocialLoginPartialState.Fetched(page))
                    }
                        .onFailure {
                            emit(SocialLoginUiState.SocialLoginPartialState.Error(it))
                        }
                }
        }

    override fun mapIntents(intent: AuthLoginIntent): Flow<SocialLoginUiState.SocialLoginPartialState> =
        when (intent) {
            is AuthLoginIntent.GetGoogleLogin -> postGoogleLogin(
                intent.googleToken,
                intent.deviceId
            )
            is AuthLoginIntent.GetAppleLogin -> postAppleLogin(
                intent.appleToken,
                intent.deviceId
            )
            is AuthLoginIntent.GetFacebookLogin -> postFacebookLogin(
                intent.facebookToken,
                intent.deviceId
            )
            is AuthLoginIntent.SendVerificationCode -> sendOtp()
            is AuthLoginIntent.GetCountryCode -> getCountryCodes()
        }

    override fun reduceUiState(
        previousState: SocialLoginUiState,
        partialState: SocialLoginUiState.SocialLoginPartialState
    ): SocialLoginUiState = when (partialState) {
        is SocialLoginUiState.SocialLoginPartialState.Loading -> previousState.copy(
            isLoading = true,
            isError = false
        )
        is SocialLoginUiState.SocialLoginPartialState.VerificationKey -> previousState.copy(
            isLoading = false,
            verificationKey = partialState.verificationKey,
            isError = false
        )
        is SocialLoginUiState.SocialLoginPartialState.GetCountryCode -> previousState.copy(
            isLoading = false,
            countryCode = partialState.countryCode,
            isError = false
        )
        is SocialLoginUiState.SocialLoginPartialState.Fetched -> previousState.copy(
            isLoading = false,
            signInTokenResponse = partialState.signInTokenResponse,
            isError = false
        )
        is SocialLoginUiState.SocialLoginPartialState.Error -> previousState.copy(
            isLoading = false,
            isError = true,
            error = partialState.throwable,
            apolloApiError = CommonUtils.getApolloError(partialState.throwable)
        )
    }

    fun handleEditTextData(
        emailOrMobile: String,
        showEmailOrMobile: ShowEmailOrMobile
    ) {
        emailOrMobileText = emailOrMobile
        emailOrMobileState.value = emailOrMobile

        if (System.currentTimeMillis() - lastClickTime < 1000) {
            return
        }

        lastClickTime = System.currentTimeMillis()

        if (emailOrMobile.isNotBlank()) {
            viewModelScope.launch {
                switchEmailOrMobile(emailOrMobile, showEmailOrMobile, true)
            }
        }
    }

    fun switchEmailOrMobile(
        emailOrMobile: String,
        showEmailOrMobile: ShowEmailOrMobile,
        focus: Boolean
    ) {
        if (emailOrMobile.trim().isNotBlank()) {
            if (isValidNumber(emailOrMobile.trim())) {
                if (showEmailOrMobile != ShowEmailOrMobile.MobileOptions) {
                    showEmailOrMobile(ShowEmailOrMobile.MobileOptions)
                }
            } else {
                if (showEmailOrMobile != ShowEmailOrMobile.EmailOptions) {
                    showEmailOrMobile(ShowEmailOrMobile.EmailOptions)
                }
            }
        } else {
            if (focus) {
                if (showEmailOrMobile != ShowEmailOrMobile.DefaultOptions) {
                    showEmailOrMobile(ShowEmailOrMobile.DefaultOptions)
                }
            } else {
                if (showEmailOrMobile != ShowEmailOrMobile.NoOptions) {
                    showEmailOrMobile(ShowEmailOrMobile.NoOptions)
                }
            }

        }
    }

    var lastClickTime: Long = 0

    fun focusChanged(focus: Boolean, show: ShowEmailOrMobile) {
        viewModelScope.launch {
            if (!emailOrMobileState.value.isNullOrEmpty()) {
                if (isValidNumber(emailOrMobileState.value)) {
                    if (show != ShowEmailOrMobile.MobileOptions) {
                        showEmailOrMobile(ShowEmailOrMobile.MobileOptions)
                    }
                } else {
                    if (show != ShowEmailOrMobile.EmailOptions) {
                        showEmailOrMobile(ShowEmailOrMobile.EmailOptions)
                    }
                }
            } else {
                if (focus) {
                    if (show != ShowEmailOrMobile.DefaultOptions) {
                        showEmailOrMobile(ShowEmailOrMobile.DefaultOptions)
                    }
                } else {
                    if (show != ShowEmailOrMobile.NoOptions) {
                        showEmailOrMobile(ShowEmailOrMobile.NoOptions)
                    }
                }
            }
        }
    }

    init {
        viewModelScope.launch {

            countryCode = bootstrapRepository.getCachedBootstrap()?.countryCode
        }
    }
}
