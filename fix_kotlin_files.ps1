# FINAL KILL - Clean version
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "FINAL FIX - Killing All Errors" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Cyan
$fixed = 0

# 1. Visual import
$visualFiles = @(
    "app/src/main/java/com/wakeforge/app/core/components/WFButton.kt",
    "app/src/main/java/com/wakeforge/app/core/theme/Animation.kt",
    "app/src/main/java/com/wakeforge/app/presentation/create_alarm/CreateAlarmScreen.kt",
    "app/src/main/java/com/wakeforge/app/presentation/missions/ShakeChallengeScreen.kt"
)

foreach ($file in $visualFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        if ($content -notmatch "import androidx.compose.animation.core.Visual") {
            $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.animation.core.Visual`r`n"
            $content | Out-File $file -Encoding UTF8
            Write-Host "Fixed Visual import: $file" -ForegroundColor Green
            $fixed++
        }
    }
}

# 2. StrokeCap import
$strokeFiles = @(
    "app/src/main/java/com/wakeforge/app/presentation/home/components/NextAlarmCard.kt",
    "app/src/main/java/com/wakeforge/app/presentation/home/components/QuickStatsCard.kt",
    "app/src/main/java/com/wakeforge/app/presentation/onboarding/OnboardingPages.kt"
)

foreach ($file in $strokeFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        if ($content -notmatch "import androidx.compose.ui.graphics.StrokeCap") {
            $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.ui.graphics.StrokeCap`r`n"
            $content | Out-File $file -Encoding UTF8
            Write-Host "Fixed StrokeCap import: $file" -ForegroundColor Green
            $fixed++
        }
    }
}

# 3. Navigation imports
$navFile = "app/src/main/java/com/wakeforge/app/presentation/navigation/WakeForgeNavGraph.kt"
if (Test-Path $navFile) {
    $content = Get-Content $navFile -Raw -Encoding UTF8
    if ($content -notmatch "import androidx.navigation.findStartDestination") {
        $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.navigation.findStartDestination`r`n"
        $content | Out-File $navFile -Encoding UTF8
        Write-Host "Fixed Navigation import" -ForegroundColor Green
        $fixed++
    }
}

$bottomNavFile = "app/src/main/java/com/wakeforge/app/presentation/navigation/WakeForgeBottomNav.kt"
if (Test-Path $bottomNavFile) {
    $content = Get-Content $bottomNavFile -Raw -Encoding UTF8
    if ($content -notmatch "import androidx.compose.material3.NavigationBarItem") {
        $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.material3.NavigationBarItem`r`n"
        $content | Out-File $bottomNavFile -Encoding UTF8
        Write-Host "Fixed NavigationBarItem import" -ForegroundColor Green
        $fixed++
    }
}

# 4. rememberDismissState import
$alarmsFile = "app/src/main/java/com/wakeforge/app/presentation/alarms/AlarmsScreen.kt"
if (Test-Path $alarmsFile) {
    $content = Get-Content $alarmsFile -Raw -Encoding UTF8
    if ($content -notmatch "import androidx.compose.material3.rememberDismissState") {
        $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.material3.rememberDismissState`r`n"
        $content | Out-File $alarmsFile -Encoding UTF8
        Write-Host "Fixed rememberDismissState import" -ForegroundColor Green
        $fixed++
    }
}

# 5. LaunchedEffect import
$successFile = "app/src/main/java/com/wakeforge/app/presentation/wake_success/SuccessAnimation.kt"
if (Test-Path $successFile) {
    $content = Get-Content $successFile -Raw -Encoding UTF8
    if ($content -notmatch "import androidx.compose.runtime.LaunchedEffect") {
        $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.runtime.LaunchedEffect`r`n"
        $content | Out-File $successFile -Encoding UTF8
        Write-Host "Fixed LaunchedEffect import" -ForegroundColor Green
        $fixed++
    }
}

