package com.viewlift.authentication.presentation.uicomponents

import android.view.ViewTreeObserver
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.viewlift.authentication.presentation.authUtils.*
import com.viewlift.authentication.presentation.intent.AuthLoginIntent
import com.viewlift.authentication.presentation.uistate.ShowEmailOrMobile
import com.viewlift.authentication.presentation.viewmodel.AuthViewModel
import com.viewlift.authentication.presentation.viewmodel.UpdateAccountViewModel
import com.viewlift.authentication.presentation.viewmodel.VerifyOTPViewModel
import com.viewlift.common.label.*
import com.viewlift.core.extensions.collectAsStateWithLifecycle
import com.viewlift.common.ui.Typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmailEditText(
    bringIntoViewRequester: BringIntoViewRequester,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val emailMobileLoginButtons by viewModel.emailMobileLoginButtons.collectAsStateWithLifecycle(
        listOf()
    )

    if (emailMobileLoginButtons.isNotEmpty() && emailMobileLoginButtons[0].enable == true) {
        val showEmailOrMobile by viewModel.isEmailOrMobile.collectAsStateWithLifecycle(
            ShowEmailOrMobile.NoOptions
        )

        Column {
            Row(
                modifier = Modifier
            ) {
                // Show Country Code When we have Mobile
                val editTextModifier: Modifier
                if (showEmailOrMobile == ShowEmailOrMobile.MobileOptions) {
                    val checkboxModifier = Modifier.weight(0.2f)
                    editTextModifier = Modifier.weight(0.8f)

                    defaultCountry = viewModel.countryCodeResponses?.find { it.code == viewModel.countryCode }

                    CountryCodeDropDown(modifier = checkboxModifier, viewModel.countryCodeResponses)

                    Spacer(modifier = Modifier.width(5.dp))
                } else {
                    editTextModifier = Modifier.weight(1f)
                }

                Column(
                    modifier = editTextModifier
                ) {
                    CustomEdiTextField(
                        bringIntoViewRequester
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (showEmailOrMobile == ShowEmailOrMobile.EmailOptions) {
                Spacer(modifier = Modifier.height(10.dp))

                EmailConsentCheckbox()
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (showEmailOrMobile != ShowEmailOrMobile.NoOptions) {
                // Default View
                Spacer(modifier = Modifier.height(10.dp))

                VerificationButton()

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun EmailConsentCheckbox(viewModel: AuthViewModel = hiltViewModel()) {
    val showEmailConsent by viewModel.showEmailConsent.collectAsStateWithLifecycle(initialValue = false)

    if(showEmailConsent){
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultCheckbox()
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = BootstrapLabels.emailConsentText,
                color = BootstrapColors.generalTextColor.parse,
                style = Typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable {
                    viewModel.checkboxValue.value = !viewModel.checkboxValue.value
                }
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun VerificationButton(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val emailOrMobileText by viewModel.emailOrMobileState.collectAsStateWithLifecycle(initialValue = "")
    val keyboardController = LocalSoftwareKeyboardController.current
    val showEmailOrMobile by viewModel.isEmailOrMobile.collectAsStateWithLifecycle(
        ShowEmailOrMobile.NoOptions
    )

    var isEnabled by remember {
        mutableStateOf(false)
    }

    isEnabled = AuthCommonUtils.getValidButton(
        showEmailOrMobile == ShowEmailOrMobile.EmailOptions,
        emailOrMobileText
    )

    ButtonDefaultLayout(
        modifier = Modifier.height(CommonValues.COMMON_EDITTEXT_HEIGHT),
        buttonTitle = LoginScreenLabels.getVerificationCodeButtonTitle,
        buttonTextColor = BootstrapColors.generalTextColor,
        isEnabled = isEnabled,
        backgroundColor = LoginScreenColors.activeCTAColorLogin,
        disabledBackgroundColor = LoginScreenColors.disabledCTAColorLogin,
        onClick = {
            keyboardController?.hide()
            if (showEmailOrMobile == ShowEmailOrMobile.EmailOptions) {
                if (emailOrMobileText != null && emailOrMobileText?.isBlank() == true || !isEmailValid(
                        emailOrMobileText?.trim()
                    ) || emailOrMobileText!!.trim().length < 6 || emailOrMobileText!!.trim().length > 50
                ) {
                    viewModel.ShowError(Pair(false, "Invalid Email Address"))
                } else {
                    viewModel.acceptIntent(
                        AuthLoginIntent.SendVerificationCode
                    )
                }
            } else {
                if (emailOrMobileText != null && emailOrMobileText?.isBlank() == true || !isValidMobile(
                        emailOrMobileText?.trim()
                    ) || emailOrMobileText!!.trim().length < 5 || emailOrMobileText!!.trim().length > 15
                ) {
                    viewModel.ShowError(Pair(false, "Invalid Phone Number"))
                } else {
                    viewModel.acceptIntent(
                        AuthLoginIntent.SendVerificationCode
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun UpdateAccountAge(
    modifier: Modifier = Modifier,
    label: String,
    type: String,
    bringIntoViewRequester: BringIntoViewRequester,
    viewModel: UpdateAccountViewModel = hiltViewModel()
) {

    var text by remember {
        mutableStateOf("")
    }

    val coroutineScope = rememberCoroutineScope()

    var showError by remember {
        mutableStateOf(false)
    }

    var maxChar = 4
    if (type == "MONTH") {
        maxChar = 2
        viewModel.monthOfAge = if (text.isNotBlank() && isValidInt(text)) text.toInt() else 0
    } else if (type == "DAY") {
        maxChar = 2
        viewModel.dayOfAge = if (text.isNotBlank() && isValidInt(text)) text.toInt() else 0
    } else if (type == "YEAR") {
        maxChar = 4
        viewModel.yearOfAge = if (text.isNotBlank() && isValidInt(text)) text.toInt() else 0
    }

    var lastClickTime: Long = 0
    val focusManager = LocalFocusManager.current

    Box {
            BasicTextField(
                value = text,
                onValueChange = { newText ->
                    if (newText.length <= maxChar) text = newText

                    if(!isValidInt(newText)){
                        showError = true
                        return@BasicTextField
                    }
                    val validationText: String = newText.trim()
                    if (type == "MONTH" && validationText.isNotEmpty()) {
                        if(validationText.toInt() in 2..12){
                            showError = false
                            text = String.format("%02d", newText.toInt())
                            focusManager.moveFocus(FocusDirection.Right)
                        } else if (validationText.toInt() > 12) {
                            showError = true
                        } else if(validationText.toInt() == 1){
                            showError = false
                        }
                    }
                    if (type == "DAY") {
                        if(newText.isEmpty()) focusManager.moveFocus(FocusDirection.Left)
                        else if(newText.length == 1 && newText.toInt() in 1..3){
                            showError = false
                        } else if(newText.length == 1 && newText.toInt() > 3) {
                            showError = false
                            text = String.format("%02d", newText.toInt())
                            focusManager.moveFocus(FocusDirection.Right)
                        } else if(newText.length == 2 && newText.toInt() in 10..31) {
                            showError = false
                            focusManager.moveFocus(FocusDirection.Right)
                        } else if (newText.toInt() > 31) {
                            showError = true
                        }
                    }
                    if (type == "YEAR") {
                        if (validationText.isEmpty()) focusManager.moveFocus(FocusDirection.Left)
                        else showError = !(validationText.length == 4 && AuthCommonUtils.getValidYear(validationText))
                    }
                },
                cursorBrush = SolidColor(Color.Black),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = BootstrapColors.secondayBackground.parse.copy(
                                    alpha = 0.2f
                                ),
                                shape = EditTextShape
                            )
                            .padding(all = 10.dp), // inner padding
                        contentAlignment = Alignment.Center
                    ) {
                        innerTextField()
                    }
                },
                modifier = modifier
                    .onFocusEvent { focusState ->
                        if (System.currentTimeMillis() - lastClickTime < 1000) {
                            return@onFocusEvent
                        }

                        lastClickTime = System.currentTimeMillis()
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                delay(400)
                                bringIntoViewRequester.bringIntoView()
                            }
                        }
                    }
                    .onKeyEvent { event ->
                        if (text.isEmpty() && event.key == Key.Backspace && (type == "DAY" || type == "YEAR")) {
                            focusManager.moveFocus(
                                FocusDirection.Left
                            )
                        }
                        false
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            if (text.isEmpty()) {
                Box(
                    modifier = modifier
                        .padding(all = 10.dp), // inner padding
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = Color.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                Box(
                    modifier = modifier
                        .padding(all = 10.dp), // inner padding
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if(showError){
                        Image(
                            painter = painterResource(com.viewlift.common.R.drawable.ic_error),
                            contentDescription = "",
                            modifier = Modifier
                                .height(20.dp)
                                .width(20.dp),
                            contentScale = ContentScale.FillHeight
                        )
                    }
                }
            }
            BackHandler(enabled = true) {
                if(text.isEmpty()){
                    focusManager.moveFocus(
                        FocusDirection.Right
                    )
                }
            }
    }
}

fun getErrorMessage(type: String): String {
    return when (type) {
        "MONTH" -> {
            "Invalid"
        }
        "DAY" -> {
            "Invalid"
        }
        "YEAR" -> {
            "Invalid"
        }
        else -> {
            "Invalid"
        }
    }
}


@Composable
fun PinView(
    viewModel: VerifyOTPViewModel = hiltViewModel(),
    digitSize: TextUnit = 16.sp,
    containerSize: Dp = digitSize.value.dp * 2,
    digitCount: Int = 6
) {
    val pinText by viewModel.otpState.collectAsStateWithLifecycle(initialValue = "")

    BasicTextField(
        value = pinText,
        onValueChange = {
            viewModel.otpState.value = it
        },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(digitCount) { index ->
                    DigitView(index, pinText, digitSize, containerSize)
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CustomEdiTextField(
    bringIntoViewRequester: BringIntoViewRequester,
    viewModel: AuthViewModel = hiltViewModel()
) {

    val error by viewModel.showError.collectAsStateWithLifecycle(null)

    val textFieldData = viewModel.emailOrMobileText

    val showEmailOrMobile: ShowEmailOrMobile by viewModel.isEmailOrMobile.collectAsStateWithLifecycle(
        ShowEmailOrMobile.NoOptions
    )

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val coroutineScope = rememberCoroutineScope()

    var text by remember {
        mutableStateOf(textFieldData)
    }

    var maxChar = 50

    if (text.isNotBlank()) {
        if (showEmailOrMobile == ShowEmailOrMobile.EmailOptions) {
            maxChar = 50
        } else {
            maxChar = 15
        }
    }

    var lastClickTime: Long = 0

    val view = LocalView.current
    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val isKeyboardOpen = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
            // ... do anything you want here with `isKeyboardOpen`

            if (isKeyboardOpen) {
                // Keyboard open

            } else {
                focusManager.clearFocus()
                viewModel.focusChanged(false, showEmailOrMobile)
            }
        }

        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Box {
        BasicTextField(
            value = text,
            onValueChange = { newText ->
                viewModel.ShowError(null)
                if (newText.length <= maxChar) {
                    text = newText
                    viewModel.handleEditTextData(
                        newText,
                        showEmailOrMobile
                    )
                }
            },
            singleLine = true,
            cursorBrush = SolidColor(Color.White),
            textStyle = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (error?.first == false) Color.Black else Color.White
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    viewModel.acceptIntent(
                        AuthLoginIntent.SendVerificationCode
                    )
                }
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (error?.first == false) LoginScreenColors.errorcolor.parse else LoginScreenColors.inputBgColor.parse,
                            shape = EditTextShape
                        )
                        .border(
                            width = 2.dp,
                            color = if (error?.first == false) LoginScreenColors.activeCTAColorLogin.parse else LoginScreenColors.inputBgColor.parse,
                            shape = EditTextShape
                        )
                        .padding(all = 10.dp), // inner padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(width = 8.dp))
                    innerTextField()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusEvent { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            delay(400)
                            bringIntoViewRequester.bringIntoView()
                        }

                        viewModel.focusChanged(true, showEmailOrMobile)

                    } else {
                        viewModel.focusChanged(false, showEmailOrMobile)
                    }
                }
                .focusRequester(focusRequester)
        )
        if (text.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 10.dp), // inner padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(width = 8.dp))

                Text(
                    text = LoginScreenLabels.emailTextFieldPlaceHolder,
                    color = BootstrapColors.generalTextColor.parse,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun UpdateAccountName(
    hint: String,
    textValue: String = "",
    errorMessage: String? = "",
    viewModel: UpdateAccountViewModel = hiltViewModel()
) {
    var text by remember {
        mutableStateOf(textValue)
    }

    var isEmailOrMobile: ShowEmailOrMobile = ShowEmailOrMobile.EmailOptions
    var maxChar = 50

    if (text.isNotBlank()) {
        if (isValidNumber(text.trim())) {
            isEmailOrMobile = ShowEmailOrMobile.MobileOptions
            maxChar = 15
        } else {
            isEmailOrMobile = ShowEmailOrMobile.EmailOptions
            maxChar = 50
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {

        Spacer(modifier = Modifier.weight(.25f))

        Box(
            modifier = Modifier.weight(9.5f)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { newText ->
                    viewModel.showEditTextError.value = null
                    if (newText.length <= maxChar) {
                        text = newText
                        updateText(hint, viewModel, newText)
                    }
                },
                singleLine = true,
                cursorBrush = SolidColor(Color.Black),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (errorMessage?.isNotBlank() == true) LoginScreenColors.errorcolor.parse else Color.White,
                                shape = EditTextShape
                            )
                            .border(
                                width = 2.dp,
                                color = if (errorMessage?.isNotBlank() == true) LoginScreenColors.activeCTAColorLogin.parse else Color.White,
                                shape = EditTextShape
                            )
                            .padding(all = 10.dp), // inner padding
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(width = 2.dp))
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = getInputType(hint), imeAction = ImeAction.Done)
            )

            HintAndError(text, hint, errorMessage)
        }
        Spacer(modifier = Modifier.weight(.25f))
    }
}

@Composable
private fun HintAndError(edittextValue: String, hint: String, errorMessage: String?) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp), // inner padding,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    Spacer(modifier = Modifier.width(width = 2.dp))
                    Text(
                        text = if (edittextValue.trim().isEmpty()) hint else "",
                        color = Color.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun updateText(
    hint: String,
    viewModel: UpdateAccountViewModel,
    newText: String
) {
    when (hint) {
        UpdateScreenLabels.emailTextField -> {
            viewModel.userEmail = newText
            viewModel.validateEmail(viewModel.userEmail, viewModel.isEmailRequired)
        }
        UpdateScreenLabels.phoneTextField -> {
            viewModel.userMobile = newText
            viewModel.validateMobile(viewModel.userMobile, viewModel.isMobileRequired)
        }
        UpdateScreenLabels.nameTextField -> {
            viewModel.userName = newText
            viewModel.validateName(viewModel.userName, viewModel.isNameRequired)
        }
    }
}

fun getInputType(
    hint: String
) : KeyboardType {
    return when (hint) {
        UpdateScreenLabels.emailTextField -> {
            KeyboardType.Email
        }
        UpdateScreenLabels.phoneTextField -> {
            KeyboardType.Number
        }
        UpdateScreenLabels.nameTextField -> {
            KeyboardType.Text
        } else -> {
            KeyboardType.Text
        }
    }
}


@Composable
private fun DigitView(
    index: Int,
    pinText: String,
    digitSize: TextUnit,
    containerSize: Dp
) {

    if (index >= pinText.length) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(40.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(5.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.size(12.dp),
                shape = CircleShape
            ) {
                Image(
                    painter = painterResource(com.viewlift.common.R.drawable.ic_ellipse),
                    contentDescription = "",
                    modifier = Modifier
                        .height(12.dp)
                        .width(12.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

    } else {

        Box(
            modifier = Modifier
                .height(40.dp)
                .width(40.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(5.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (index >= pinText.length) "" else pinText[index].toString(),
                color = Color.Black,
                style = MaterialTheme.typography.body1,
                fontSize = digitSize,
                textAlign = TextAlign.Center
            )
        }
    }
}