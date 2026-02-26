"""
Backend API client for communicating with Java expense tracker
"""
import logging
import requests
from typing import Dict, Any, Optional
from config import config

logger = logging.getLogger(__name__)


class BackendClient:
    """
    HTTP client for communicating with the Spring Boot backend
    """

    def __init__(self):
        self.base_url = config.BACKEND_API_URL
        self.timeout = config.BACKEND_TIMEOUT
        self.session = requests.Session()

    def process_mcp_transaction(self, mcp_request: Dict[str, Any], jwt_token: str) -> Dict[str, Any]:
        """
        Send MCP transaction request to backend
        
        Args:
            mcp_request: Dictionary with transaction data from MCP
            jwt_token: JWT token for authentication
        
        Returns:
            Response from backend API
        """
        url = f"{self.base_url}/transactions/mcp/process"
        headers = {
            "Authorization": f"Bearer {jwt_token}",
            "Content-Type": "application/json",
        }

        try:
            logger.info(f"Sending MCP request to {url}")
            response = self.session.post(
                url,
                json=mcp_request,
                headers=headers,
                timeout=self.timeout
            )
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            logger.error(f"Backend request failed: {e}")
            raise

    def get_user_categories(self, jwt_token: str) -> list:
        """
        Get list of user's available categories
        
        Args:
            jwt_token: JWT token for authentication
        
        Returns:
            List of category objects
        """
        url = f"{self.base_url}/categories"
        headers = {
            "Authorization": f"Bearer {jwt_token}",
        }

        try:
            logger.info(f"Fetching categories from {url}")
            response = self.session.get(
                url,
                headers=headers,
                timeout=self.timeout
            )
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            logger.error(f"Failed to fetch categories: {e}")
            return []

    def verify_token(self, jwt_token: str) -> bool:
        """
        Verify if JWT token is valid
        
        Args:
            jwt_token: JWT token to verify
        
        Returns:
            True if token is valid, False otherwise
        """
        url = f"{self.base_url.rsplit('/api', 1)[0]}/api/auth/verify"
        headers = {
            "Authorization": f"Bearer {jwt_token}",
        }

        try:
            logger.info(f"Verifying token at {url}")
            response = self.session.get(
                url,
                headers=headers,
                timeout=self.timeout
            )
            return response.status_code == 200
        except requests.exceptions.RequestException as e:
            logger.warning(f"Token verification failed: {e}")
            return False

    def get_transaction(self, transaction_id: int, jwt_token: str) -> Optional[Dict[str, Any]]:
        """
        Get a specific transaction
        
        Args:
            transaction_id: ID of the transaction
            jwt_token: JWT token for authentication
        
        Returns:
            Transaction object or None if not found
        """
        url = f"{self.base_url}/transactions/{transaction_id}"
        headers = {
            "Authorization": f"Bearer {jwt_token}",
        }

        try:
            logger.info(f"Fetching transaction {transaction_id}")
            response = self.session.get(
                url,
                headers=headers,
                timeout=self.timeout
            )
            if response.status_code == 200:
                return response.json()
            else:
                logger.warning(f"Transaction not found: {response.status_code}")
                return None
        except requests.exceptions.RequestException as e:
            logger.error(f"Failed to fetch transaction: {e}")
            return None


backend_client = BackendClient()
