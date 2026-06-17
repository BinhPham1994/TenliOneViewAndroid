package com.tenli.aiot.main

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tenli.aiot.ui.features.auth.forgot.ForgotPassScreen
import com.tenli.aiot.ui.features.auth.login.LoginScreen
import com.tenli.aiot.ui.features.auth.register.RegisterScreen
import com.tenli.aiot.ui.theme.TenliAIoTTheme
import com.tenli.aiot.ui.utils.LocaleManager

class LoginActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        super.onCreate(savedInstanceState)

        setContent {
            TenliAIoTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                onNavigateToForgot = {
                                    navController.navigate("forgot_pass")
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onFinish = { navController.popBackStack() }
                            )
                        }
                        composable("forgot_pass") {
                            ForgotPassScreen(
                                onFinish = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val context = LocaleManager.updateResources(newBase)
        super.attachBaseContext(context)
    }
}