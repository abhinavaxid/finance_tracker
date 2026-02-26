# MCP Server - Finance Tracker Integration

Model Context Protocol (MCP) Server for natural language transaction processing using GLM-5 AI model via CCAPI.

## Features

- 🗣️ **Natural Language Processing**: Convert spoken language to structured transactions
- 🤖 **GLM-5 Integration**: Uses CCAPI's GLM-5 model with tool calling for accurate parsing
- 📱 **Full Transaction CRUD**: Create, read, update, and delete transactions via MCP
- 🔐 **JWT Authentication**: Secure communication with backend
- 📊 **Batch Processing**: Process multiple transactions at once
- 🎯 **Category Matching**: Fuzzy matching for category resolution

## Prerequisites

- Python 3.8+
- CCAPI API Key (from https://ccapi.ai/)
- Running Finance Tracker backend (Spring Boot on port 8080)

## Installation

### 1. Clone and Navigate to MCP Server Directory

```bash
cd mcp-server
```

### 2. Create Virtual Environment

```bash
# Windows
python -m venv venv
venv\Scripts\activate

# macOS/Linux
python3 -m venv venv
source venv/bin/activate
```

### 3. Install Dependencies

```bash
pip install -r requirements.txt
```

### 4. Configure Environment

Copy `.env.example` to `.env` and update with your configuration:

```bash
cp .env.example .env
```

Edit `.env` with your CCAPI API key:

```env
CCAPI_API_KEY=sk-ccapi-your-actual-api-key
BACKEND_API_URL=http://localhost:8080/api
```

## Running the Server

### Development Mode

```bash
python main.py
```

This starts the server on `http://localhost:5000` by default.

### Using Uvicorn Directly

```bash
uvicorn main:app --reload --host 0.0.0.0 --port 5000
```

### Production Mode

```bash
uvicorn main:app --host 0.0.0.0 --port 5000 --workers 4
```

## API Endpoints

### Health Check

```bash
GET /health
```

Returns server status and configuration.

### Main Processing Endpoint

```bash
POST /mcp/process
```

Process natural language transaction input.

**Request:**
```json
{
  "input": "Add ₹1200 for groceries today",
  "jwt_token": "your-jwt-token-here",
  "use_llm": true
}
```

**Response:**
```json
{
  "success": true,
  "message": "✓ − ₹1200.00 added to Food & Dining on 2024-01-15",
  "action": "CREATE",
  "transaction": {
    "id": 123,
    "categoryId": 5,
    "categoryName": "Food & Dining",
    "amount": 1200.00,
    "type": "EXPENSE",
    "description": "groceries",
    "transactionDate": "2024-01-15",
    "createdAt": "2024-01-15T10:30:00"
  },
  "confidence": 0.95
}
```

### List Available Tools

```bash
GET /mcp/tools
```

Returns list of available MCP tools.

### Batch Processing

```bash
POST /mcp/batch-process
```

Process multiple transactions in one request.

**Request:**
```json
[
  {
    "input": "Add ₹1200 for groceries today",
    "jwt_token": "token1",
    "use_llm": true
  },
  {
    "input": "Received ₹50000 salary",
    "jwt_token": "token2",
    "use_llm": true
  }
]
```

## Usage Examples

### Example 1: Simple Expense Creation

```bash
curl -X POST http://localhost:5000/mcp/process \
  -H "Content-Type: application/json" \
  -d '{
    "input": "Add ₹1200 for groceries today",
    "jwt_token": "your-jwt-token",
    "use_llm": true
  }'
```

### Example 2: Income Transaction

```bash
curl -X POST http://localhost:5000/mcp/process \
  -H "Content-Type: application/json" \
  -d '{
    "input": "Received ₹50000 salary yesterday",
    "jwt_token": "your-jwt-token",
    "use_llm": true
  }'
```

### Example 3: Using Local Parser (No LLM Cost)

```bash
curl -X POST http://localhost:5000/mcp/process \
  -H "Content-Type: application/json" \
  -d '{
    "input": "Paid $15 for coffee",
    "jwt_token": "your-jwt-token",
    "use_llm": false
  }'
```

### Example 4: Batch Processing

```bash
curl -X POST http://localhost:5000/mcp/batch-process \
  -H "Content-Type: application/json" \
  -d '[
    {
      "input": "Add ₹500 for lunch",
      "jwt_token": "token1",
      "use_llm": true
    },
    {
      "input": "Paid ₹2000 to gym",
      "jwt_token": "token1",
      "use_llm": true
    }
  ]'
```

## Parsing Examples

The MCP server can parse various natural language formats:

```
# Simple amount and category
"Add ₹1200 for groceries"
→ amount: 1200, category_hint: "groceries", type: "EXPENSE"

# With date
"Paid ₹500 for coffee yesterday"
→ amount: 500, category_hint: "coffee", type: "EXPENSE", date: "2024-01-14"

# Income transaction
"Received ₹30000 from freelance project"
→ amount: 30000, category_hint: "freelance project", type: "INCOME"

# Multiple currencies
"Spent $50 on books"
→ amount: 50, category_hint: "books", type: "EXPENSE"

# Detailed description
"Paid ₹2500 to gym membership for January"
→ amount: 2500, category_hint: "gym", description: "gym membership for January"
```

## Configuration Options

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MCP_SERVER_HOST` | 0.0.0.0 | Server host address |
| `MCP_SERVER_PORT` | 5000 | Server port |
| `DEBUG` | False | Enable debug mode |
| `BACKEND_API_URL` | http://localhost:8080/api | Backend API URL |
| `BACKEND_TIMEOUT` | 30 | Backend request timeout (seconds) |
| `CCAPI_API_KEY` | (required) | CCAPI authentication key |
| `CCAPI_BASE_URL` | https://api.ccapi.ai/v1 | CCAPI endpoint |
| `LLM_MODEL` | zhipu/glm-5 | GLM-5 model name |
| `LLM_TEMPERATURE` | 0.7 | Model temperature (creativity) |
| `LLM_TOP_P` | 0.9 | Model top-p (diversity) |
| `LLM_MAX_TOKENS` | 1000 | Max tokens per response |

## Integration with Frontend

The MCP server is designed to be called from your frontend application:

### React/Vue Example

```javascript
async function processTransaction(userInput, jwtToken) {
  const response = await fetch('http://localhost:5000/mcp/process', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      input: userInput,
      jwt_token: jwtToken,
      use_llm: true
    })
  });
  
  const result = await response.json();
  if (result.success) {
    console.log('Transaction created:', result.transaction);
  } else {
    console.error('Error:', result.errorDetails);
  }
}

