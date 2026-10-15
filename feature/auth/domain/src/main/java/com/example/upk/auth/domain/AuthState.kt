package com.example.upk.auth.domain

sealed interface AuthState {
    data object RestoringSession : AuthState

    data object CheckingSession : AuthState

    data object SignedOut : AuthState

    data class SignedIn(
        val userId: String,
    ) : AuthState

    data object SessionCheckUnavailable : AuthState

    data object ReauthenticationRequired : AuthState
}