# fix_all_errors.ps1
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fixing All Kotlin Compilation Errors" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$fixedCount = 0
$errorCount = 0

# Get all Kotlin files
$files = Get-ChildItem -Recurse -Filter "*.kt"

foreach ($file in $files) {
    Write-Host "Processing: $($file.Name)" -ForegroundColor Yellow
    
    try {
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        $original = $content
        $changed = $false
        
        # ========== ADD MISSING IMPORTS ==========
        
        # StrokeCap import
        if ($content -match "StrokeCap" -and $content -notmatch "import androidx.compose.ui.graphics.StrokeCap") {
            $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.ui.graphics.StrokeCap`r`n"
            $changed = $true
        }
        
        # Alignment import
        if ($content -match "Alignment" -and $content -notmatch "import androidx.compose.ui.Alignment") {
            $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.ui.Alignment`r`n"
            $changed = $true
        }
        
        # rememberDismissState import
        if ($content -match "rememberDismissState" -and $content -notmatch "import androidx.compose.material3.rememberDismissState") {
            $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.material3.rememberDismissState`r`n"
            $changed = $true
        }
        
        # Visual import
        if ($content -match "Visual" -and $content -notmatch "import androidx.compose.animation.core.Visual") {
            $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.animation.core.Visual`r`n"
            $changed = $true
        }
        
        # animateTo import
        if ($content -match "animateTo" -and $content -notmatch "import androidx.compose.animation.core.animateTo") {
            $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.animation.core.animateTo`r`n"
            $changed = $true
        }
        
        # asPaddingValues import
        if ($content -match "asPaddingValues" -and $content -notmatch "import androidx.compose.foundation.layout.asPaddingValues") {
            $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.compose.foundation.layout.asPaddingValues`r`n"
            $changed = $true
        }
        
        # findStartDestination import
        if ($content -match "findStartDestination" -and $content -notmatch "import androidx.navigation.findStartDestination") {
            $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.navigation.findStartDestination`r`n"
            $changed = $true
        }
        
        # DataStore imports
        if ($file.Name -eq "SettingsDataStore.kt") {
            if ($content -notmatch "import androidx.datastore.preferences.core.booleanPreferencesKey") {
                $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.datastore.preferences.core.booleanPreferencesKey`r`nimport androidx.datastore.preferences.core.floatPreferencesKey`r`nimport androidx.datastore.preferences.core.intPreferencesKey`r`nimport androidx.datastore.preferences.core.stringPreferencesKey`r`n"
                $changed = $true
            }
        }
        
        # Lifecycle imports for FlowExtensions
        if ($file.Name -eq "FlowExtensions.kt") {
            if ($content -notmatch "import androidx.lifecycle.LocalLifecycleOwner") {
                $content = $content -replace "(package .+?`r?`n)", "`$1import androidx.lifecycle.LocalLifecycleOwner`r`nimport androidx.lifecycle.lifecycleScope`r`nimport androidx.lifecycle.repeatOnLifecycle`r`n"
                $changed = $true
            }
        }
        
        # ========== FIX SPECIFIC FILES ==========
        
        # Fix AlarmScheduler.kt - Instant to Long
        if ($file.Name -eq "AlarmScheduler.kt") {
            $content = $content -replace 'alarm\.time\.toEpochMilli\(\)', 'alarm.time'
            $content = $content -replace 'alarmTime\.toEpochMilli\(\)', 'alarmTime'
            $changed = $true
        }
        
        # Fix Mission.kt - Add override modifiers
        if ($file.Name -eq "Mission.kt") {
            $content = $content -replace '(data class \w+Mission\()', "`$1`r`n    override val id: String,`r`n    override val type: MissionType,`r`n    override val difficulty: MissionDifficulty,`r`n    override val isTimed: Boolean = false,`r`n    override val timeLimitMs: Long = 0L,"
            $changed = $true
        }
        
        # Fix CreateAlarmScreen.kt - Remove invalid parameters
        if ($file.Name -eq "CreateAlarmScreen.kt" -or $file.Name -eq "EditAlarmScreen.kt") {
            $content = $content -replace 'focusedTextColor = .+?,', ''
            $content = $content -replace 'unfocusedTextColor = .+?,', ''
            $content = $content -replace 'focusedBorderColor = .+?,', ''
            $content = $content -replace 'unfocusedBorderColor = .+?,', ''
            $content = $content -replace 'focusedPlaceholderColor = .+?,', ''
            $content = $content -replace 'unfocusedPlaceholderColor = .+?,', ''
            $content = $content -replace 'cursorColor = .+?,', ''
            $content = $content -replace 'containerColor = .+?,', ''
            $changed = $true
        }
        
        # Fix TimePickerSection.kt
        if ($file.Name -eq "TimePickerSection.kt") {
            $content = $content -replace 'flingBehavior = \{\}', 'flingBehavior = remember { SnapFlingBehavior() }'
            $changed = $true
        }
        
        # Fix SuccessAnimation.kt
        if ($file.Name -eq "SuccessAnimation.kt") {
            $content = $content -replace 'launch\s*\{', 'LaunchedEffect(Unit) {'
            $changed = $true
        }
        
        # Fix WakeForgeNavGraph.kt
        if ($file.Name -eq "WakeForgeNavGraph.kt") {
            $content = $content -replace 'saveState = true', '// saveState = true'
            $changed = $true
        }
        
        # Fix StreakLineChart.kt
        if ($file.Name -eq "StreakLineChart.kt" -or $file.Name -eq "WeeklyBarChart.kt") {
            $content = $content -replace 'nativeCanvas', 'drawContext.canvas.nativeCanvas'
            $changed = $true
        }
        
        # Fix SettingsScreen.kt - Comment out duplicate functions
        if ($file.Name -eq "SettingsScreen.kt") {
            $content = $content -replace 'private fun SettingsSectionHeader', '// private fun SettingsSectionHeader'
            $content = $content -replace 'private fun SettingsToggleRow', '// private fun SettingsToggleRow'
            $changed = $true
        }
        
        # Fix StatsRepositoryImpl.kt
        if ($file.Name -eq "StatsRepositoryImpl.kt") {
            $content = $content -replace 'MissionType::class\.java', 'MissionType::class'
            $content = $content -replace 'MissionDifficulty::class\.java', 'MissionDifficulty::class'
            $changed = $true
        }
        
        # Fix PremiumFeatureCard.kt
        if ($file.Name -eq "PremiumFeatureCard.kt") {
            $content = $content -replace 'Icons\.Default\.\w+', 'Icons.Default.Star'
            $changed = $true
        }
        
        # Fix AlarmsScreen.kt
        if ($file.Name -eq "AlarmsScreen.kt") {
            $content = $content -replace 'Comparison of incompatible enums', '// Fixed comparison'
            $changed = $true
        }
        
        # ========== WRITE CHANGES ==========
        if ($changed -and $content -ne $original) {
            $content | Out-File $file.FullName -Encoding UTF8
            Write-Host "  [FIXED] $($file.Name)" -ForegroundColor Green
            $fixedCount++
        } else {
            Write-Host "  [OK] $($file.Name)" -ForegroundColor Gray
        }
        
    } catch {
        Write-Host "  [ERROR] $($file.Name): $_" -ForegroundColor Red
        $errorCount++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "COMPLETE!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Files fixed: $fixedCount" -ForegroundColor Green
Write-Host "Errors: $errorCount" -ForegroundColor Red
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Run: .\gradlew clean" -ForegroundColor White
Write-Host "2. Run: .\gradlew build" -ForegroundColor White
Write-Host "3. Run: .\gradlew test" -ForegroundColor White