import re

with open('app/src/main/java/com/tenli/oneview/main/MainActivity.kt', 'r') as f:
    content = f.read()

# Add imports
imports_to_add = """
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenli.oneview.ui.features.splash.SplashDestination
import com.tenli.oneview.ui.features.splash.SplashViewModel
import com.tenli.oneview.ui.features.auth.login.LoginScreen
"""
content = content.replace("import com.tenli.oneview.ui.utils.PermissionUtils", "import com.tenli.oneview.ui.utils.PermissionUtils\n" + imports_to_add.strip())

# Add viewmodel and splash logic
content = content.replace("class MainActivity : ComponentActivity() {\n\n    private var startEventId", "class MainActivity : ComponentActivity() {\n\n    private val splashViewModel: SplashViewModel by viewModels()\n    private var startEventId")
content = content.replace("    override fun onCreate(savedInstanceState: Bundle?) {\n        enableEdgeToEdge(", "    override fun onCreate(savedInstanceState: Bundle?) {\n        val splashScreen = installSplashScreen()\n        enableEdgeToEdge(")
content = content.replace("        super.onCreate(savedInstanceState)\n\n        appUpdateManager", "        super.onCreate(savedInstanceState)\n\n        splashScreen.setKeepOnScreenCondition {\n            splashViewModel.uiState.value.destination is SplashDestination.Loading\n        }\n\n        appUpdateManager")

# Rewrite setContent
set_content_replacement = """        setContent {
            TenliAIoTTheme {
                val uiState by splashViewModel.uiState.collectAsStateWithLifecycle()

                when (uiState.destination) {
                    is SplashDestination.Login -> {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            LoginScreen(
                                onLoginSuccess = { splashViewModel.navigateToMain() }
                            )
                        }
                    }
                    is SplashDestination.Main -> {
                        val context = LocalContext.current
                        val lifecycleOwner = LocalLifecycleOwner.current"""

content = content.replace("        setContent {\n            TenliAIoTTheme {\n                val context = LocalContext.current\n                val lifecycleOwner = LocalLifecycleOwner.current", set_content_replacement)

# End of setContent branches and replace navigateToLogin
content = content.replace("""                AppNavigation(
                    onLogoutRequest = { navigateToLogin() },
                    initialEventId = startEventId
                )

                LaunchedEffect(startEventId) {
                    if (startEventId != null) {
                        kotlinx.coroutines.delay(1000)
                        startEventId = null
                        intent.removeExtra("OPEN_EVENT_DETAIL")
                    }
                }
            }
        }""", """                AppNavigation(
                    onLogoutRequest = { splashViewModel.logout() },
                    initialEventId = startEventId
                )

                LaunchedEffect(startEventId) {
                    if (startEventId != null) {
                        kotlinx.coroutines.delay(1000)
                        startEventId = null
                        intent.removeExtra("OPEN_EVENT_DETAIL")
                    }
                }
                    }
                    is SplashDestination.Loading -> { /* Keep Splash */ }
                }
            }
        }""")

# Delete navigateToLogin function
nav_func = """    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }"""
content = content.replace(nav_func, "")

with open('app/src/main/java/com/tenli/oneview/main/MainActivity.kt', 'w') as f:
    f.write(content)
