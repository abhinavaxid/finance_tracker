@echo off
REM MCP Server Quick Start Script for Windows

echo ================================================
echo Finance Tracker MCP Server - Quick Start
echo ================================================
echo.

REM Check Python version
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Python is not installed or not in PATH
    exit /b 1
)

python --version
echo.

REM Check if venv exists
if not exist "venv" (
    echo Creating virtual environment...
    python -m venv venv
)

REM Activate venv
echo Activating virtual environment...
call venv\Scripts\activate.bat

REM Install requirements
echo Installing dependencies...
pip install -q -r requirements.txt

REM Check for .env file
if not exist ".env" (
    echo.
    echo Warning: .env file not found!
    echo Creating .env from .env.example...
    copy .env.example .env
    echo.
    echo IMPORTANT: Edit .env and add your CCAPI_API_KEY
    echo Get your key from: https://ccapi.ai/
    echo.
)

REM Verify config
echo.
echo Checking configuration...
findstr /M "sk-ccapi-" .env >nul
if %errorlevel% equ 0 (
    echo ✓ CCAPI API Key configured
) else (
    echo ✗ CCAPI_API_KEY not set in .env
    echo Please add your API key to .env file
)

REM Start server
echo.
echo Starting MCP Server...
echo Server will be available at: http://localhost:5000
echo.
echo API Documentation: http://localhost:5000/docs
echo Press Ctrl+C to stop the server
echo.

python main.py
