# debug_settings.ps1
$file = "app/src/main/java/com/wakeforge/app/presentation/settings/SettingsScreen.kt"

if (Test-Path $file) {
    $lines = Get-Content $file -Encoding UTF8
    Write-Host "Lines 135-145:" -ForegroundColor Yellow
    for ($i = 134; $i -lt [Math]::Min(145, $lines.Count); $i++) {
        Write-Host "$($i+1): $($lines[$i])" -ForegroundColor White
    }
    
    Write-Host "`nLines 295-305:" -ForegroundColor Yellow
    for ($i = 294; $i -lt [Math]::Min(305, $lines.Count); $i++) {
        Write-Host "$($i+1): $($lines[$i])" -ForegroundColor White
    }
}