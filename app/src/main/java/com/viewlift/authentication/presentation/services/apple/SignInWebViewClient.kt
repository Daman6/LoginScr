package com.viewlift.authentication.presentation.services.apple

import android.graphics.Bitmap
import android.os.Handler
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.web.AccompanistWebViewClient
import com.viewlift.authentication.presentation.viewmodel.AuthViewModel
import java.lang.Exception

internal class SignInWebViewClient(
    private val javascriptToInject: String,
    val viewModel: AuthViewModel
) : AccompanistWebViewClient() {

    var mainHandler= Handler()
    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {

        if(request?.method == "POST" && request?.url.toString().contains(REDIRECT_URI)){
            try {
                Thread.currentThread().interrupt()
            }catch (ex: Exception){}

            mainHandler.post {
                view?.stopLoading()
                view?.loadUrl("javascript:$javascriptToInject")
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        viewModel.fullScreenLoader.value = true
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        viewModel.fullScreenLoader.value = false
        super.onPageFinished(view, url)

    }
}
