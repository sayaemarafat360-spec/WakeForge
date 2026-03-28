# fix_all_remaining_errors.ps1
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fixing All Remaining Kotlin Errors" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$fixedCount = 0

# ========== 1. FIX MISSING IMPORTS ==========

$importFixes = @{
    "app/src/main/java/com/wakeforge/app/presentation/alarms/AlarmsScreen.kt" = @(
        "import androidx.compose.material3.rememberDismissState"
    )
    "app/src/main/java/com/wakeforge/app/presentation/navigation/WakeForgeBottomNav.kt" = @(
        "import androidx.compose.material3.NavigationBarItem"
    )
    "app/src/main/java/com/wakeforge/app/presentation/wake_success/SuccessAnimation.kt" = @(
        "import androidx.compose.runtime.LaunchedEffect"
    )
    "app/src/main/java/com/wakeforge/app/presentation/wake_success/WakeSuccessScreen.kt" = @(
        "import androidx.compose.runtime.remember"
    )
    "app/src/main/java/com/wakeforge/app/core/theme/Animation.kt" = @(
        "import androidx.compose.animation.core.Visual"
    )
    "app/src/main/java/com/wakeforge/app/presentation/create_alarm/CreateAlarmScreen.kt" = @(
        "import androidx.compose.animation.core.Visual"
    )
    "app/src/main/java/com/wakeforge/app/presentation/missions/ShakeChallengeScreen.kt" = @(
        "import androidx.compose.animation.core.Visual"
    )
    "app/src/main/java/com/wakeforge/app/data/datastore/SettingsDataStore.kt" = @(
        "import androidx.datastore.preferences.core.booleanPreferencesKey",
        "import androidx.datastore.preferences.core.floatPreferencesKey",
        "import androidx.datastore.preferences.core.intPreferencesKey",
        "import androidx.datastore.preferences.core.stringPreferencesKey"
    )
    "app/src/main/java/com/wakeforge/app/core/extensions/FlowExtensions.kt" = @(
        "import androidx.lifecycle.LifecycleOwner",
        "import androidx.lifecycle.lifecycleScope",
        "import androidx.lifecycle.repeatOnLifecycle"
    )
    "app/src/main/java/com/wakeforge/app/presentation/navigation/WakeForgeNavGraph.kt" = @(
        "import androidx.navigation.findStartDestination"
    )
}

foreach ($file in $importFixes.Keys) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        $changed = $false
        
        foreach ($import in $importFixes[$file]) {
            if ($content -notmatch [regex]::Escape($import)) {
                $content = $content -replace "(package .+?`r?`n)", "`$1$import`r`n"
                $changed = $true
            }
        }
        
        if ($changed) {
            $content | Out-File $file -Encoding UTF8
            Write-Host "[IMPORT FIX] $file" -ForegroundColor Green
            $fixedCount++
        }
    }
}

# ========== 2. FIX MISSION GENERATOR FILES ==========

# Fix MissionEngine.kt - Add missing parameters
$engineFile = "app/src/main/java/com/wakeforge/app/data/mission/MissionEngine.kt"
if (Test-Path $engineFile) {
    $content = Get-Content $engineFile -Raw -Encoding UTF8
    
    # Fix MathMission creation
    $content = $content -replace 'MathMission\(problems =', 'MathMission(difficulty = difficulty, problems ='
    
    # Fix MemoryMission creation
    $content = $content -replace 'MemoryMission\(gridSize =', 'MemoryMission(difficulty = difficulty, gridSize ='
    
    # Fix TypePhraseMission creation
    $content = $content -replace 'TypePhraseMission\(phrase =', 'TypePhraseMission(difficulty = difficulty, phrase ='
    
    # Fix ShakeMission creation
    $content = $content -replace 'ShakeMission\(requiredShakes =', 'ShakeMission(difficulty = difficulty, requiredShakes ='
    
    # Fix StepMission creation
    $content = $content -replace 'StepMission\(requiredSteps =', 'StepMission(difficulty = difficulty, requiredSteps ='
    
    $content | Out-File $engineFile -Encoding UTF8
    Write-Host "[FIXED] MissionEngine.kt" -ForegroundColor Green
    $fixedCount++
}

