package com.spaceapp.presentation.space_news

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spaceapp.R
import com.spaceapp.core.common.DateFormatter
import com.spaceapp.core.navigation.NavName
import com.spaceapp.core.navigation.NavScreen
import com.spaceapp.core.ui.component.*
import com.spaceapp.presentation.utils.NewsScreenConstants
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val constants = NewsScreenConstants

@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    viewModel: SpaceNewsViewModel = hiltViewModel(),
    navController: NavController
) {
    val spaceNewsState by viewModel.spaceNewsState.collectAsState()
    val weatherConditionState by viewModel.weatherConditionState.collectAsState()

    val activity = LocalContext.current as Activity
    OnBackPressed(activity = activity)

    NewsContent(
        modifier = modifier,
        spaceNewsState = spaceNewsState,
        weatherConditionState = weatherConditionState,
        navController = navController,
        viewModel = viewModel
    )
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
private fun NewsContent(
    modifier: Modifier,
    spaceNewsState: SpaceNewsState,
    weatherConditionState: WeatherConditionState,
    navController: NavController,
    viewModel: SpaceNewsViewModel
) {
    Scaffold(modifier = modifier.fillMaxSize()) {
        BackgroundImage(modifier = modifier.fillMaxSize(), imageId = R.drawable.background_image)
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            WelcomeSection(
                modifier = modifier,
                weatherConditionState = weatherConditionState
            )
            LatestNewsSection(
                modifier = modifier,
                spaceNewsState = spaceNewsState,
                navController = navController,
                viewModel = viewModel
            )
            NewsSection(
                modifier = modifier,
                spaceNewsState = spaceNewsState,
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun WelcomeSection(
    modifier: Modifier,
    weatherConditionState: WeatherConditionState
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "HELLO EXPLORER", style = MaterialTheme.typography.h1)
            Text(text = constants.welcome_subtitle, style = MaterialTheme.typography.h4)
        }
        CurrentWeatherInfo(modifier = modifier, weatherConditionState = weatherConditionState)
    }
}

@Composable
private fun CurrentWeatherInfo(modifier: Modifier, weatherConditionState: WeatherConditionState) {
    when (weatherConditionState) {
        is WeatherConditionState.Loading -> {
            LoadingSpinner(modifier = modifier.fillMaxWidth())
        }
        is WeatherConditionState.Success -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom,
            ) {
                Image(
                    modifier = modifier
                        .size(48.dp)
                        .padding(end = 4.dp),
                    painter = painterResource(id = R.drawable.broken_clouds_day),
                    contentDescription = null,
                )
                Text(
                    text = "${weatherConditionState.data!!.temp.temp.toInt()}??C",
                    fontSize = 36.sp
                )
            }
        }
        is WeatherConditionState.Nothing -> {}
    }
}

@Composable
private fun LatestNewsSection(
    modifier: Modifier,
    spaceNewsState: SpaceNewsState,
    navController: NavController,
    viewModel: SpaceNewsViewModel
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            modifier = modifier.padding(bottom = 16.dp, start = 16.dp),
            text = constants.latest_news_title,
            style = MaterialTheme.typography.h2
        )
        when (spaceNewsState) {
            is SpaceNewsState.Loading -> {
                LoadingSpinner(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(125.dp)
                )
            }
            is SpaceNewsState.Success -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(spaceNewsState.data!!.take(10)) {
                        LatestNewsCard(
                            onClick = {
                                val encodedUrl = URLEncoder.encode(
                                    it.newsSource,
                                    StandardCharsets.UTF_8.toString()
                                )
                                navController.navigate("${NavName.news_detail_screen}/${encodedUrl}") {
                                    popUpTo(NavScreen.NewsScreen.route)
                                }
                            },
                            newsImageUrl = it.image.image,
                            newsTitle = it.title,
                            newsAuthor = it.author,
                            context = LocalContext.current
                        )
                    }
                }
            }
            is SpaceNewsState.Error -> {
                ErrorCard(
                    errorDescription = spaceNewsState.errorMessage.toString(),
                    paddingValues = PaddingValues(vertical = 16.dp),
                    isButtonAvailable = true,
                    buttonText = "Try Again",
                    onClick = { viewModel.getSpaceNewsFromNetwork() }
                )
            }
        }
    }
}

@Composable
private fun NewsSection(
    modifier: Modifier,
    spaceNewsState: SpaceNewsState,
    navController: NavController,
    viewModel: SpaceNewsViewModel
) {
    Column(modifier = modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp)) {
        Text(text = constants.news_title, style = MaterialTheme.typography.h2)
        when (spaceNewsState) {
            is SpaceNewsState.Loading -> {
                LoadingSpinner(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .padding(top = 48.dp)
                )
            }
            is SpaceNewsState.Success -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(spaceNewsState.data!!.asReversed()) {
                        NewsCard(
                            onClick = {
                                val encodedUrl = URLEncoder.encode(
                                    it.newsSource,
                                    StandardCharsets.UTF_8.toString()
                                )
                                navController.navigate("${NavName.news_detail_screen}/${encodedUrl}") {
                                    popUpTo(NavScreen.NewsScreen.route)
                                }
                            },
                            newsImageUrl = it.image.image,
                            newsTime = DateFormatter.dateFormatter(date = it.date),
                            newsTitle = it.title,
                            newsAuthor = it.author,
                            context = LocalContext.current
                        )
                    }
                }
            }
            is SpaceNewsState.Error -> {
                ErrorCard(
                    errorDescription = spaceNewsState.errorMessage.toString(),
                    paddingValues = PaddingValues(vertical = 16.dp),
                    isButtonAvailable = true,
                    buttonText = "Try Again",
                    onClick = { viewModel.getSpaceNewsFromNetwork() }
                )
            }
        }
    }
}

@Composable
private fun OnBackPressed(activity: Activity) {
    BackHandler {
        activity.finish()
    }
}