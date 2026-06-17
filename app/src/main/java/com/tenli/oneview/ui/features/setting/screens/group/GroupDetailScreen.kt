package com.tenli.oneview.ui.features.setting.screens.group

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenli.oneview.R
import com.tenli.oneview.ui.component.AppConfirmDialog
import com.tenli.oneview.ui.features.setting.components.SettingPrimaryButton
import com.tenli.oneview.ui.features.setting.core.SettingViewModel
import com.tenli.oneview.ui.features.setting.core.closeShareCodeDialog
import com.tenli.oneview.ui.features.setting.core.confirmDeleteMember
import com.tenli.oneview.ui.features.setting.core.createShareCode
import com.tenli.oneview.ui.features.setting.core.handleGroupAction
import com.tenli.oneview.ui.features.setting.core.setExpiryTime
import com.tenli.oneview.ui.features.setting.core.showConfirmDeleteMember
import com.tenli.oneview.ui.features.setting.core.toggleGroupActionConfirm
import com.tenli.oneview.ui.features.setting.core.toggleMemberMenu
import com.tenli.oneview.ui.theme.spacing

@SuppressLint("DefaultLocale")
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSecs = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSecs)
}

@Composable
fun GroupDetailScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val groupDetail = uiState.group.groupDetail
    val groupInfo = uiState.group.selectedGroup?.group

    val owner = groupDetail?.users?.find { it.role == "owner" }
    val members = groupDetail?.users?.filter { it.role != "owner" } ?: emptyList()
    val isOwner = groupInfo?.userRequestRole == "owner"
    val isMyHome = uiState.title == "Nhà của tôi"

    if (uiState.group.isConfirmDeleteMemberOpen) {
        AppConfirmDialog(
            title = "Xác nhận xóa",
            message = "Bạn có chắc chắn muốn xóa thành viên ${uiState.group.memberToDelete?.name} khỏi nhà không?",
            confirmText = "Xóa",
            cancelText = "Hủy",
            confirmColor = MaterialTheme.colorScheme.error, // Màu đỏ cảnh báo xóa
            iconRes = R.drawable.member,      // Sử dụng icon member để người dùng biết đang tác động đến nhân sự
            onConfirm = { viewModel.confirmDeleteMember() },
            onDismiss = { viewModel.showConfirmDeleteMember(null) }
        )
    }

    if (uiState.group.isShareCodeDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.closeShareCodeDialog() },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { viewModel.closeShareCodeDialog() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Hủy", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mã gia nhập vào nhà là", style = MaterialTheme.typography.bodyLarge)

                    Text(
                        text = uiState.group.shareCode,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.small),
                        color = MaterialTheme.colorScheme.primary
                    )

                    // --- ĐẶT ĐOẠN CODE LOGIC Ở ĐÂY ---
                    val membershipDuration = when (val mins = uiState.group.selectedExpiryMinutes) {
                        null -> "Vĩnh viễn"
                        60 -> "1 giờ"
                        1440 -> "1 ngày"
                        else -> "$mins phút"
                    }

                    // Hiển thị thời hạn của Tư cách thành viên (Dữ liệu bạn gửi lên expiredIn)
                    Text(
                        text = "Thời hạn ở trong nhà: $membershipDuration",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    // Hiển thị thời hạn của Mã số (Mã sẽ vô hiệu sau x giây/phút)
                    Text(
                        text = "Mã sẽ hết hạn sau: ${formatTime(uiState.group.shareCodeExpireTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusLarge),
            containerColor = Color.White
        )
    }

    if (uiState.group.isConfirmGroupActionOpen) { // Bạn hãy thêm biến này vào State nhé
        AppConfirmDialog(
            title = if (isOwner) "Xác nhận xóa nhà" else "Xác nhận rời nhà",
            message = if (isOwner)
                "Bạn có chắc chắn muốn xóa nhà này không? Toàn bộ thiết bị và dữ liệu AI liên quan sẽ bị gỡ bỏ hoàn toàn."
            else "Bạn có chắc chắn muốn rời khỏi nhà này không? Bạn sẽ không còn quyền truy cập vào các thiết bị tại đây.",
            confirmText = if (isOwner) "Xóa ngay" else "Rời đi",
            cancelText = "Hủy",
            confirmColor = MaterialTheme.colorScheme.error, // Màu đỏ cảnh báo
            iconRes = if (isOwner) R.drawable.delete else R.drawable.logout_icon, // Chọn icon phù hợp
            onConfirm = {
                viewModel.toggleGroupActionConfirm(false) // Đóng dialog
                viewModel.handleGroupAction {
                    viewModel.navigateBack()
                }
            },
            onDismiss = { viewModel.toggleGroupActionConfirm(false) }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(MaterialTheme.spacing.medium)
        ) {
            // --- 1. PHẦN CHỦ NHÀ ---
            item { Text("Chủ nhà", style = MaterialTheme.typography.labelLarge, color = Color.Gray) }
            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    owner?.let {
                        GroupMemberItem(name = it.name, role = "Chủ nhà")
                    }
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
            }

            // --- 2. PHẦN THÀNH VIÊN ---
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Thành viên", style = MaterialTheme.typography.labelLarge, color = Color.Gray, modifier = Modifier.weight(1f))
                    if (isOwner) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(MaterialTheme.spacing.iconLarge)
                                    .clickable { viewModel.toggleMemberMenu(true) }
                            )

                            DropdownMenu(
                                expanded = uiState.group.isMemberMenuExpanded,
                                onDismissRequest = { viewModel.toggleMemberMenu(false) },
                                modifier = Modifier
                                    .width(220.dp) // Rộng thêm một chút để các dòng chữ thở được
                                    .background(Color.White, RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                            ) {
                                // --- 1. TIÊU ĐỀ NHÓM THỜI GIAN ---
                                Text(
                                    text = "THỜI HẠN Ở TRONG NHÀ", // Viết hoa nhẹ để tạo cảm giác Header
                                    modifier = Modifier.padding(start = MaterialTheme.spacing.medium, top = 12.dp, end = MaterialTheme.spacing.medium, bottom = MaterialTheme.spacing.small),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )

                                val expiryOptions = listOf(
                                    null to "Vĩnh viễn",
                                    10 to "10 phút",
                                    60 to "1 giờ",
                                    1440 to "1 ngày"
                                )

                                expiryOptions.forEach { (mins, label) ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (uiState.group.selectedExpiryMinutes == mins) MaterialTheme.colorScheme.primary else Color.Unspecified
                                            )
                                        },
                                        trailingIcon = {
                                            if (uiState.group.selectedExpiryMinutes == mins) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        },
                                        onClick = { viewModel.setExpiryTime(mins) },
                                        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small) // Tăng lên 8dp cho thoáng
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.small, horizontal = MaterialTheme.spacing.medium),
                                    thickness = MaterialTheme.spacing.borderMedium,
                                    color = Color.LightGray.copy(alpha = 0.3f)
                                )

                                // --- 2. NHÓM NÚT TẠO MÃ ---
                                Text(
                                    text = "VAI TRÒ GIA NHẬP",
                                    modifier = Modifier.padding(start = MaterialTheme.spacing.medium, top = MaterialTheme.spacing.extraSmall, end = MaterialTheme.spacing.medium, bottom = MaterialTheme.spacing.small),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )

                                // Nút tạo mã Thành viên
                                DropdownMenuItem(
                                    text = { Text("Thành viên", style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.person_icon_green),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {
                                        viewModel.toggleMemberMenu(false)
                                        viewModel.createShareCode("member")
                                    },
                                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium, vertical = 10.dp)
                                )

                                // Nút tạo mã Hỗ trợ kỹ thuật
                                DropdownMenuItem(
                                    text = { Text("Hỗ trợ kỹ thuật", style = MaterialTheme.typography.bodyMedium) },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.person_icon_green),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = Color(0xFFFFA000)
                                        )
                                    },
                                    onClick = {
                                        viewModel.toggleMemberMenu(false)
                                        viewModel.createShareCode("technical")
                                    },
                                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                Card(
                    shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        members.forEachIndexed { index, member ->
                            SwipeableMemberItem(
                                name = member.name,
                                role = when (member.role) {
                                    "technical" -> "Hỗ trợ kĩ thuật"
                                    else -> "Thành viên"
                                },
                                showDivider = index != members.size - 1,
                                isOwner = isOwner,
                                onDelete = {
                                    viewModel.showConfirmDeleteMember(member)
                                }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        if (!isOwner || (!isMyHome)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(MaterialTheme.spacing.medium)
            ) {
                SettingPrimaryButton(
                    text = if (isOwner) "Xóa nhà" else "Rời khỏi nhà",
                    containerColor = Color(0xFFE02B00),
                    isLoading = uiState.account.isUpdating,
                    onClick = {
                        viewModel.toggleGroupActionConfirm(true)
                    }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(MaterialTheme.spacing.medium)
            ) {
                Text(
                    text = "Nhà mặc định không thể xóa",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
            }
        }
    }
}

@Composable
fun GroupMemberItem(name: String, role: String, showDivider: Boolean = false) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.person_icon_green),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
            Column {
                Text(text = name, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = role, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
                thickness = MaterialTheme.spacing.borderThin,
                color = Color.LightGray.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableMemberItem(
    name: String,
    role: String,
    showDivider: Boolean,
    isOwner: Boolean,
    onDelete: () -> Unit
) {
    if (!isOwner) {
        GroupMemberItem(name, role, showDivider)
        return
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false // Nảy lại để hiện Dialog xác nhận
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val isMoving = dismissState.progress > 0f && dismissState.targetValue == SwipeToDismissBoxValue.EndToStart

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isMoving) Color.Red else Color.Transparent,
                        RoundedCornerShape(MaterialTheme.spacing.radiusMedium)
                    )
                    .padding(horizontal = MaterialTheme.spacing.large),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(MaterialTheme.spacing.iconMedium)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Xóa",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        content = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                GroupMemberItem(name, role, showDivider)
            }
        }
    )
}