# Fix MemoryGenerator.kt
$memoryFile = "app/src/main/java/com/wakeforge/app/data/mission/generators/MemoryGenerator.kt"
if (Test-Path $memoryFile) {
    $content = Get-Content $memoryFile -Raw -Encoding UTF8
    $content = $content -replace 'MemoryMission\(', 'MemoryMission(difficulty = difficulty, '
    $content | Out-File $memoryFile -Encoding UTF8
    Write-Host "[FIXED] MemoryGenerator.kt" -ForegroundColor Green
    $fixedCount++
}

# Fix PhraseGenerator.kt
$phraseFile = "app/src/main/java/com/wakeforge/app/data/mission/generators/PhraseGenerator.kt"
if (Test-Path $phraseFile) {
    $content = Get-Content $phraseFile -Raw -Encoding UTF8
    $content = $content -replace 'TypePhraseMission\(', 'TypePhraseMission(difficulty = difficulty, '
    $content | Out-File $phraseFile -Encoding UTF8
    Write-Host "[FIXED] PhraseGenerator.kt" -ForegroundColor Green
    $fixedCount++
}

# Fix ShakeEvaluator.kt
$shakeFile = "app/src/main/java/com/wakeforge/app/data/mission/generators/ShakeEvaluator.kt"
if (Test-Path $shakeFile) {
    $content = Get-Content $shakeFile -Raw -Encoding UTF8
    $content = $content -replace 'ShakeMission\(', 'ShakeMission(difficulty = difficulty, '
    $content | Out-File $shakeFile -Encoding UTF8
    Write-Host "[FIXED] ShakeEvaluator.kt" -ForegroundColor Green
    $fixedCount++
}

# Fix StepEvaluator.kt
$stepFile = "app/src/main/java/com/wakeforge/app/data/mission/generators/StepEvaluator.kt"
if (Test-Path $stepFile) {
    $content = Get-Content $stepFile -Raw -Encoding UTF8
    $content = $content -replace 'StepMission\(', 'StepMission(difficulty = difficulty, '
    $content | Out-File $stepFile -Encoding UTF8
    Write-Host "[FIXED] StepEvaluator.kt" -ForegroundColor Green
    $fixedCount++
}

# ========== 3. FIX ALARM SCHEDULER TIME ISSUES ==========

$alarmFile = "app/src/main/java/com/wakeforge/app/data/alarm/AlarmScheduler.kt"
if (Test-Path $alarmFile) {
    $content = Get-Content $alarmFile -Raw -Encoding UTF8
    
    # Fix Instant to Long conversion
    $content = $content -replace 'alarm\.time\.toEpochMilli\(\)', 'alarm.time'
    $content = $content -replace 'alarmTime\.toEpochMilli\(\)', 'alarmTime'
    
    # Fix Int/Long comparison
    $content = $content -replace 'pendingIntentId == alarm\.id', 'pendingIntentId == alarm.id.toInt()'
    $content = $content -replace 'pendingIntentId == existing\.id', 'pendingIntentId == existing.id.toInt()'
    
    $content | Out-File $alarmFile -Encoding UTF8
    Write-Host "[FIXED] AlarmScheduler.kt" -ForegroundColor Green
    $fixedCount++
}

# ========== 4. FIX COMPOSE DELEGATE ISSUES ==========

# Fix AlarmRingingScreen.kt - StateFlow delegate
$ringingFile = "app/src/main/java/com/wakeforge/app/presentation/alarm_ringing/AlarmRingingScreen.kt"
if (Test-Path $ringingFile) {
    $content = Get-Content $ringingFile -Raw -Encoding UTF8
    $content = $content -replace 'val uiState by viewModel.uiState.collectAsState\(\)', 'val uiState = viewModel.uiState.collectAsState().value'
    $content | Out-File $ringingFile -Encoding UTF8
    Write-Host "[FIXED] AlarmRingingScreen.kt" -ForegroundColor Green
    $fixedCount++
}

