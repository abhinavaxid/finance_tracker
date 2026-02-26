#!/bin/bash
# MCP Server Quick Start Script

echo "================================================"
echo "Finance Tracker MCP Server - Quick Start"
echo "================================================"
echo ""

# Check Python version
python_version=$(python3 --version 2>&1 | awk '{print $2}')
echo "✓ Python version: $python_version"

# Check if venv exists
if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
fi

# Activate venv
echo "Activating virtual environment..."
source venv/bin/activate

# Install requirements
echo "Installing dependencies..."
pip install -q -r requirements.txt

# Check for .env file
if [ ! -f ".env" ]; then
    echo ""
    echo "⚠️  .env file not found!"
    echo "Creating .env from .env.example..."
    cp .env.example .env
    echo ""
    echo "📝 IMPORTANT: Edit .env and add your CCAPI_API_KEY"
    echo "   Get your key from: https://ccapi.ai/"
    echo ""
fi

# Verify config
echo ""
echo "Checking configuration..."

if grep -q "sk-ccapi-" .env; then
    echo "✓ CCAPI API Key configured"
else
    echo "❌ CCAPI_API_KEY not set in .env"
    echo "   Please add your API key to .env file"
fi

# Start server
echo ""
echo "Starting MCP Server..."
echo "Server will be available at: http://localhost:5000"
echo ""
echo "📚 API Documentation: http://localhost:5000/docs"
echo "❌ Press Ctrl+C to stop the server"
echo ""

python main.py
