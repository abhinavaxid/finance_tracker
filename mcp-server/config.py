import os
from typing import Optional
from dotenv import load_dotenv

load_dotenv()

class Config:
    """Configuration for MCP Server"""
    
    # Server Configuration
    MCP_SERVER_HOST: str = os.getenv("MCP_SERVER_HOST", "0.0.0.0")
    MCP_SERVER_PORT: int = int(os.getenv("MCP_SERVER_PORT", "5000"))
    DEBUG: bool = os.getenv("DEBUG", "False").lower() == "true"
    
    # Backend Configuration
    BACKEND_API_URL: str = os.getenv("BACKEND_API_URL", "http://localhost:8080/api")
    BACKEND_TIMEOUT: int = int(os.getenv("BACKEND_TIMEOUT", "30"))
    
    # GLM-5/CCAPI Configuration
    CCAPI_API_KEY: str = os.getenv("CCAPI_API_KEY", "")
    CCAPI_BASE_URL: str = os.getenv("CCAPI_BASE_URL", "https://api.ccapi.ai/v1")
    LLM_MODEL: str = os.getenv("LLM_MODEL", "zhipu/glm-5")
    
    # LLM Configuration
    LLM_TEMPERATURE: float = float(os.getenv("LLM_TEMPERATURE", "0.7"))
    LLM_TOP_P: float = float(os.getenv("LLM_TOP_P", "0.9"))
    LLM_MAX_TOKENS: int = int(os.getenv("LLM_MAX_TOKENS", "1000"))
    
    # Validation
    @classmethod
    def validate(cls):
        """Validate required configuration"""
        errors = []
        
        if not cls.CCAPI_API_KEY:
            errors.append("CCAPI_API_KEY environment variable is not set")
        
        if errors:
            raise ValueError("Configuration errors:\n" + "\n".join(errors))
    
    @classmethod
    def to_dict(cls):
        """Return configuration as dictionary"""
        return {
            "mcp_server_host": cls.MCP_SERVER_HOST,
            "mcp_server_port": cls.MCP_SERVER_PORT,
            "debug": cls.DEBUG,
            "backend_api_url": cls.BACKEND_API_URL,
            "backend_timeout": cls.BACKEND_TIMEOUT,
            "llm_model": cls.LLM_MODEL,
            "llm_temperature": cls.LLM_TEMPERATURE,
            "llm_top_p": cls.LLM_TOP_P,
            "llm_max_tokens": cls.LLM_MAX_TOKENS,
        }


config = Config()
