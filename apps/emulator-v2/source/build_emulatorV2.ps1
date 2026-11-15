# Che779 EmulatorV2 1.3.0 - build script (PowerShell)
# Yeu cau: JDK 8+ (javac, jar). Chay:
# powershell -ExecutionPolicy Bypass -File build_emulatorV2.ps1

$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

try {
    # Tim javac va jar
    if ($env:JAVA_HOME) {
        $javac = Join-Path $env:JAVA_HOME 'bin\javac.exe'
        $jar   = Join-Path $env:JAVA_HOME 'bin\jar.exe'
    }
    else {
        $javac = 'javac'
        $jar   = 'jar'
    }

    if (-not (Test-Path -LiteralPath $javac -PathType Leaf) -and
        -not (Get-Command $javac -ErrorAction SilentlyContinue)) {
        throw 'Khong tim thay javac. Cai JDK 8+ va dat JAVA_HOME hoac them vao PATH.'
    }

    if (-not (Test-Path -LiteralPath $jar -PathType Leaf) -and
        -not (Get-Command $jar -ErrorAction SilentlyContinue)) {
        throw 'Khong tim thay jar. Kiem tra lai JDK/JAVA_HOME.'
    }

    & $javac -version 2>&1 | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw 'javac khong chay duoc.'
    }

    # Kiem tra dau vao
    foreach ($required in @('stubs', 'src', 'res', 'lib\asm-3.2.jar')) {
        if (-not (Test-Path -LiteralPath $required)) {
            throw "Khong tim thay: $required"
        }
    }

    # Don cac thu muc build cu va tao lai
    Write-Host '[CLEAN] Don thu muc build cu...'
    foreach ($dir in @('out', 'stubout', 'jarroot')) {
        if (Test-Path -LiteralPath $dir) {
            Remove-Item -LiteralPath $dir -Recurse -Force
        }
        if (Test-Path -LiteralPath $dir) {
            throw "Khong the xoa thu muc $dir."
        }
        New-Item -ItemType Directory -Path $dir | Out-Null
    }

    Remove-Item -LiteralPath 'stubs.txt', 'srcs.txt' -Force -ErrorAction SilentlyContinue

    # Tao response file cho javac.
    # Dung dau / trong duong dan de tranh loi parser voi dau \ tren Windows.
    Write-Host '[LIST] Liet ke file Java...'
    $stubFiles = @(Get-ChildItem -LiteralPath 'stubs' -Recurse -File -Filter '*.java')
    $srcFiles  = @(Get-ChildItem -LiteralPath 'src' -Recurse -File -Filter '*.java')

    if ($stubFiles.Count -eq 0) {
        throw 'Khong tim thay file .java nao trong stubs.'
    }
    if ($srcFiles.Count -eq 0) {
        throw 'Khong tim thay file .java nao trong src.'
    }

    $utf8 = New-Object System.Text.UTF8Encoding($false)
    $stubLines = @($stubFiles | ForEach-Object {
        '"' + ($_.FullName -replace '\\', '/') + '"'
    })
    $srcLines = @($srcFiles | ForEach-Object {
        '"' + ($_.FullName -replace '\\', '/') + '"'
    })

    [IO.File]::WriteAllLines((Join-Path $PWD 'stubs.txt'), [string[]]$stubLines, $utf8)
    [IO.File]::WriteAllLines((Join-Path $PWD 'srcs.txt'), [string[]]$srcLines, $utf8)

    # [1/5] Bien dich stubs
    Write-Host '[1/5] Bien dich stubs...'
    & $javac -source 8 -target 8 -nowarn -encoding UTF-8 -d 'stubout' '@stubs.txt'
    if ($LASTEXITCODE -ne 0) {
        throw 'Bien dich stubs that bai.'
    }

    # [2/5] Bien dich ma nguon
    Write-Host '[2/5] Bien dich ma nguon...'
    & $javac -source 8 -target 8 -nowarn -proc:none -encoding UTF-8 -d 'out' -cp 'lib\asm-3.2.jar;stubout' '@srcs.txt'
    if ($LASTEXITCODE -ne 0) {
        throw 'Bien dich ma nguon that bai.'
    }

    # [3/5] Gom class va tai nguyen
    Write-Host '[3/5] Gom tai nguyen...'
    $outItems = @(Get-ChildItem -LiteralPath 'out' -Force)
    if ($outItems.Count -gt 0) {
        $outItems | Copy-Item -Destination 'jarroot' -Recurse -Force
    }

    $resItems = @(Get-ChildItem -LiteralPath 'res' -Force)
    if ($resItems.Count -gt 0) {
        $resItems | Copy-Item -Destination 'jarroot' -Recurse -Force
    }

    Push-Location 'jarroot'
    try {
        & $jar xf '..\lib\asm-3.2.jar'
        if ($LASTEXITCODE -ne 0) {
            throw 'Khong the giai nen lib\asm-3.2.jar.'
        }
        if (Test-Path -LiteralPath 'META-INF') {
            Remove-Item -LiteralPath 'META-INF' -Recurse -Force
        }
    }
    finally {
        Pop-Location
    }

    # [4/5] Tao manifest
    Write-Host '[4/5] Tao manifest...'
    $manifest = @(
        'Manifest-Version: 1.0'
        'Main-Class: com.che779.emu.Che779Main'
        'Class-Path: lib/jna.jar'
        'Implementation-Title: Che779 EmulatorV2'
        'Implementation-Version: 1.3.0'
        'Created-By: Che779 Team'
        ''
    ) -join "`r`n"

    [IO.File]::WriteAllText((Join-Path $PWD 'manifest.txt'), $manifest, $utf8)

    # [5/5] Dong goi JAR
    Write-Host '[5/5] Dong goi jar...'
    $output = Join-Path $PSScriptRoot 'Che779_EmulatorV2.jar'

    if (Test-Path -LiteralPath $output) {
        Remove-Item -LiteralPath $output -Force
    }
    if (Test-Path -LiteralPath $output) {
        throw 'File Che779_EmulatorV2.jar dang duoc su dung. Hay dong emulator va thu lai.'
    }

    & $jar cfm $output 'manifest.txt' -C 'jarroot' .
    if ($LASTEXITCODE -ne 0) {
        throw 'Dong goi JAR that bai.'
    }
    if (-not (Test-Path -LiteralPath $output -PathType Leaf)) {
        throw 'Lenh jar ket thuc nhung khong tao duoc file dau ra.'
    }

    Remove-Item -LiteralPath 'stubs.txt', 'srcs.txt' -Force -ErrorAction SilentlyContinue
    Write-Host ''
    Write-Host "XONG: $output"
    exit 0
}
catch {
    Write-Host ''
    Write-Host "[LOI] $($_.Exception.Message)" -ForegroundColor Red
    Write-Host '[LOI] Build that bai. Khong dong goi tiep neu buoc truoc bi loi.'
    exit 1
}