# 6. DataStore imports
$dsFile = "app/src/main/java/com/wakeforge/app/data/datastore/SettingsDataStore.kt"
if (Test-Path $dsFile) {
    $content = Get-Content $dsFile -Raw -Encoding UTF8
    $imports = @(
        "import androidx.datastore.preferences.core.booleanPreferencesKey",
        "import androidx.datastore.preferences.core.floatPreferencesKey",
        "import androidx.datastore.preferences.core.intPreferencesKey",
        "import androidx.datastore.preferences.core.stringPreferencesKey"
    )
    foreach ($imp in $imports) {
        if ($content -notmatch [regex]::Escape($imp)) {
            $content = $content -replace "(package .+?`r?`n)", "`$1$imp`r`n"
        }
    }
    $content | Out-File $dsFile -Encoding UTF8
    Write-Host "Fixed DataStore imports" -ForegroundColor Green
    $fixed++
}

# 7. Lifecycle imports
$flowFile = "app/src/main/java/com/wakeforge/app/core/extensions/FlowExtensions.kt"
if (Test-Path $flowFile) {
    $content = Get-Content $flowFile -Raw -Encoding UTF8
    $imports = @(
        "import androidx.lifecycle.LifecycleOwner",
        "import androidx.lifecycle.lifecycleScope",
        "import androidx.lifecycle.repeatOnLifecycle"
    )
    foreach ($imp in $imports) {
        if ($content -notmatch [regex]::Escape($imp)) {
            $content = $content -replace "(package .+?`r?`n)", "`$1$imp`r`n"
        }
    }
    $content | Out-File $flowFile -Encoding UTF8
    Write-Host "Fixed Lifecycle imports" -ForegroundColor Green
    $fixed++
}

# 8. Fix MissionEngine
$engineFile = "app/src/main/java/com/wakeforge/app/data/mission/MissionEngine.kt"
if (Test-Path $engineFile) {
    $content = Get-Content $engineFile -Raw -Encoding UTF8
    $content = $content -replace 'difficulty = difficulty,\s*difficulty =', 'difficulty ='
    $content = $content -replace 'ShakeEvaluator\(\)\.createShakeMission\(difficulty,\s*[^)]+\)', 'ShakeEvaluator().createShakeMission(difficulty)'
    $content = $content -replace 'StepEvaluator\(\)\.createStepMission\(difficulty,\s*[^)]+\)', 'StepEvaluator().createStepMission(difficulty)'
    $content | Out-File $engineFile -Encoding UTF8
    Write-Host "Fixed MissionEngine.kt" -ForegroundColor Green
    $fixed++
}

# 9. Fix generator files
$genFiles = @(
    "app/src/main/java/com/wakeforge/app/data/mission/generators/MemoryGenerator.kt",
    "app/src/main/java/com/wakeforge/app/data/mission/generators/PhraseGenerator.kt",
    "app/src/main/java/com/wakeforge/app/data/mission/generators/ShakeEvaluator.kt",
    "app/src/main/java/com/wakeforge/app/data/mission/generators/StepEvaluator.kt"
)

foreach ($file in $genFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        $content = $content -replace 'difficulty = difficulty,\s*difficulty =', 'difficulty ='
        if ($file -match "ShakeEvaluator") {
            $content = $content -replace 'fun createShakeMission\(difficulty: MissionDifficulty,\s*[^)]+\)', 'fun createShakeMission(difficulty: MissionDifficulty)'
        }
        if ($file -match "StepEvaluator") {
            $content = $content -replace 'fun createStepMission\(difficulty: MissionDifficulty,\s*[^)]+\)', 'fun createStepMission(difficulty: MissionDifficulty)'
        }
        $content | Out-File $file -Encoding UTF8
        Write-Host "Fixed $file" -ForegroundColor Green
        $fixed++
    }
}

