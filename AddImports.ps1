$imports = @(
    "import androidx.compose.ui.Alignment",
    "import androidx.compose.ui.graphics.StrokeCap",
    "import androidx.compose.material3.rememberDismissState"
)
$importBlock = ($imports -join "`r`n") + "`r`n"

# Search for .kt files in the app source directory
Get-ChildItem -Path "app/src/main/java" -Filter "*.kt" -Recurse | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    # Only add if the import isn't already there to avoid duplicates
    if ($content -notmatch "import androidx.compose.ui.Alignment") {
        $newContent = $importBlock + $content
        Set-Content -Path $_.FullName -Value $newContent -Encoding UTF8
        Write-Host "Updated: $($_.Name)"
    } else {
        Write-Host "Skipped: $($_.Name) (Already has imports)"
    }
}