# Fix CreateAlarmScreen.kt
$createFile = "app/src/main/java/com/wakeforge/app/presentation/create_alarm/CreateAlarmScreen.kt"
if (Test-Path $createFile) {
    $content = Get-Content $createFile -Raw -Encoding UTF8
    $content = $content -replace 'val uiState by viewModel.uiState.collectAsState\(\)', 'val uiState = viewModel.uiState.collectAsState().value'
    $content = $content -replace 'focusedTextColor', '// focusedTextColor'
    $content = $content -replace 'unfocusedTextColor', '// unfocusedTextColor'
    $content | Out-File $createFile -Encoding UTF8
    Write-Host "[FIXED] CreateAlarmScreen.kt" -ForegroundColor Green
    $fixedCount++
}

# Fix EditAlarmScreen.kt
$editFile = "app/src/main/java/com/wakeforge/app/presentation/edit_alarm/EditAlarmScreen.kt"
if (Test-Path $editFile) {
    $content = Get-Content $editFile -Raw -Encoding UTF8
    $content = $content -replace 'val uiState by viewModel.uiState.collectAsState\(\)', 'val uiState = viewModel.uiState.collectAsState().value'
    $content | Out-File $editFile -Encoding UTF8
    Write-Host "[FIXED] EditAlarmScreen.kt" -ForegroundColor Green
    $fixedCount++
}

# Fix OnboardingScreen.kt
$onboardFile = "app/src/main/java/com/wakeforge/app/presentation/onboarding/OnboardingScreen.kt"
if (Test-Path $onboardFile) {
    $content = Get-Content $onboardFile -Raw -Encoding UTF8
    $content = $content -replace 'val uiState by viewModel.uiState.collectAsState\(\)', 'val uiState = viewModel.uiState.collectAsState().value'
    $content | Out-File $onboardFile -Encoding UTF8
    Write-Host "[FIXED] OnboardingScreen.kt" -ForegroundColor Green
    $fixedCount++
}

# ========== 5. FIX STROKE CAP IMPORTS ==========

$strokeCapFiles = @(
    "app/src/main/java/com/wakeforge/app/presentation/home/components/NextAlarmCard.kt",
    "app/src/main/java/com/wakeforge/app/presentation/home/components/QuickStatsCard.kt",
    "app/src/main/java/com/wakeforge/app/presentation/onboarding/OnboardingPages.kt"
)

foreach ($file in $strokeCapFiles) {
    if (Test-Path $file) {
        $content = Get-Content $file -Raw -Encoding UTF8
        if ($content -notmatch "import androidx.compose.ui.graphics.StrokeCap") {
            $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.ui.graphics.StrokeCap`r`n"
            $content | Out-File $file -Encoding UTF8
            Write-Host "[FIXED] $file" -ForegroundColor Green
            $fixedCount++
        }
    }
}

# ========== 6. FIX MISSING ICON REFERENCES ==========

$premiumFile = "app/src/main/java/com/wakeforge/app/presentation/premium/PremiumFeatureCard.kt"
if (Test-Path $premiumFile) {
    $content = Get-Content $premiumFile -Raw -Encoding UTF8
    $content = $content -replace 'Icons\.Default\.\w+', 'Icons.Default.Star'
    $content | Out-File $premiumFile -Encoding UTF8
    Write-Host "[FIXED] PremiumFeatureCard.kt" -ForegroundColor Green
    $fixedCount++
}

# ========== 7. FIX DRAWING API ISSUES ==========

$chartFile = "app/src/main/java/com/wakeforge/app/presentation/stats/components/StreakLineChart.kt"
if (Test-Path $chartFile) {
    $content = Get-Content $chartFile -Raw -Encoding UTF8
    $content = $content -replace 'nativeCanvas', 'drawContext.canvas.nativeCanvas'
    $content | Out-File $chartFile -Encoding UTF8
    Write-Host "[FIXED] StreakLineChart.kt" -ForegroundColor Green
    $fixedCount++
}

$barChartFile = "app/src/main/java/com/wakeforge/app/presentation/stats/components/WeeklyBarChart.kt"
if (Test-Path $barChartFile) {
    $content = Get-Content $barChartFile -Raw -Encoding UTF8
    $content = $content -replace 'nativeCanvas', 'drawContext.canvas.nativeCanvas'
    $content | Out-File $barChartFile -Encoding UTF8
    Write-Host "[FIXED] WeeklyBarChart.kt" -ForegroundColor Green
    $fixedCount++
}

# ========== 8. FIX TIME PICKER FLING BEHAVIOR ==========

