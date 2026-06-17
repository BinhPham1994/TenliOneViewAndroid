package com.tenli.oneview.ui.features.setting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tenli.oneview.R
import com.tenli.oneview.ui.theme.spacing

@Composable
fun UserAvatarBox(url: String?, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.LightGray.copy(alpha = 0.2f))
    ) {
        AsyncImage(
            model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.avatar_default), error = painterResource(R.drawable.avatar_default)
        )
    }
}

@Composable
fun ProfileGenderDropdown(currentGender: Int, onGenderSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf(1 to "Nam", 2 to "Nữ", 0 to "Khác")
    val genderText = when (currentGender) {
        1 -> "Nam"; 2 -> "Nữ"; else -> "Khác"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.spacing.medium)
    ) {
        Text("Giới tính", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = MaterialTheme.spacing.small))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                    .background(Color.White)
                    .clickable { expanded = true }
                    .padding(horizontal = MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(genderText, modifier = Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
            }
            DropdownMenu(
                expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(Color.White)
            ) {
                genderOptions.forEach { (value, label) ->
                    DropdownMenuItem(text = { Text(label) }, onClick = { onGenderSelect(value); expanded = false })
                }
            }
        }
    }
}