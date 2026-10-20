<#
    一键开关：8081 在监听就停止，否则启动 target\score-web.war。

    用法：双击「一键开关.cmd」，或：
      powershell -NoProfile -ExecutionPolicy Bypass -File tools\toggle-app.ps1 [-Stop] [-OpenBrowser] [-Port 8081]

    输出全用 ASCII，避免 PowerShell 5.1 按 GBK 解码 UTF-8 脚本产生乱码；
    启动日志写 logs\app.log，标准错误写 logs\app.err.log。
#>
param(
    [int]$Port = 8081,
    [switch]$Stop,
    [switch]$OpenBrowser
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
if (-not $root) { $root = (Get-Location).Path }

function Get-AppListener {
    Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
}

function Stop-App {
    $listener = Get-AppListener
    if (-not $listener) {
        Write-Host ("[SKIP ] nothing is listening on port " + $Port)
        return 0
    }
    foreach ($procId in ($listener | Select-Object -ExpandProperty OwningProcess -Unique)) {
        $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
        $name = if ($proc) { $proc.ProcessName } else { 'pid' }
        Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
        Write-Host ("[STOP ] " + $name + " (PID " + $procId + ")")
    }

    # 等端口真正释放，避免下次启动撞上未退出的进程
    for ($i = 0; $i -lt 25; $i++) {
        Start-Sleep -Milliseconds 200
        if (-not (Get-AppListener)) { break }
    }
    if (Get-AppListener) {
        Write-Host ("[FAIL ] port " + $Port + " is still in use") -ForegroundColor Red
        return 1
    }
    Write-Host ("[ OK  ] stopped, port " + $Port + " released") -ForegroundColor Green
    return 0
}

function Start-App {
    $war = Join-Path $root 'target\score-web.war'
    if (-not (Test-Path $war)) {
        Write-Host ("[FAIL ] not found: " + $war) -ForegroundColor Red
        Write-Host "        build it first:  mvn -DskipTests package"
        return 1
    }

    $logDir = Join-Path $root 'logs'
    if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir | Out-Null }
    $outLog = Join-Path $logDir 'app.log'
    $errLog = Join-Path $logDir 'app.err.log'

    # 工程路径含空格，jar 路径必须加引号
    $argLine = '-jar "' + $war + '"'

    Start-Process -FilePath 'java' `
        -ArgumentList $argLine `
        -WorkingDirectory $root `
        -RedirectStandardOutput $outLog `
        -RedirectStandardError $errLog `
        -WindowStyle Hidden

    for ($i = 0; $i -lt 80; $i++) {
        Start-Sleep -Milliseconds 500
        if (Get-AppListener) { break }
    }

    if (-not (Get-AppListener)) {
        Write-Host ("[FAIL ] app did not listen on port " + $Port) -ForegroundColor Red
        Write-Host ("        see log: " + $outLog)
        return 1
    }

    Write-Host ("[ OK  ] started -> http://localhost:" + $Port + "/login") -ForegroundColor Green
    Write-Host ("        log: " + $outLog)
    if ($OpenBrowser) {
        Start-Process ("http://localhost:" + $Port + "/login")
    }
    return 0
}

if ($Stop) {
    exit (Stop-App)
}

if (Get-AppListener) {
    exit (Stop-App)
} else {
    exit (Start-App)
}