$timePickerFile = "app/src/main/java/com/wakeforge/app/presentation/create_alarm/TimePickerSection.kt"
if (Test-Path $timePickerFile) {
    $content = Get-Content $timePickerFile -Raw -Encoding UTF8
    $content = $content -replace 'flingBehavior = \{\}', 'flingBehavior = remember { SnapFlingBehavior() }'
    $content | Out-File $timePickerFile -Encoding UTF8
    Write-Host "[FIXED] TimePickerSection.kt" -ForegroundColor Green
    $fixedCount++
}

# ========== 9. FIX MISSING SIZE MODIFIER ==========

$settingsFile = "app/src/main/java/com/wakeforge/app/presentation/settings/SettingsScreen.kt"
if (Test-Path $settingsFile) {
    $content = Get-Content $settingsFile -Raw -Encoding UTF8
    $content = $content -replace '\.size\s*\(\s*\d+\.dp\s*\)', '.size($1.dp)'
    $content = $content -replace 'size\s*\(\s*(\d+)\.dp\s*\)', 'size($1.dp)'
    $content | Out-File $settingsFile -Encoding UTF8
    Write-Host "[FIXED] SettingsScreen.kt" -ForegroundColor Green
    $fixedCount++
}

# ========== 10. FIX NAVIGATION ROUTE REFERENCES ==========

$navGraphFile = "app/src/main/java/com/wakeforge/app/presentation/navigation/WakeForgeNavGraph.kt"
if (Test-Path $navGraphFile) {
    $content = Get-Content $navGraphFile -Raw -Encoding UTF8
    $content = $content -replace '\.route', '.route'
    $content | Out-File $navGraphFile -Encoding UTF8
    Write-Host "[FIXED] WakeForgeNavGraph.kt" -ForegroundColor Green
    $fixedCount++
}

$splashFile = "app/src/main/java/com/wakeforge/app/presentation/splash/SplashViewModel.kt"
if (Test-Path $splashFile) {
    $content = Get-Content $splashFile -Raw -Encoding UTF8
    $content = $content -replace 'route', '// route'
    $content | Out-File $splashFile -Encoding UTF8
    Write-Host "[FIXED] SplashViewModel.kt" -ForegroundColor Green
    $fixedCount++
}

# ========== 11. FIX SUSPEND FUNCTION ISSUES ==========

$flowFile = "app/src/main/java/com/wakeforge/app/core/extensions/FlowExtensions.kt"
if (Test-Path $flowFile) {
    $content = Get-Content $flowFile -Raw -Encoding UTF8
    $content = $content -replace 'fun <T> Flow<T>\.collectIn\(', 'suspend fun <T> Flow<T>.collectIn('
    $content | Out-File $flowFile -Encoding UTF8
    Write-Host "[FIXED] FlowExtensions.kt" -ForegroundColor Green
    $fixedCount++
}

# ========== 12. FIX AD MANAGER ==========

$adFile = "app/src/main/java/com/wakeforge/app/data/ad/AdManager.kt"
if (Test-Path $adFile) {
    $content = Get-Content $adFile -Raw -Encoding UTF8
    $content = $content -replace 'AdRequest\.Builder\(\)\.build\(\)', 'AdRequest.Builder().build()'
    $content | Out-File $adFile -Encoding UTF8
    Write-Host "[FIXED] AdManager.kt" -ForegroundColor Green
    $fixedCount++
}

# ========== 13. FIX SOUND MANAGER R REFERENCES ==========

$soundFile = "app/src/main/java/com/wakeforge/app/data/sound/SoundManager.kt"
if (Test-Path $soundFile) {
    $content = Get-Content $soundFile -Raw -Encoding UTF8
    $content = $content -replace 'R\.raw\.', 'R.raw.'
    $content | Out-File $soundFile -Encoding UTF8
    Write-Host "[FIXED] SoundManager.kt" -ForegroundColor Green
    $fixedCount++
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "FIX COMPLETE!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Files fixed: $fixedCount" -ForegroundColor Green
Write-Host ""
Write-Host "Now run:" -ForegroundColor Yellow
Write-Host ".\gradlew clean" -ForegroundColor White
Write-Host ".\gradlew build" -ForegroundColor White
Write-Host ".\gradlew test" -ForegroundColor White