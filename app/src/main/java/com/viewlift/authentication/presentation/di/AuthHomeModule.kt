package com.viewlift.authentication.presentation.di

import com.viewlift.core.navigation.NavigationFactory
import com.viewlift.authentication.presentation.AuthNavigationFactory
import com.viewlift.authentication.presentation.uistate.SocialLoginUiState
import com.viewlift.authentication.presentation.uistate.VerifyOTPScreenUiState
import com.viewlift.authentication.presentation.uistate.UpdateAccountUiState
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton


@Module
@InstallIn(ViewModelComponent::class)
object AuthLoginPageViewModelModule {

    @Provides
    fun provideLoginAuthInitialPageUiState(): VerifyOTPScreenUiState = VerifyOTPScreenUiState(false, null,null,false)

    @Provides
    fun provideLoginUpdateAccountUiState(): UpdateAccountUiState = UpdateAccountUiState(false, null,false)

    @Provides
    fun provideGoogleLoginUiState(): SocialLoginUiState = SocialLoginUiState()

}

/**
 * Home singleton module
 *
 * @constructor Create empty Home singleton module
 */
@Module
@InstallIn(SingletonComponent::class)
interface AuthSingletonModule {

    /**
     * Bind home navigation factory
     *
     * @param factory
     * @return
     */
    @Singleton
    @Binds
    @IntoSet
    fun bindAuthNavigationFactory(factory: AuthNavigationFactory): NavigationFactory
}
