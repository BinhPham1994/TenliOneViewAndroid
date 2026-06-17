package com.tenli.oneview

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.tenli.oneview.main.LoginActivity
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.ui.features.splash.SplashDestination
import com.tenli.oneview.ui.features.splash.SplashViewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.uiState.value.destination is SplashDestination.Loading
        }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(uiState.destination) {
                val eventId = intent.getStringExtra("EVENT_ID")
                val isFromCall = intent.getBooleanExtra("FROM_CALL", false)
                val isFromNotify = intent.getBooleanExtra("FROM_NOTIFICATION", false)

                when (uiState.destination) {
                    is SplashDestination.Main -> {
                        navigateToMain(eventId, isFromCall, isFromNotify)
                    }
                    is SplashDestination.Login -> {
                        navigateToLogin()
                    }
                    is SplashDestination.Loading -> { /* Vẫn đang đợi, không làm gì */ }
                }
            }
        }
    }

    private fun navigateToMain(eventId: String?, fromCall: Boolean, fromNotify: Boolean) {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            if (!eventId.isNullOrEmpty()) {
                putExtra("OPEN_EVENT_DETAIL", eventId)
                putExtra("FROM_CALL", fromCall)
                putExtra("FROM_NOTIFICATION", fromNotify)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(mainIntent)
        finish()
    }

    private fun navigateToLogin() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivity(loginIntent)
        finish()
    }
}