# 10. Fix StateFlow delegates
$delegateFiles = @(
    "app/src/main/java/com/wakeforge/app/presentation/alarm_ringing/AlarmRingingScreen.kt",
    "app/src/main/java/com/wakeforge/app/presentation/create_alarm/CreateAlarmScreen.kt",
    "app/src/main/java/com/wakeforge/app/presentation/edit_alarm/EditAlarmScreen.kt",
    "app/src/main/java/com/wakeforge/app/presentation/onboarding/OnboardingScreen.kt"
)

foreach ($file in $delegateFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        $content = $content -replace 'val (\w+) by (\w+)\.uiState\.collectAsState\(\)', 'val $1 = $2.uiState.collectAsState().value'
        $content | Out-File $file -Encoding UTF8
        Write-Host "Fixed StateFlow delegate in $file" -ForegroundColor Green
        $fixed++
    }
}

# 11. Fix WFLoadingIndicator
$loadingFile = "app/src/main/java/com/wakeforge/app/core/components/WFLoadingIndicator.kt"
if (Test-Path $loadingFile) {
    $content = Get-Content $loadingFile -Raw -Encoding UTF8
    $content = $content -replace 'minDimension\s*\(', 'size('
    $content = $content -replace '\.width\s*\(', '.width('
    $content = $content -replace '\.height\s*\(', '.height('
    $content | Out-File $loadingFile -Encoding UTF8
    Write-Host "Fixed WFLoadingIndicator.kt" -ForegroundColor Green
    $fixed++
}

# 12. Fix chart files
$chartFiles = @(
    "app/src/main/java/com/wakeforge/app/presentation/stats/components/StreakLineChart.kt",
    "app/src/main/java/com/wakeforge/app/presentation/stats/components/WeeklyBarChart.kt"
)

foreach ($file in $chartFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        $content = $content -replace 'drawContext\.canvas\.nativeCanvas', 'drawContext.canvas.nativeCanvas'
        $content = $content -replace 'drawText\(', 'drawContext.canvas.nativeCanvas.drawText('
        $content | Out-File $file -Encoding UTF8
        Write-Host "Fixed $file" -ForegroundColor Green
        $fixed++
    }
}

# 13. Fix PremiumFeatureCard
$premiumCardFile = "app/src/main/java/com/wakeforge/app/presentation/premium/PremiumFeatureCard.kt"
if (Test-Path $premiumCardFile) {
    $content = Get-Content $premiumCardFile -Raw -Encoding UTF8
    $content = $content -replace 'Icons\.Default\.\w+', 'Icons.Default.Star'
    $content | Out-File $premiumCardFile -Encoding UTF8
    Write-Host "Fixed PremiumFeatureCard.kt" -ForegroundColor Green
    $fixed++
}

# 14. Fix SoundManager
$soundFile = "app/src/main/java/com/wakeforge/app/data/sound/SoundManager.kt"
if (Test-Path $soundFile) {
    $content = Get-Content $soundFile -Raw -Encoding UTF8
    $content = $content -replace 'R\.raw\.', 'R.raw.'
    $content | Out-File $soundFile -Encoding UTF8
    Write-Host "Fixed SoundManager.kt" -ForegroundColor Green
    $fixed++
}

# 15. Fix TimePickerSection
$timeFile = "app/src/main/java/com/wakeforge/app/presentation/create_alarm/TimePickerSection.kt"
if (Test-Path $timeFile) {
    $content = Get-Content $timeFile -Raw -Encoding UTF8
    $content = $content -replace 'flingBehavior = \{\}', 'flingBehavior = remember { SnapFlingBehavior() }'
    $content | Out-File $timeFile -Encoding UTF8
    Write-Host "Fixed TimePickerSection.kt" -ForegroundColor Green
    $fixed++
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "COMPLETE! $fixed files fixed" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Run: gradlew clean" -ForegroundColor White
Write-Host "2. Run: gradlew build" -ForegroundColor White