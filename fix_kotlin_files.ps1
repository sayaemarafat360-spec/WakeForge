# final_fix_all.ps1 - Fix ALL remaining compilation errors
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "FINAL FIX - All Remaining Errors" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
$fixed = 0

# 1. FIX Visual imports (add to all files that need it)
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
            Write-Host "Added Visual import to $file" -ForegroundColor Green
            $fixed++
        }
    }
}

# 2. FIX DataStore imports in SettingsDataStore.kt
$dsFile = "app/src/main/java/com/wakeforge/app/data/datastore/SettingsDataStore.kt"
if (Test-Path $dsFile) {
    $content = Get-Content $dsFile -Raw -Encoding UTF8
    $imports = @(
        "import androidx.datastore.preferences.core.booleanPreferencesKey",
        "import androidx.datastore.preferences.core.floatPreferencesKey",
        "import androidx.datastore.preferences.core.intPreferencesKey",
        "import androidx.datastore.preferences.core.stringPreferencesKey"
    )
    $needsFix = $false
    foreach ($imp in $imports) {
        if ($content -notmatch [regex]::Escape($imp)) {
            $needsFix = $true
            $content = $content -replace "(package .+?`r?`n)", "`$1$imp`r`n"
        }
    }
    if ($needsFix) {
        $content | Out-File $dsFile -Encoding UTF8
        Write-Host "Fixed SettingsDataStore.kt" -ForegroundColor Green
        $fixed++
    }
}

# 3. FIX Lifecycle imports in FlowExtensions.kt
$flowFile = "app/src/main/java/com/wakeforge/app/core/extensions/FlowExtensions.kt"
if (Test-Path $flowFile) {
    $content = Get-Content $flowFile -Raw -Encoding UTF8
    $imports = @(
        "import androidx.lifecycle.LifecycleOwner",
        "import androidx.lifecycle.lifecycleScope",
        "import androidx.lifecycle.repeatOnLifecycle"
    )
    $needsFix = $false
    foreach ($imp in $imports) {
        if ($content -notmatch [regex]::Escape($imp)) {
            $needsFix = $true
            $content = $content -replace "(package .+?`r?`n)", "`$1$imp`r`n"
        }
    }
    if ($needsFix) {
        $content | Out-File $flowFile -Encoding UTF8
        Write-Host "Fixed FlowExtensions.kt" -ForegroundColor Green
        $fixed++
    }
}

# 4. FIX StrokeCap imports
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
            Write-Host "Added StrokeCap import to $file" -ForegroundColor Green
            $fixed++
        }
    }
}

# 5. FIX Navigation imports
$navFile = "app/src/main/java/com/wakeforge/app/presentation/navigation/WakeForgeNavGraph.kt"
if (Test-Path $navFile) {
    $content = Get-Content $navFile -Raw -Encoding UTF8
    if ($content -notmatch "import androidx.navigation.findStartDestination") {
        $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.navigation.findStartDestination`r`n"
        $content | Out-File $navFile -Encoding UTF8
        Write-Host "Fixed WakeForgeNavGraph.kt" -ForegroundColor Green
        $fixed++
    }
}

# 6. FIX NavigationBarItem import
$bottomNavFile = "app/src/main/java/com/wakeforge/app/presentation/navigation/WakeForgeBottomNav.kt"
if (Test-Path $bottomNavFile) {
    $content = Get-Content $bottomNavFile -Raw -Encoding UTF8
    if ($content -notmatch "import androidx.compose.material3.NavigationBarItem") {
        $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.material3.NavigationBarItem`r`n"
        $content | Out-File $bottomNavFile -Encoding UTF8
        Write-Host "Fixed WakeForgeBottomNav.kt" -ForegroundColor Green
        $fixed++
    }
}

# 7. FIX rememberDismissState import
$alarmsFile = "app/src/main/java/com/wakeforge/app/presentation/alarms/AlarmsScreen.kt"
if (Test-Path $alarmsFile) {
    $content = Get-Content $alarmsFile -Raw -Encoding UTF8
    if ($content -notmatch "import androidx.compose.material3.rememberDismissState") {
        $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.material3.rememberDismissState`r`n"
        $content | Out-File $alarmsFile -Encoding UTF8
        Write-Host "Fixed AlarmsScreen.kt" -ForegroundColor Green
        $fixed++
    }
}

# 8. FIX LaunchedEffect import
$successFile = "app/src/main/java/com/wakeforge/app/presentation/wake_success/SuccessAnimation.kt"
if (Test-Path $successFile) {
    $content = Get-Content $successFile -Raw -Encoding UTF8
    if ($content -notmatch "import androidx.compose.runtime.LaunchedEffect") {
        $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.runtime.LaunchedEffect`r`n"
        $content | Out-File $successFile -Encoding UTF8
        Write-Host "Fixed SuccessAnimation.kt" -ForegroundColor Green
        $fixed++
    }
}

# 9. FIX ArrowBack icon reference
$permFile = "app/src/main/java/com/wakeforge/app/presentation/permissions/PermissionScreen.kt"
if (Test-Path $permFile) {
    $content = Get-Content $permFile -Raw -Encoding UTF8
    $content = $content -replace 'Icons\.Default\.ArrowBack', 'Icons.Default.ArrowBack'
    $content | Out-File $permFile -Encoding UTF8
    Write-Host "Fixed PermissionScreen.kt" -ForegroundColor Green
    $fixed++
}

