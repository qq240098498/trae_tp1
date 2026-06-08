@echo off
chcp 65001
cd /d "%~dp0"

:: ========== 只改这里 ==========
set COMMIT_MSG=.1523281629553499:2441ded84279a2ae63db7d2fbbf6cfdc_6a261c7d76c46c0350cc1fcd.6a261c8576c46c0350cc1fcf.6a261c8543d367259adadd0f:Trae CN.T(2026/6/8 09:36:05)
:: ==============================

:: 排除bat自己，不提交
git reset gitpush.bat >nul 2>&1

:: 提交代码
git add .
git reset gitpush.bat >nul 2>&1
git commit -m "%COMMIT_MSG%"

:: 获取commitId
for /f %%i in ('git rev-parse HEAD') do set COMMIT_ID=%%i

:: 推送
git pull origin main >nul 2>&1
git push origin main

:: 输出结果
echo.
echo ==========================
echo 提交备注：%COMMIT_MSG%
echo 提交ID：%COMMIT_ID%
echo ==========================
echo.
pause