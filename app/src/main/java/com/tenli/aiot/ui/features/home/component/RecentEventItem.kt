package com.tenli.aiot.ui.features.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tenli.aiot.R
import com.tenli.aiot.data.mapper.EventProcessor
import com.tenli.aiot.model.network.EventItem
import com.tenli.aiot.ui.theme.spacing
import com.tenli.aiot.ui.utils.bounceClick

@Composable
fun RecentEventItem(event: EventItem, onClick: () -> Unit) {
    val context = LocalContext.current
    val uiConfig = remember(event.eType, event.eTypeGroup) {
        EventProcessor.getUIConfig(event.eType, event.eTypeGroup)
    }

    val annotatedTitle = remember(event.id) {
        buildAnnotatedString {
            val description = event.actionPart

            if (event.eType == "new-device-login") {
                val saved = event.eValues?.get("saved") as? Map<*, *>
                val loginName = saved?.get("name")?.toString() ?: "không rõ"

                append("Thiết bị ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(loginName)
                }
                append(" vừa đăng nhập vào tài khoản của bạn")
            } else {
                val profileName = event.eValues?.get("profileName") as? String
                if (profileName != null && description.contains(profileName)) {
                    val before = description.substringBefore(profileName)
                    val after = description.substringAfter(profileName)
                    append(before)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(profileName) }
                    append(after)
                } else {
                    append(description)
                }

                append(event.foundConnector)

                if (event.cameraName.isNotEmpty()) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(event.cameraName) }
                }
                if (event.deviceName.isNotEmpty()) {
                    append(" (")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(event.deviceName) }
                    append(")")
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = MaterialTheme.spacing.medium, end = MaterialTheme.spacing.medium, top = 3.dp, bottom = 10.dp)
            .bounceClick { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .height(70.dp)
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(event.localImageUrl)
                        .addHeader("Authorization", "Bearer ${event.deviceKey ?: ""}")
                        .size(width = 320, height = 180)
                        .crossfade(true)
                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                        .build(),
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.image_default),
                    error = painterResource(R.drawable.image_default),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )

                val iconColor = uiConfig.color
                if (iconColor != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = MaterialTheme.spacing.extraSmall, y = MaterialTheme.spacing.extraSmall)
                            .size(25.dp)
                            .background(iconColor, CircleShape)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = uiConfig.iconRes),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = MaterialTheme.spacing.extraSmall)
            ) {
                Text(
                    text = annotatedTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                Text(
                    text = event.timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 5.dp)
            ) {
                if (event.isNew) {
                    Box(
                        modifier = Modifier
                            .size(MaterialTheme.spacing.small)
                            .background(Color(0xFFE42E1B), CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}