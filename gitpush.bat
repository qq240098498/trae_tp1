@echo off
chcp 65001
cd /d "%~dp0"

:: ========== 只有这2个地方需要你改 ==========
set REPO_URL=https://github.com/qq240098498/trae_tp1
set COMMIT_MSG=自动提交
:: ==========================================

git add .
git commit -m "%COMMIT_MSG%"

for /f %%i in ('git rev-parse HEAD') do set COMMIT_ID=%%i

git pull origin main --allow-unrelated-histories >nul
git push origin main

echo.
echo ==============================
echo 仓库地址：%REPO_URL%
echo 提交ID：%COMMIT_ID%
echo ==============================
echo.

pause