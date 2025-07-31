package com.awesomenessstudios.vivian.sense.presentation.ui.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.awesomenessstudios.vivian.sense.R
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import com.awesomenessstudios.vivian.sense.utils.HelpMe.formatDate
import java.util.*

@Composable
fun PostCard(
    modifier: Modifier = Modifier,
    post: SensePost? = null,
    currentUser: SenseUser?,
    onDeleteClick: (String) -> Unit = {},
    onPostClick: (String) -> Unit = {},
    onLikeClick: (String) -> Unit = {},
    onCommentClick: (String) -> Unit = {},
    onShareClick: (String) -> Unit = {},
) {
    if (post == null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { /*onPostClick(post.id)*/ },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Error loading post")
            }
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onPostClick(post.id) },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                // Main content area
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header with user info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Profile picture
                            if (post.user?.profilePicture?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = post.user.profilePicture,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    error = painterResource(id = R.drawable.ic_default_profile),
                                    placeholder = painterResource(id = R.drawable.ic_default_profile),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Profile picture placeholder
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (post.user?.displayName?.firstOrNull()
                                                ?.toString()
                                                ?: "U"),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = post.user?.displayName ?: "Unknown User",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatDate(post.createdAt),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Delete button for own posts
                        if (currentUser?.id == post.userId) {
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(
                                    onClick = { showMenu = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        onClick = {
                                            showMenu = false
                                            onDeleteClick(post.id)
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Post content
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Post image if available
                    if (post.imageUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AsyncImage(
                            model = post.imageUrl,
                            contentDescription = "Post image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.ic_image_placeholder),
                            placeholder = painterResource(id = R.drawable.ic_image_placeholder)
                        )
                    }
                }

                // Bottom action bar
                Divider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Like button
                    PostActionButton(
                        icon = if (post.isLikedByCurrentUser) Icons.Default.Favorite else Icons.Filled.FavoriteBorder, // Use Icons.Default.Favorite for liked state
                        label = "Like",
                        count = post.likeCount,
                        isActive = post.isLikedByCurrentUser, // You'll need to add this to SensePost
                        onClick = { onLikeClick(post.id) },
                        activeColor = MaterialTheme.colorScheme.error
                    )

                    // Comment button
                    PostActionButton(
                        icon = Icons.Default.ChatBubbleOutline,
                        label = "Comment",
                        count = post.commentCount,
                        isActive = false,
                        onClick = { onCommentClick(post.id) },
                        activeColor = MaterialTheme.colorScheme.primary
                    )

                    // Share button
                    PostActionButton(
                        icon = Icons.Default.Share,
                        label = "Share",
                        count = post.shareCount, // You'll need to add this to SensePost
                        isActive = false,
                        onClick = { onShareClick(post.id) },
                        activeColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

}

@Composable
private fun PostActionButton(
    icon: ImageVector,
    label: String,
    count: Int,
    isActive: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    modifier: Modifier = Modifier
) {
    val color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
        )
    }
}

