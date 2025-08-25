package com.awesomenessstudios.vivian.sense.presentation.ui.analytics

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.awesomenessstudios.vivian.sense.data.models.CommentsPostedAnalytics
import com.awesomenessstudios.vivian.sense.data.models.CommentsReceivedAnalytics
import com.awesomenessstudios.vivian.sense.data.models.EngagementAnalytics
import com.awesomenessstudios.vivian.sense.data.models.OverviewStats
import com.awesomenessstudios.vivian.sense.data.models.PostsAnalytics
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.SentimentTrends
import com.awesomenessstudios.vivian.sense.presentation.viewmodels.AnalyticsViewModel
import com.awesomenessstudios.vivian.sense.presentation.viewmodels.PostsDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsAnalyticsDetailScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onPostClick: (String) -> Unit,
    postsDetailViewModel: PostsDetailViewModel = hiltViewModel(),
) {
    val uiState = postsDetailViewModel.postsDetailState.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) {
        postsDetailViewModel.onEvent(PostsDetailUiEvent.LoadPostsDetail)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Posts Analytics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Sort dropdown
                    Box {
                        var expanded by remember { mutableStateOf(false) }

                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            PostsSortType.values().forEach { sortType ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = when (sortType) {
                                                PostsSortType.MOST_RECENT -> "Most Recent"
                                                PostsSortType.MOST_LIKED -> "Most Liked"
                                                PostsSortType.MOST_COMMENTED -> "Most Commented"
                                                PostsSortType.BEST_SENTIMENT -> "Best Sentiment"
                                            }
                                        )
                                    },
                                    onClick = {
                                        postsDetailViewModel.onEvent(
                                            PostsDetailUiEvent.ChangeSorting(
                                                sortType
                                            )
                                        )
                                        expanded = false
                                    },
                                    leadingIcon = if (uiState.sortedBy == sortType) {
                                        { Icon(Icons.Default.Check, contentDescription = null) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Analytics Summary Card
                item {
                    PostsAnalyticsSummaryCard(analytics = uiState.analytics)
                }

                // Posts List Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Posts (${uiState.posts.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sorted by: ${getSortDisplayName(uiState.sortedBy)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Posts List
                items(uiState.posts) { post ->
                    PostAnalyticsCard(
                        post = post,
                        onPostClick = { onPostClick(post.id) }
                    )
                }
            }
        }

        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Handle error display
            }
        }
    }
}

@Composable
fun PostsAnalyticsSummaryCard(analytics: PostsAnalytics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Posts Performance Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Performance metrics grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    MetricCard(
                        "Total Posts",
                        analytics.totalPosts.toString(),
                        Icons.Default.Article
                    )
                }
                item {
                    MetricCard(
                        "Avg Likes",
                        analytics.averageLikes.toString(),
                        Icons.Default.Favorite
                    )
                }
                item {
                    MetricCard(
                        "Avg Comments",
                        analytics.averageComments.toString(),
                        Icons.Default.Comment
                    )
                }
                item {
                    MetricCard(
                        "Avg Shares",
                        analytics.averageShares.toString(),
                        Icons.Default.Share
                    )
                }
            }
        }
    }
}

@Composable
fun PostAnalyticsCard(
    post: SensePost,
    onPostClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPostClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Post content preview
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Engagement metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Likes",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Red
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likeCount.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Comment,
                        contentDescription = "Comments",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.commentCount.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Shares",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.shareCount.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Post date
                Text(
                    text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(post.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Total engagement score
            val totalEngagement = post.likeCount + post.commentCount + post.shareCount
            if (totalEngagement > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (totalEngagement / 100f).coerceAtMost(1f) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Total Engagement: $totalEngagement",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: ImageVector) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getSortDisplayName(sortType: PostsSortType): String {
    return when (sortType) {
        PostsSortType.MOST_RECENT -> "Most Recent"
        PostsSortType.MOST_LIKED -> "Most Liked"
        PostsSortType.MOST_COMMENTED -> "Most Commented"
        PostsSortType.BEST_SENTIMENT -> "Best Sentiment"
    }
}

// ViewModel for Posts Detail Screen

// UI Events for Posts Detail
sealed class PostsDetailUiEvent {
    object LoadPostsDetail : PostsDetailUiEvent()
    data class ChangeSorting(val sortType: PostsSortType) : PostsDetailUiEvent()
}