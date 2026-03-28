# fix_settingscreen_final.ps1
$file = "app/src/main/java/com/wakeforge/app/presentation/settings/SettingsScreen.kt"

if (Test-Path $file) {
    Write-Host "Fixing SettingsScreen.kt..." -ForegroundColor Cyan
    
    $content = Get-Content $file -Raw -Encoding UTF8
    
    # Fix the broken size() calls
    # Pattern: .size(22.dp) is correct, but the error shows something like .size(22.dp) with extra spaces
    $content = $content -replace '\.size\s*\(\s*(\d+)\.dp\s*\)', '.size($1.dp)'
    
    # Fix any size calls that lost their parentheses
    $content = $content -replace '\.size\s+(\d+)\.dp', '.size($1.dp)'
    
    # Fix any lines where the modifier is broken
    $content = $content -replace 'modifier\s*=\s*Modifier\s*\.\s*size', 'modifier = Modifier.size'
    
    # Save the fixed file
    $content | Out-File $file -Encoding UTF8
    Write-Host "Fixed SettingsScreen.kt" -ForegroundColor Green
} else {
    Write-Host "SettingsScreen.kt not found" -ForegroundColor Red
}