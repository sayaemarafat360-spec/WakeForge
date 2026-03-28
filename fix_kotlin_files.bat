# fix_kotlin_files.ps1
$fixedCount = 0
$failedCount = 0

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Fixing Kotlin Files" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Find all Kotlin files
$kotlinFiles = Get-ChildItem -Recurse -Filter "*.kt"

foreach ($file in $kotlinFiles) {
    Write-Host "Fixing: $($file.FullName)"
    
    try {
        # Read all lines
        $lines = Get-Content $file.FullName -Encoding UTF8
        
        # Find the package line
        $packageIndex = -1
        for ($i = 0; $i -lt $lines.Count; $i++) {
            if ($lines[$i] -match '^package\s+') {
                $packageIndex = $i
                break
            }
        }
        
        if ($packageIndex -eq -1) {
            Write-Host "  No package declaration found" -ForegroundColor Yellow
            $failedCount++
            continue
        }
        
        $packageLine = $lines[$packageIndex]
        
        # Collect all imports that come AFTER the package declaration
        $imports = @()
        $restOfFile = @()
        $afterPackage = $false
        
        for ($i = 0; $i -lt $lines.Count; $i++) {
            if (-not $afterPackage) {
                if ($i -eq $packageIndex) {
                    $afterPackage = $true
                }
                continue
            }
            
            # Skip the line if it's after package and is an import
            if ($lines[$i] -match '^import\s+') {
                $imports += $lines[$i]
            } else {
                # First non-import line and everything after
                $restOfFile += $lines[$i]
            }
        }
        
        # Remove any empty lines at the start of restOfFile
        while ($restOfFile.Count -gt 0 -and $restOfFile[0] -eq '') {
            $restOfFile = $restOfFile[1..($restOfFile.Count-1)]
        }
        
        # Sort imports
        $imports = $imports | Sort-Object
        
        # Build the new file content
        $newContent = @()
        $newContent += $packageLine
        $newContent += ""
        
        if ($imports.Count -gt 0) {
            $newContent += $imports
            $newContent += ""
        }
        
        $newContent += $restOfFile
        
        # Write back to file
        $newContent -join "`r`n" | Out-File -FilePath $file.FullName -Encoding UTF8
        
        Write-Host "  Fixed successfully" -ForegroundColor Green
        $fixedCount++
    }
    catch {
        Write-Host "  Error: $_" -ForegroundColor Red
        $failedCount++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Completed!" -ForegroundColor Cyan
Write-Host "Fixed: $fixedCount" -ForegroundColor Green
Write-Host "Failed: $failedCount" -ForegroundColor Red
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Run: gradlew clean" -ForegroundColor White
Write-Host "2. Run: gradlew build" -ForegroundColor White
Write-Host "3. Run: gradlew test" -ForegroundColor White
Read-Host "Press Enter to exit"