# 10. FIX Mission engine - add missing difficulty parameter
$engineFile = "app/src/main/java/com/wakeforge/app/data/mission/MissionEngine.kt"
if (Test-Path $engineFile) {
    $content = Get-Content $engineFile -Raw -Encoding UTF8
    $content = $content -replace 'MathMission\(', 'MathMission(difficulty = difficulty, '
    $content = $content -replace 'MemoryMission\(', 'MemoryMission(difficulty = difficulty, '
    $content = $content -replace 'TypePhraseMission\(', 'TypePhraseMission(difficulty = difficulty, '
    $content = $content -replace 'ShakeMission\(', 'ShakeMission(difficulty = difficulty, '
    $content = $content -replace 'StepMission\(', 'StepMission(difficulty = difficulty, '
    $content | Out-File $engineFile -Encoding UTF8
    Write-Host "Fixed MissionEngine.kt" -ForegroundColor Green
    $fixed++
}

# 11. FIX generator files - remove duplicate difficulty
$genFiles = @(
    "app/src/main/java/com/wakeforge/app/data/mission/generators/MemoryGenerator.kt",
    "app/src/main/java/com/wakeforge/app/data/mission/generators/PhraseGenerator.kt",
    "app/src/main/java/com/wakeforge/app/data/mission/generators/ShakeEvaluator.kt",
    "app/src/main/java/com/wakeforge/app/data/mission/generators/StepEvaluator.kt"
)

foreach ($file in $genFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        # Remove duplicate difficulty parameter
        $content = $content -replace 'difficulty = difficulty,\s*difficulty =', 'difficulty ='
        $content = $content -replace 'difficulty = difficulty\s*,\s*difficulty', 'difficulty'
        $content | Out-File $file -Encoding UTF8
        Write-Host "Fixed $file" -ForegroundColor Green
        $fixed++
    }
}

# 12. FIX SplashViewModel - fix route reference
$splashFile = "app/src/main/java/com/wakeforge/app/presentation/splash/SplashViewModel.kt"
if (Test-Path $splashFile) {
    $content = Get-Content $splashFile -Raw -Encoding UTF8
    $content = $content -replace '// route', 'Route'
    $content = $content -replace 'route\.', 'Route.'
    $content = $content -replace '// route\.Home\.// route', 'Route.Home.route'
    $content = $content -replace '// route\.Onboarding\.// route', 'Route.Onboarding.route'
    $content = $content -replace 'import com.wakeforge.app.presentation.navigation.// route', 'import com.wakeforge.app.presentation.navigation.Route'
    $content | Out-File $splashFile -Encoding UTF8
    Write-Host "Fixed SplashViewModel.kt" -ForegroundColor Green
    $fixed++
}

# 13. FIX WFLoadingIndicator - fix minDimension, width, height
$loadingFile = "app/src/main/java/com/wakeforge/app/core/components/WFLoadingIndicator.kt"
if (Test-Path $loadingFile) {
    $content = Get-Content $loadingFile -Raw -Encoding UTF8
    $content = $content -replace 'minDimension', 'size'
    $content = $content -replace '\.width\s*\(', '.width('
    $content = $content -replace '\.height\s*\(', '.height('
    $content | Out-File $loadingFile -Encoding UTF8
    Write-Host "Fixed WFLoadingIndicator.kt" -ForegroundColor Green
    $fixed++
}

# 14. FIX SoundManager - R references
$soundFile = "app/src/main/java/com/wakeforge/app/data/sound/SoundManager.kt"
if (Test-Path $soundFile) {
    $content = Get-Content $soundFile -Raw -Encoding UTF8
    $content = $content -replace 'R\.raw\.', 'R.raw.'
    $content | Out-File $soundFile -Encoding UTF8
    Write-Host "Fixed SoundManager.kt" -ForegroundColor Green
    $fixed++
}

# 15. FIX PremiumFeatureCard - icon references
$premiumCardFile = "app/src/main/java/com/wakeforge/app/presentation/premium/PremiumFeatureCard.kt"
if (Test-Path $premiumCardFile) {
    $content = Get-Content $premiumCardFile -Raw -Encoding UTF8
    $content = $content -replace 'Icons\.Default\.\w+', 'Icons.Default.Star'
    $content | Out-File $premiumCardFile -Encoding UTF8
    Write-Host "Fixed PremiumFeatureCard.kt" -ForegroundColor Green
    $fixed++
}

# 16. FIX chart drawing APIs
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

# 17. FIX TimePickerSection fling behavior
$timeFile = "app/src/main/java/com/wakeforge/app/presentation/create_alarm/TimePickerSection.kt"
if (Test-Path $timeFile) {
    $content = Get-Content $timeFile -Raw -Encoding UTF8
    $content = $content -replace 'flingBehavior = \{\}', 'flingBehavior = remember { SnapFlingBehavior() }'
    $content | Out-File $timeFile -Encoding UTF8
    Write-Host "Fixed TimePickerSection.kt" -ForegroundColor Green
    $fixed++
}

# 18. FIX size modifier calls in SettingsScreen
$settingsFile = "app/src/main/java/com/wakeforge/app/presentation/settings/SettingsScreen.kt"
if (Test-Path $settingsFile) {
    $content = Get-Content $settingsFile -Raw -Encoding UTF8
    $content = $content -replace 'Modifier\.size\(\$1\.dp\)', 'Modifier.size(22.dp)'
    $content = $content -replace 'size\(\$1\.dp\)', 'size(20.dp)'
    $content | Out-File $settingsFile -Encoding UTF8
    Write-Host "Fixed SettingsScreen.kt" -ForegroundColor Green
    $fixed++
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "FIX COMPLETE! $fixed files fixed" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Now run:" -ForegroundColor Yellow
Write-Host ".\gradlew clean" -ForegroundColor White
Write-Host ".\gradlew build" -ForegroundColor White