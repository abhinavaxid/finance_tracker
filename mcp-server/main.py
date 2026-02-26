"""
MCP (Model Context Protocol) Server for Finance Tracker
Integrates with GLM-5/CCAPI for natural language transaction processing
"""

import logging
from fastapi import FastAPI, HTTPException, Request, Depends
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from typing import Optional, Dict, Any, List
from datetime import datetime
import asyncio

from config import config
from tools import TransactionParser, llm_service, backend_client

# Configure logging
logging.basicConfig(
    level=logging.DEBUG if config.DEBUG else logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# FastAPI app initialization
app = FastAPI(
    title="Finance Tracker MCP Server",
    description="Model Context Protocol Server for Natural Language Transaction Processing",
    version="1.0.0"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==================== Pydantic Models ====================

class ProcessMCPRequest(BaseModel):
    """Request model for MCP transaction processing"""
    input: str = Field(..., description="Natural language transaction input")
    jwt_token: str = Field(..., description="JWT authentication token")
    use_llm: bool = Field(default=True, description="Whether to use GLM-5 or local parser")


class MCPToolRequest(BaseModel):
    """Request model for MCP tool execution"""
    tool_name: str = Field(..., description="Name of the tool to execute")
    input: str = Field(..., description="Input for the tool")
    jwt_token: str = Field(..., description="JWT authentication token")


class HealthResponse(BaseModel):
    """Health check response"""
    status: str
    timestamp: datetime
    config: Dict[str, Any]


# ==================== Startup/Shutdown ====================

@app.on_event("startup")
async def startup_event():
    """Startup event - validate configuration"""
    try:
        config.validate()
        logger.info("✓ Configuration validated successfully")
        logger.info(f"✓ GLM-5 MCP Server starting on {config.MCP_SERVER_HOST}:{config.MCP_SERVER_PORT}")
        logger.info(f"✓ Backend API: {config.BACKEND_API_URL}")
    except ValueError as e:
        logger.error(f"✗ Configuration Error: {e}")
        raise


# ==================== Health & Info Routes ====================

@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint"""
    logger.debug("Health check requested")
    return HealthResponse(
        status="healthy",
        timestamp=datetime.now(),
        config=config.to_dict()
    )


@app.get("/info")
async def info():
    """Get server information"""
    return {
        "name": "Finance Tracker MCP Server",
        "version": "1.0.0",
        "description": "Model Context Protocol Server with GLM-5 Integration",
        "endpoints": {
            "health": "/health",
            "info": "/info",
            "process": "/mcp/process",
            "tools": "/mcp/tools",
            "models": "/mcp/models"
        },
        "llm_model": config.LLM_MODEL,
        "backend_url": config.BACKEND_API_URL
    }


# ==================== MCP Routes ====================

@app.post("/mcp/process")
async def process_transaction(request: ProcessMCPRequest):
    """
    Main endpoint for processing natural language transactions
    
    Accepts natural language input and JWT token, returns parsed/processed transaction
    
    Args:
        request: ProcessMCPRequest with input text and JWT token
    
    Returns:
        Response with processed transaction or error
    """
    logger.info(f"Processing MCP request: {request.input}")

    try:
        # Verify JWT token with backend
        if not backend_client.verify_token(request.jwt_token):
            logger.warning("Invalid or expired JWT token")
            raise HTTPException(status_code=401, detail="Invalid or expired token")

        # Parse transaction using GLM-5 or local parser
        if request.use_llm:
            logger.debug("Using GLM-5 for transaction parsing")
            parsed_data = llm_service.parse_transaction_text(request.input)
        else:
            logger.debug("Using local parser for transaction parsing")
            parsed_data = TransactionParser.parse(request.input)

        # Check if clarification is needed
        if parsed_data.get("clarification_needed") and not parsed_data.get("amount"):
            logger.info("Clarification needed from user")
            return {
                "success": False,
                "error": "Could not understand transaction details",
                "message": "Could you please provide more details? For example: 'Add ₹1200 for groceries today'",
                "confidence": parsed_data.get("confidence", 0),
                "original_input": request.input
            }

        # Send parsed transaction to backend for processing
        logger.debug(f"Sending parsed transaction to backend: {parsed_data}")
        backend_response = backend_client.process_mcp_transaction(parsed_data, request.jwt_token)

        logger.info(f"Successfully processed MCP transaction")
        return backend_response

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error processing MCP request: {e}")
        return JSONResponse(
            status_code=500,
            content={
                "success": False,
                "error": "Internal server error",
                "message": str(e),
                "original_input": request.input
            }
        )


@app.get("/mcp/tools")
async def list_tools():
    """
    List available MCP tools
    
    Returns:
        List of available tools and their descriptions
    """
    logger.debug("Listing available tools")
    return {
        "tools": [
            {
                "name": "parse_transaction",
                "description": "Parse natural language into structured transaction data",
                "input": "Natural language transaction string (e.g., 'Add ₹1200 for groceries today')",
                "output": "Parsed transaction with amount, category, date, type, etc."
            },
            {
                "name": "create_transaction",
                "description": "Create a new transaction in the expense tracker",
                "input": "Parsed transaction data",
                "output": "Created transaction with ID and confirmation message"
            },
            {
                "name": "get_categories",
                "description": "Get list of user's available transaction categories",
                "input": "JWT token",
                "output": "List of categories with IDs and names"
            },
            {
                "name": "get_transaction",
                "description": "Retrieve a specific transaction by ID",
                "input": "Transaction ID and JWT token",
                "output": "Transaction details"
            },
            {
                "name": "delete_transaction",
                "description": "Delete a transaction by ID",
                "input": "Transaction ID and JWT token",
                "output": "Confirmation of deletion"
            }
        ]
    }


@app.get("/mcp/models")
async def list_models():
    """
    List available LLM models
    
    Returns:
        Information about configured models
    """
    return {
        "models": [
            {
                "name": config.LLM_MODEL,
                "provider": "CCAPI",
                "capabilities": ["text generation", "function calling", "structured output"]
            }
        ]
    }


@app.post("/mcp/batch-process")
async def batch_process_transactions(requests_list: List[ProcessMCPRequest]):
    """
    Process multiple transactions in batch
    
    Args:
        requests_list: List of ProcessMCPRequest objects
    
    Returns:
        List of processed transactions
    """
    logger.info(f"Batch processing {len(requests_list)} transactions")
    
    results = []
    for req in requests_list:
        try:
            result = await process_transaction(req)
            results.append(result)
        except Exception as e:
            logger.error(f"Error processing transaction in batch: {e}")
            results.append({
                "success": False,
                "error": str(e),
                "original_input": req.input
            })

    return {"results": results}


# ==================== Fallback Routes ====================

@app.get("/")
async def root():
    """Root endpoint - returns API information"""
    return {
        "message": "Finance Tracker MCP Server",
        "status": "running",
        "documentation": "/docs"
    }


@app.post("/mcp/echo")
async def echo(request: ProcessMCPRequest):
    """
    Echo endpoint for testing
    
    Simply echoes back the input for testing purposes
    """
    logger.debug(f"Echo test: {request.input}")
    return {
        "echo": request.input,
        "timestamp": datetime.now().isoformat()
    }


# ==================== Error Handlers ====================

@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    """Handle HTTP exceptions"""
    logger.error(f"HTTP Exception: {exc.status_code} - {exc.detail}")
    return JSONResponse(
        status_code=exc.status_code,
        content={"detail": exc.detail}
    )


@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    """Handle general exceptions"""
    logger.error(f"Unhandled Exception: {exc}")
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"}
    )


# ==================== Main Entry Point ====================

if __name__ == "__main__":
    import uvicorn
    
    logger.info("Starting Finance Tracker MCP Server...")
    uvicorn.run(
        app,
        host=config.MCP_SERVER_HOST,
        port=config.MCP_SERVER_PORT,
        log_level="debug" if config.DEBUG else "info",
        reload=config.DEBUG
    )
