package com.awesomenessstudios.vivian.sense.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.awesomenessstudios.vivian.sense.R
import com.awesomenessstudios.vivian.sense.data.models.SenseComment

@Composable
fun CommentItem(comment: SenseComment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
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
            Text(
                text = comment.user?.displayName ?: "Unknown",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodySmall
            )
            comment.sentiment?.let {
                Text(
                    text = "Sentiment: ${it.label} (${String.format("%.2f", it.confidence * 100)}%)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
