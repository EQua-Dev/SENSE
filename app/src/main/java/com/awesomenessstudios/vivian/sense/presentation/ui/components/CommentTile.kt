package com.awesomenessstudios.vivian.sense.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.awesomenessstudios.vivian.sense.R
import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.ml.models.SentimentLabel

@Composable
fun CommentItem(
    comment: SenseComment,
    isPostOwner: Boolean = false
) {
    var showNegativeComment by remember { mutableStateOf(false) }

    // Determine if comment should be obfuscated
    val shouldObfuscate = isPostOwner &&
            comment.sentiment != null &&
            (comment.sentiment.label == SentimentLabel.NEGATIVE.name.lowercase() ||
                    comment.sentiment.label == SentimentLabel.VERY_NEGATIVE.name.lowercase()) &&
            !showNegativeComment

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // Profile picture/avatar
        if (comment.user?.profilePicture?.isNotEmpty() == true) {
            AsyncImage(
                model = comment.user.profilePicture,
                contentDescription = "User Profile",
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                placeholder = painterResource(id = R.drawable.ic_default_profile),
                error = painterResource(id = R.drawable.ic_default_profile),
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = comment.user?.displayName?.firstOrNull()?.toString() ?: "U",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            // Username
            Text(
                text = comment.user?.displayName ?: "Unknown",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )

            if (shouldObfuscate) {
                // Show warning message instead of content
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Potentially negative comment hidden",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showNegativeComment = true },
                            modifier = Modifier.align(Alignment.Start),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Show anyway",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            } else {
                // Show actual comment content
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Show sentiment info (always visible)
            comment.sentiment?.let { sentiment ->
                val sentimentLabel = SentimentLabel.valueOf(sentiment.label.uppercase())
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = sentimentLabel.getEmoji(),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${sentimentLabel.getDescription()} (${
                            String.format(
                                "%.1f",
                                sentiment.confidence * 100
                            )
                        }%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(sentimentLabel.getColor())
                    )
                }
            }
        }
    }
}