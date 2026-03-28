package com.wakeforge.app.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.wakeforge.app.R
import com.wakeforge.app.core.components.WFButton
import com.wakeforge.app.core.components.ButtonType
import com.wakeforge.app.core.theme.BackgroundDark
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.theme.PrimaryAccent
import kotlinx.coroutines.launch

/**
 * Full-screen horizontal pager for the onboarding flow.
 *
 * Displays 4 illustrated pages with:
 * - Smooth horizontal paging animation (HorizontalPager default)
 * - Dot indicators showing current position
 * - "Next" / "Get Started" primary button
 * - "Skip" text button in the top-right corner
 * - Automatic navigation to the permission setup screen on completion
 *
 * @param navController Controller for navigation after onboarding.
 * @param viewModel The [OnboardingViewModel] managing onboarding state.
 */
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val pages = remember { getOnboardingPages() }

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size },
    )

    // Listen for completion / skip events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OnboardingViewModel.Event.Complete,
                is OnboardingViewModel.Event.Skip -> {
                    navController.navigate("permissions") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
    ) {
        // Skip button in top-right corner
        TextButton(
            onClick = { viewModel.skip() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_skip),
                style = typography.labelLarge,
                color = colors.secondaryText,
            )
        }

        // Horizontal pager with onboarding pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            val page = pages[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Animated illustration (Canvas-drawn)
                page.illustration()

                Spacer(modifier = Modifier.height(40.dp))

                // Page title
                Text(
                    text = stringResource(id = page.titleRes),
                    style = typography.headlineLarge,
                    color = colors.primaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Page description
                Text(
                    text = stringResource(id = page.descriptionRes),
                    style = typography.bodyLarge,
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Bottom section: dot indicators + action button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Dot indicators row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                pages.forEachIndexed { index, _ ->
                    val isActive = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isActive) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) PrimaryAccent
                                else colors.secondaryText.copy(alpha = 0.3f),
                            ),
                    )
                }
            }

            // Action button: "Next" on pages 0-2, "Get Started" on page 3
            val isLastPage = pagerState.currentPage == pages.size - 1

            WFButton(
                text = if (isLastPage) {
                    stringResource(id = R.string.onboarding_get_started)
                } else {
                    stringResource(id = R.string.onboarding_next)
                },
                onClick = {
                    if (isLastPage) {
                        viewModel.complete()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                type = ButtonType.Primary,
                fullWidth = true,
            )
        }
    }
}