// Usage
processTransaction('Add ₹1200 for groceries today', 'your-jwt-token');
```

## Development

### Running Tests

```bash
# Currently no tests - contribute!
pytest
```

### Debugging

Enable debug logging:

```bash
export DEBUG=True
python main.py
```

## Troubleshooting

### "Invalid or expired token"

- Verify JWT token is valid and not expired
- Check token format - should be in `Bearer sk-...` format
- Ensure backend is running and `/api/auth/verify` endpoint is accessible

### "Could not match category"

- The category hint may not match any of the user's categories
- Use `/mcp/tools` to see available tools
- Try a different category name or check your backend categories

### "CCAPI_API_KEY not set"

- Copy `.env.example` to `.env`
- Add your CCAPI API key to `.env`
- Restart the server

### "Connection refused" to backend

- Ensure Finance Tracker backend is running on port 8080
- Check `BACKEND_API_URL` in `.env` is correct
- Verify no firewall is blocking localhost:8080

## Performance Tips

1. **Use Local Parser for Simple Cases**: Set `use_llm: false` for pattern-based transactions to save API costs

2. **Batch Similar Transactions**: Use `/mcp/batch-process` for multiple transactions

3. **Cache JWT Tokens**: Reuse valid tokens instead of requesting new ones

4. **Enable Caching**: Consider adding Redis caching layer for repeated category queries

## Security

- Always use HTTPS in production
- Store CCAPI API key securely (use environment variables)
- Validate JWT tokens on each request
- Rate limit the `/mcp/process` endpoint
- Log all transactions for audit trail

## Contributing

Contributions welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

MIT License - see LICENSE file for details

## Support

For issues and questions:

1. Check the [Troubleshooting](#troubleshooting) section
2. Review existing GitHub issues
3. Create a new issue with detailed description
4. Include logs and configuration (sanitize API keys)

## Roadmap

- [ ] WebSocket support for real-time updates
- [ ] File upload for receipt processing (OCR)
- [ ] Multi-language support
- [ ] Advanced filtering and search
- [ ] Analytics integration
- [ ] Mobile app support

---

**Last Updated**: February 23, 2026

Get your CCAPI key: https://ccapi.ai/
