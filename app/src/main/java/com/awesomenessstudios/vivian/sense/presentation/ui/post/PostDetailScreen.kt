package com.awesomenessstudios.vivian.sense.presentation.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import com.awesomenessstudios.vivian.sense.presentation.ui.components.CommentItem
import com.awesomenessstudios.vivian.sense.presentation.ui.components.PostCard
import com.awesomenessstudios.vivian.sense.presentation.viewmodels.PostDetailViewModel
import java.util.*


@Composable
fun PostDetailScreen(
    postId: String,
    postDetailViewModel: PostDetailViewModel = hiltViewModel()
) {

    val uiState = postDetailViewModel.postDetailState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        postDetailViewModel.onPostDetailEvent(PostDetailUiEvent.GetCurrentUser)
        postDetailViewModel.onPostDetailEvent(PostDetailUiEvent.LoadPostDetail(postId))
        postDetailViewModel.onPostDetailEvent(PostDetailUiEvent.LoadPostComments(postId))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp) // padding to prevent input bar from overlapping
    ) {
        // Post at the top
        PostCard(
            post = uiState.postDetail,
            currentUser = uiState.currentUser,
            onLikeClick = { postDetailViewModel.onPostDetailEvent(PostDetailUiEvent.LikePost(uiState.postDetail!!.id)) },
            onDeleteClick = {
                postDetailViewModel.onPostDetailEvent(
                    PostDetailUiEvent.DeletePost(
                        uiState.postDetail!!.id
                    )
                )
            },
            onCommentClick = {}, // already in detail
            onPostClick = {}, // no-op in detail
            onShareClick = { }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Comments",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.titleMedium
        )

        // Comment list
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(uiState.comments) { comment ->
                CommentItem(comment)
                Divider()
            }
        }
    }

    // Bottom input bar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = uiState.commentText,
                onValueChange = {
                    postDetailViewModel.onPostDetailEvent(
                        PostDetailUiEvent.OnCommentTextChanged(
                            it
                        )
                    )
                },
                placeholder = { Text("Write a comment...") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors().copy(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                maxLines = 3,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    postDetailViewModel.onPostDetailEvent(
                        PostDetailUiEvent.WriteComment(
                            uiState.commentText,
                            postId,
                            null
                        )
                    )
                })
            )

            IconButton(onClick = {

                postDetailViewModel.onPostDetailEvent(
                    PostDetailUiEvent.WriteComment(
                        uiState.commentText,
                        postId,
                        null
                    )
                )
            }) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Comment",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

