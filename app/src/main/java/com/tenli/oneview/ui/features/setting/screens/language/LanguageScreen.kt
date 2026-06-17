package com.tenli.oneview.ui.features.setting.screens.language

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tenli.oneview.R
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.LocaleManager

@Composable
fun LanguageScreen() {
    val context = LocalContext.current
    val savedLanguage = remember { LocaleManager.getLocale(context) }
    var selectedLanguage by remember { mutableStateOf(savedLanguage) }

    fun handleLanguageChange(lang: String) {
        if (selectedLanguage == lang) return // Nếu chọn lại ngôn ngữ cũ thì không làm gì
        selectedLanguage = lang
        LocaleManager.setLocale(context, lang)
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        (context as? Activity)?.finish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
    ) {
        Text(
            text = "Chọn ngôn ngữ",
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 5.dp, start = MaterialTheme.spacing.small)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
            color = Color.White,
        ) {
            Column {
                LanguageItem(
                    flagRes = R.drawable.vietnam,
                    name = "Tiếng Việt",
                    isSelected = selectedLanguage == "vi",
                    onClick = { handleLanguageChange("vi") }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                    thickness = MaterialTheme.spacing.borderThin,
                    color = Color.LightGray.copy(alpha = 0.3f)
                )

                LanguageItem(
                    flagRes = R.drawable.kingdom,
                    name = "English",
                    isSelected = selectedLanguage == "en",
                    showDivider = false,
                    onClick = { handleLanguageChange("en") }
                )
            }
        }
    }
}

@Composable
fun LanguageItem(
    flagRes: Int,
    name: String,
    isSelected: Boolean,
    showDivider: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(MaterialTheme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = flagRes),
            contentDescription = null,
            modifier = Modifier.size(MaterialTheme.spacing.iconMedium)
        )

        Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

        Text(
            text = name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check, // Icon check mặc định [cite: 2026-03-15]
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}