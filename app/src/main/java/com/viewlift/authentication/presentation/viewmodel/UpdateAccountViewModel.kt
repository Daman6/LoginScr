package com.viewlift.authentication.presentation.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.api.Optional
import com.viewlift.authentication.domain.usecase.CountryCodeUseCase
import com.viewlift.authentication.domain.usecase.UpdateAccountUseCase
import com.viewlift.authentication.presentation.authUtils.*
import com.viewlift.authentication.presentation.events.HomePageEvent
import com.viewlift.authentication.presentation.intent.HomePageIntent
import com.viewlift.authentication.presentation.uicomponents.defaultCountry
import com.viewlift.authentication.presentation.uistate.ShowEmailOrMobile
import com.viewlift.authentication.presentation.uistate.SocialLoginUiState
import com.viewlift.authentication.presentation.uistate.UpdateAccountUiState
import com.viewlift.authentication.presentation.uistate.UpdateAccountValidation
import com.viewlift.common.label.*
import com.viewlift.common.utils.CommonUtils
import com.viewlift.core.base.BaseViewModel
import com.viewlift.core.data.AppDataRepository
import com.viewlift.core.navigation.NavigationManager
import com.viewlift.network.BootStrapQuery
import com.viewlift.network.PageQuery
import com.viewlift.network.data.remote.model.response.CountryCodeResponse
import com.viewlift.network.data.remote.model.request.UpdateInitialProfileParams
import com.viewlift.network.domain.repository.BootstrapRepository
import com.viewlift.network.type.BettingInput
import com.viewlift.network.type.PreferencesInput
import com.viewlift.network.type.UpdateUserIntitalInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class UpdateAccountViewModel @Inject constructor(
    private val countryCodeUseCase: CountryCodeUseCase,
    val bootstrapRepository: BootstrapRepository,
    var navigationManager: NavigationManager,
    savedStateHandle: SavedStateHandle,
    pageInitialState: UpdateAccountUiState,
    val appDataRepository: AppDataRepository,
    val updateAccountUseCase: UpdateAccountUseCase
) : BaseViewModel<UpdateAccountUiState, UpdateAccountUiState.PartialState, HomePageEvent, HomePageIntent>(
    savedStateHandle,
    pageInitialState
) {
    var isEmailRequired: Boolean = true
    var isNameRequired: Boolean = true
    var isMobileRequired: Boolean = true
    var enableSportsBetting: MutableStateFlow<Boolean> = MutableStateFlow(true)
    var showPersonalizationCategories: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val showEmailConsent: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val checkboxValue: MutableStateFlow<Boolean> = MutableStateFlow(false)

    var isUpdateValid: UpdateAccountValidation? = null
    var isUpdateAccountValid: MutableStateFlow<UpdateAccountValidation?> = MutableStateFlow(null)

    var isNameFieldValid: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isEmailFieldValid: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isPhoneFieldValid: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isBettingValid: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isLocationPermissionValid: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isAgeAbove18Valid: MutableStateFlow<Boolean> = MutableStateFlow(false)

    var isLocationEnabled = false

    fun handleMultipleValidations() {
        viewModelScope.launch {

            combineUpdateValidation(
                isEmailFieldValid,
                isNameFieldValid,
                isPhoneFieldValid,
                isBettingValid,
                isLocationPermissionValid,
                isAgeAbove18Valid
            ) { email, name, phone, betting, location, above18 ->

                isUpdateValid = UpdateAccountValidation(
                    isEmailValid = email,
                    isMobileFieldValid = phone,
                    isNameFieldValid = name,
                    isBettingEnabled = betting,
                    isLocationPermissionGiven = location,
                    isAgeAbove18 = above18
                )
                isUpdateAccountValid.value = isUpdateValid
            }.stateIn(viewModelScope)

        }

    }

    var location: String = ""

    val teams: MutableStateFlow<BootStrapQuery.Teams?> = MutableStateFlow(null)
    val editTextData: MutableStateFlow<UpdateAccountEditTextData?> = MutableStateFlow(null)
    val countryCodes: MutableStateFlow<List<CountryCodeResponse>?> = MutableStateFlow(null)
    val showEditTextError = MutableStateFlow<Pair<String, String>?>(null)
    val showError = MutableStateFlow<String>("")

    //    var countryCodeResponses: List<CountryCodeResponse>? = null
    var countryCode: String? = null

    var userId = ""
    var userName = ""
    var userEmail = ""
    var userMobile = ""

    val listOfTeams: MutableList<String> = mutableListOf()

    var dayOfAge: Int = 0
    var monthOfAge: Int = 0
    var yearOfAge: Int = 0
    var ageOfPerson: Int = 0

    val progressbar: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _performLocationAction: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val performLocationAction = _performLocationAction.asStateFlow()


    fun setPerformLocationAction(request: Boolean) {
        _performLocationAction.value = request
    }

    init {
        viewModelScope.launch {
            getCountryCodes().collect()
        }
    }

    val permissionCheck: MutableStateFlow<Boolean> = MutableStateFlow(false)

    fun getCountryCodes(): Flow<SocialLoginUiState.SocialLoginPartialState> =
        flow {
            countryCodeUseCase().onStart {
                emit(SocialLoginUiState.SocialLoginPartialState.Loading)
            }.collect { result ->
                result.onSuccess { codes ->
                    countryCodes.value = codes?.filter { it.image != null }?.toList()
                }
                result.onFailure { error ->
//                    emit(SocialLoginUiState.SocialLoginPartialState.Error(error))

                    Timber.d("Error - > " + error.message)
                    Timber.d(error.stackTraceToString())
                }
            }
        }

    fun updateUserAccount(input: UpdateInitialProfileParams): Flow<UpdateAccountUiState.PartialState> =
        flow {
            updateAccountUseCase(input)
                .onStart {
                    Timber.d("getPage() -> emit(Loading)-> $input")
                    emit(UpdateAccountUiState.PartialState.Loading)
                }
                .collect { result ->
                    result.onSuccess { page ->
                        if (page?.identityUpdateUserProfileInitial != null) {
                            emit(UpdateAccountUiState.PartialState.Fetched(page))
                        } else {
                            emit(UpdateAccountUiState.PartialState.Error(Throwable("Error Submitting Data")))
                        }
                        Timber.d("getPage() -> " + "success -> " + page?.identityUpdateUserProfileInitial?.name)
                    }
                        .onFailure {
                            Timber.d("getPage() -> " + "emit(Error(it))")
                            emit(UpdateAccountUiState.PartialState.Error(it))
                        }
                }

        }

    override fun reduceUiState(
        previousState: UpdateAccountUiState,
        partialState: UpdateAccountUiState.PartialState
    ): UpdateAccountUiState = when (partialState) {
        is UpdateAccountUiState.PartialState.Loading -> previousState.copy(
            isLoading = true,
            isError = false
        )
        is UpdateAccountUiState.PartialState.Fetched -> previousState.copy(
            isLoading = false,
            updateAccountResponse = partialState.updateAccountResponse,
            isError = false
        )
        is UpdateAccountUiState.PartialState.Error -> previousState.copy(
            isLoading = false,
            isError = true,
            error = partialState.throwable,
            apolloApiError = CommonUtils.getApolloError(partialState.throwable)
        )
    }

    fun openUpdateAccount() {
        val bettingInput = BettingInput(
            enabled = Optional.present(isBettingValid.value),
            age = if (ageOfPerson == 0) Optional.absent() else Optional.present(ageOfPerson),
            location = Optional.present(location)
        )

        val preferenceInput = PreferencesInput(
            teams = Optional.present(listOfTeams.toList()),
            betting = Optional.present(bettingInput)
        )

        val name = if(editTextData.value?.usernameInPref.isNullOrBlank()) {
            Optional.present(userName)
        } else if (editTextData.value?.usernameInPref != userName ){
            Optional.present(userName)
        } else {
            Optional.absent()
        }

        val mobileNumber = "${defaultCountry?.dial_code}${userMobile}"

        viewModelScope.launch {
            val data = UpdateInitialProfileParams(
                site = bootstrapRepository.getCachedMain()?.internalName ?: "",
                input = UpdateUserIntitalInput(
                    userId = userId,
                    name = name,
                    phoneNumber = if(editTextData.value?.phoneInUserPref?.isBlank() == true) Optional.present(mobileNumber) else Optional.absent() ,
                    email = if(editTextData.value?.emailInPref?.isBlank() == true) Optional.present(userEmail) else Optional.absent(),
                    emailConsent =if(editTextData.value?.emailInPref?.isBlank() == true) Optional.present(checkboxValue.value) else Optional.absent(),
                    whatsappConsent = Optional.absent(),
                    preferences = Optional.present(preferenceInput)
                )
            )
            acceptIntent(HomePageIntent.UpdateAccount(data))
        }
    }

    override fun mapIntents(intent: HomePageIntent): Flow<UpdateAccountUiState.PartialState> =
        when (intent) {
            is HomePageIntent.UpdateAccount -> updateUserAccount(intent.updateAccount)
        }

    fun validateAge(
        showCustomDialog: MutableState<Boolean>
    ) {
        val validateDay =
            AuthCommonUtils.getValidDay(dayOfAge.toString())
        val validateMonth =
            AuthCommonUtils.getValidMonth(monthOfAge.toString())
        val validateYear =
            AuthCommonUtils.getValidYear(yearOfAge.toString())

        if (!validateDay) {
            showError.value = "Enter a valid day"
        } else if (!validateMonth) {
            showError.value = "Enter a valid month"
        } else if (!validateYear) {
            showError.value = "Enter a valid year"
        } else {
            val personAge = AuthCommonUtils.getAge(
                year = yearOfAge,
                month = monthOfAge,
                day = dayOfAge
            )

            if (personAge >= 18) {
                showCustomDialog.value = false
                ageOfPerson = personAge
                permissionCheck.value = true
                isAgeAbove18Valid.value = true
            } else {
                ageOfPerson = 0
                showError.value = "You need to be above 18 years of age"
            }
        }
    }

    fun handlePageQuery(pageQueryModule: PageQuery.Module?) {
        viewModelScope.launch {
            countryCode = bootstrapRepository.getCachedBootstrap()?.countryCode

            bootstrapRepository.getCachedMain()?.emailConsent?.let{emailConsent->
                BootstrapLabels.emailConsentText = if(!emailConsent.message.isNullOrEmpty()) emailConsent.message!! else BootstrapLabels.emailConsentText
                checkboxValue.value = emailConsent.isChecked ?: false
                showEmailConsent.value = emailConsent.enableEmailConsent ?: false
            }

            teams.value = bootstrapRepository.getCachedBootstrap()?.appcmsMain?.teams

            userId = appDataRepository.getUserIdString() ?: ""

            pageQueryModule?.let { pageQuery ->
                if (pageQuery.layout != null && pageQuery.layout?.layout != null && pageQuery.layout?.layout?.settings != null) {
                    if (pageQuery.layout!!.layout.settings is LinkedHashMap<*, *>) {

                        val dataHashMap = pageQuery.layout!!.layout.settings as LinkedHashMap<*, *>

                        isEmailRequired = dataHashMap[UpdateScreenKeys.isEmailRequired] as Boolean? ?: false
                        isNameRequired = dataHashMap[UpdateScreenKeys.isNameRequired] as Boolean? ?: false
                        isMobileRequired = dataHashMap[UpdateScreenKeys.isNumberRequired] as Boolean? ?: false
                        enableSportsBetting.value = dataHashMap[UpdateScreenKeys.enableSportsBetting] as Boolean? ?: false
                        showPersonalizationCategories.value = dataHashMap[UpdateScreenKeys.showPersonalizationCategories] as Boolean? ?: false

                        UpdateScreenColors.activeCTAColor = dataHashMap[UpdateScreenKeys.activeCTAColor] as String? ?: UpdateScreenColors.activeCTAColor
                        UpdateScreenColors.activeTeamsBgColor = dataHashMap[UpdateScreenKeys.activeTeamsBgColor] as String? ?: UpdateScreenColors.activeTeamsBgColor
                        UpdateScreenColors.cancelBgColor = dataHashMap[UpdateScreenKeys.cancelBgColor] as String? ?: UpdateScreenColors.cancelBgColor
                        UpdateScreenColors.disabledCTAColor = dataHashMap[UpdateScreenKeys.disabledCTAColor] as String? ?: UpdateScreenColors.disabledCTAColor
                        UpdateScreenColors.disabledTeamsBgColor = dataHashMap[UpdateScreenKeys.disabledTeamsBgColor] as String? ?: UpdateScreenColors.disabledTeamsBgColor
                        UpdateScreenColors.inputColor = dataHashMap[UpdateScreenKeys.inputColor] as String? ?: UpdateScreenColors.inputColor
                        UpdateScreenColors.moduleBackgroundColor = dataHashMap[UpdateScreenKeys.moduleBackgroundColor] as String? ?: UpdateScreenColors.moduleBackgroundColor

                        runBlocking {
                            if(isNameRequired){
                                userName = appDataRepository.getUserName() ?: ""
                            }
                            if(isMobileRequired){
                                userMobile = appDataRepository.getPhoneNumber() ?: ""
                            }
                            if(isEmailRequired){
                                userEmail = appDataRepository.getEmailId() ?: ""
                            }

                            validateName(userName, isNameRequired)
                            validateEmail(userEmail, isEmailRequired)
                            validateMobile(userMobile, isMobileRequired)

                            val data = UpdateAccountEditTextData(
                                isEmailRequired = isEmailRequired,
                                isNameRequired = isNameRequired,
                                isNumberRequired = isMobileRequired,
                                usernameInPref = userName,
                                phoneInUserPref = userMobile,
                                emailInPref = userEmail
                            )
                            editTextData.value = data
                        }
                    }
                }

                if (pageQuery.onUserManagementModule != null && pageQuery.onUserManagementModule?.metadataMap != null) {
                    if (pageQuery.onUserManagementModule!!.metadataMap is LinkedHashMap<*, *>) {
                        val metaDataMap = pageQuery.onUserManagementModule?.metadataMap as LinkedHashMap<String?, String?>
                        UpdateScreenLabels.confirmAgeTitle =
                            if (!metaDataMap[UpdateScreenKeys.confirmAgeTitle].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.confirmAgeTitle].toString() else UpdateScreenLabels.confirmAgeTitle
                        UpdateScreenLabels.enableBetting =
                            if (!metaDataMap[UpdateScreenKeys.enableBetting].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.enableBetting].toString() else UpdateScreenLabels.enableBetting
                        UpdateScreenLabels.addYourInterests =
                            if (!metaDataMap[UpdateScreenKeys.addYourInterests].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.addYourInterests].toString() else UpdateScreenLabels.addYourInterests
                        UpdateScreenLabels.updateAccountTitle =
                            if (!metaDataMap[UpdateScreenKeys.updateAccountTitle].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.updateAccountTitle].toString() else UpdateScreenLabels.updateAccountTitle
                        UpdateScreenLabels.saveAndContinue =
                            if (!metaDataMap[UpdateScreenKeys.saveAndContinue].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.saveAndContinue].toString() else UpdateScreenLabels.saveAndContinue
                        UpdateScreenLabels.enableLocation =
                            if (!metaDataMap[UpdateScreenKeys.enableLocation].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.enableLocation].toString() else UpdateScreenLabels.enableLocation
                        UpdateScreenLabels.locationPermissionDisclaimer =
                            if (!metaDataMap[UpdateScreenKeys.locationPermissionDisclaimer].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.locationPermissionDisclaimer].toString() else UpdateScreenLabels.locationPermissionDisclaimer
                        UpdateScreenLabels.phoneTextField =
                            if (!metaDataMap[UpdateScreenKeys.phoneTextField].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.phoneTextField].toString() else UpdateScreenLabels.phoneTextField
                        UpdateScreenLabels.updateAccountEmailFieldError =
                            if (!metaDataMap[UpdateScreenKeys.updateAccountEmailFieldError].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.updateAccountEmailFieldError].toString() else UpdateScreenLabels.updateAccountEmailFieldError
                        UpdateScreenLabels.updateAccountNameFieldError =
                            if (!metaDataMap[UpdateScreenKeys.updateAccountNameFieldError].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.updateAccountNameFieldError].toString() else UpdateScreenLabels.updateAccountNameFieldError
                        UpdateScreenLabels.updateAccountPhoneFieldError =
                            if (!metaDataMap[UpdateScreenKeys.updateAccountPhoneFieldError].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.updateAccountPhoneFieldError].toString() else UpdateScreenLabels.updateAccountPhoneFieldError

                        UpdateScreenLabels.nameTextField =
                                if (!metaDataMap[UpdateScreenKeys.nameTextField].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.nameTextField].toString() else UpdateScreenLabels.nameTextField

                        UpdateScreenLabels.emailTextField =
                                if (!metaDataMap[UpdateScreenKeys.emailTextField].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.emailTextField].toString() else UpdateScreenLabels.emailTextField

                        UpdateScreenLabels.cancel =
                                if (!metaDataMap[UpdateScreenKeys.cancel].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.cancel].toString() else UpdateScreenLabels.cancel

                        UpdateScreenLabels.emailAlreadyLinked =
                                if (!metaDataMap[UpdateScreenKeys.emailAlreadyLinked].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.emailAlreadyLinked].toString() else UpdateScreenLabels.emailAlreadyLinked

                        UpdateScreenLabels.phoneAlreadyLinked =
                                if (!metaDataMap[UpdateScreenKeys.phoneAlreadyLinked].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.phoneAlreadyLinked].toString() else UpdateScreenLabels.phoneAlreadyLinked

                        UpdateScreenLabels.turnOnLocation =
                                if (!metaDataMap[UpdateScreenKeys.turnOnLocation].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.turnOnLocation].toString() else UpdateScreenLabels.turnOnLocation

                        UpdateScreenLabels.EMAIL_ALREADY_LINKED =
                                if (!metaDataMap[UpdateScreenKeys.EMAIL_ALREADY_LINKED].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.EMAIL_ALREADY_LINKED].toString() else UpdateScreenLabels.EMAIL_ALREADY_LINKED

                        UpdateScreenLabels.PHONE_ALREADY_LINKED =
                                if (!metaDataMap[UpdateScreenKeys.PHONE_ALREADY_LINKED].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.PHONE_ALREADY_LINKED].toString() else UpdateScreenLabels.PHONE_ALREADY_LINKED

                        UpdateScreenLabels.INVALID_USER =
                                if (!metaDataMap[UpdateScreenKeys.INVALID_USER].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.INVALID_USER].toString() else UpdateScreenLabels.INVALID_USER

                        UpdateScreenLabels.INTERNAL_ERROR =
                                if (!metaDataMap[UpdateScreenKeys.INTERNAL_ERROR].isNullOrEmpty()) metaDataMap[UpdateScreenKeys.INTERNAL_ERROR].toString() else UpdateScreenLabels.INTERNAL_ERROR

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

                    bootstrapRepository.getCachedBrand()?.player?.progressBarColor?.let {
                        BootstrapColors.progressBarColor = it
                    }

                    bootstrapRepository.getCachedBrand()?.player?.progressBarBackgroundColor?.let {
                        BootstrapColors.progressBarBackgroundColor = it
                    }
                }
            }
        }
    }

    fun validateMobile(mobileNumber: String?, isMobileRequired: Boolean){
        if(isMobileRequired){
            isPhoneFieldValid.value = isValidMobile(mobileNumber)
        } else {
            isPhoneFieldValid.value = true
        }
    }
    fun validateName(username: String?, isNameRequired: Boolean){
        if(isNameRequired){
            isNameFieldValid.value = !username.isNullOrBlank()
        } else {
            isNameFieldValid.value = true
        }
    }
    fun validateEmail(emailData: String?, isEmailRequired: Boolean){
        if(isEmailRequired){
            isEmailFieldValid.value = isEmailValid(emailData)
        } else {
            isEmailFieldValid.value = true
        }
    }

    companion object {
        const val PHONE_TEXTFIELD = "PHONE_TEXTFIELD"
        const val EMAIL_TEXTFIELD = "EMAIL_TEXTFIELD"
        const val NAME_TEXTFIELD = "NAME_TEXTFIELD"
    }
}

data class UpdateAccountEditTextData(
    var isEmailRequired: Boolean = false,
    var isNameRequired: Boolean = false,
    var isNumberRequired: Boolean = false,
    var usernameInPref: String? = null,
    var phoneInUserPref: String? = null,
    var emailInPref: String? = null
)