"""
LLM Service for GLM-5/CCAPI integration
Handles tool-based interactions with GLM-5 model
"""
import json
import logging
from typing import Dict, Any, List, Optional
from openai import OpenAI
from config import config

logger = logging.getLogger(__name__)


class LLMService:
    """
    Service for interacting with GLM-5 model through CCAPI
    Supports tool/function calling for structured output
    """

    def __init__(self):
        """Initialize OpenAI client pointing to CCAPI endpoint"""
        self.client = OpenAI(
            api_key=config.CCAPI_API_KEY,
            base_url=config.CCAPI_BASE_URL
        )
        self.model = config.LLM_MODEL
        self.temperature = config.LLM_TEMPERATURE
        self.top_p = config.LLM_TOP_P
        self.max_tokens = config.LLM_MAX_TOKENS

    def _define_tools(self) -> List[Dict[str, Any]]:
        """
        Define MCP tools that GLM-5 can use
        """
        return [
            {
                "type": "function",
                "function": {
                    "name": "parse_transaction",
                    "description": "Parse natural language into transaction data (amount, category, date, description, type)",
                    "parameters": {
                        "type": "object",
                        "properties": {
                            "amount": {
                                "type": "number",
                                "description": "Transaction amount as a number (e.g., 1200.00)"
                            },
                            "category_hint": {
                                "type": "string",
                                "description": "Category hint or name (e.g., 'groceries', 'food & dining')"
                            },
                            "type": {
                                "type": "string",
                                "enum": ["INCOME", "EXPENSE"],
                                "description": "Transaction type"
                            },
                            "description": {
                                "type": "string",
                                "description": "Transaction description"
                            },
                            "transaction_date": {
                                "type": "string",
                                "description": "Transaction date in YYYY-MM-DD format"
                            },
                            "payment_method": {
                                "type": "string",
                                "enum": ["CASH", "CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "UPI", "WALLET", "OTHER"],
                                "description": "Payment method if specified"
                            },
                            "confidence": {
                                "type": "number",
                                "minimum": 0,
                                "maximum": 1,
                                "description": "Confidence score for the parsing (0-1)"
                            },
                            "clarification_needed": {
                                "type": "boolean",
                                "description": "Whether clarification is needed from user"
                            }
                        },
                        "required": ["amount", "category_hint", "type", "description"]
                    }
                }
            }
        ]

    def parse_transaction_batch(self, inputs: List[str]) -> List[Dict[str, Any]]:
        """
        Parse multiple transaction inputs at once
        
        Args:
            inputs: List of natural language transaction strings
        
        Returns:
            List of parsed transaction dictionaries
        """
        results = []
        for text in inputs:
            result = self.parse_transaction_text(text)
            results.append(result)
        return results

    def parse_transaction_text(self, text: str) -> Dict[str, Any]:
        """
        Parse natural language transaction text using GLM-5 with tool calling
        
        Args:
            text: Natural language input (e.g., "Add ₹1200 for groceries today")
        
        Returns:
            Parsed transaction data
        """
        logger.info(f"Parsing transaction with GLM-5: {text}")

        try:
            system_prompt = """You are an expert financial transaction parser. 
Your task is to extract structured transaction data from natural language input.
When processing a transaction, you MUST use the parse_transaction function to return structured data.
Always extract:
- The numeric amount
- The category or type of expense
- Whether it's income or expense
- Today's date if not specified
- A brief description

Be precise with amounts, including decimals. For categories, extract the most relevant one from the input."""

            messages = [
                {
                    "role": "system",
                    "content": system_prompt
                },
                {
                    "role": "user",
                    "content": f"Parse this transaction: {text}"
                }
            ]

            # Call GLM-5 with tool definition
            response = self.client.chat.completions.create(
                model=self.model,
                messages=messages,
                tools=self._define_tools(),
                tool_choice="auto",
                temperature=self.temperature,
                top_p=self.top_p,
                max_tokens=self.max_tokens
            )

            logger.debug(f"GLM-5 response: {response}")

            # Extract tool call results
            if response.choices[0].message.tool_calls:
                for tool_call in response.choices[0].message.tool_calls:
                    if tool_call.function.name == "parse_transaction":
                        parsed_data = json.loads(tool_call.function.arguments)
                        
                        # Add metadata
                        parsed_data["action"] = "CREATE"
                        parsed_data["original_input"] = text
                        
                        logger.info(f"Successfully parsed transaction: {parsed_data}")
                        return parsed_data
            
            # Fallback if no tool was called
            logger.warning("GLM-5 did not call parse_transaction tool")
            return {
                "action": "CREATE",
                "original_input": text,
                "amount": None,
                "category_hint": None,
                "type": "EXPENSE",
                "description": text,
                "transaction_date": None,
                "confidence": 0.0,
                "clarification_needed": True,
                "error": "Could not parse transaction with GLM-5"
            }

        except Exception as e:
            logger.error(f"Error parsing transaction with GLM-5: {e}")
            return {
                "action": "CREATE",
                "original_input": text,
                "amount": None,
                "category_hint": None,
                "type": "EXPENSE",
                "description": text,
                "transaction_date": None,
                "confidence": 0.0,
                "clarification_needed": True,
                "error": str(e)
            }

    def generate_clarification_response(self, parsing_result: Dict[str, Any], suggestions: List[str]) -> str:
        """
        Generate a natural language clarification question if parsing is ambiguous
        
        Args:
            parsing_result: Parsing result from GLM-5
            suggestions: List of available categories or other options
        
        Returns:
            Clarification message for user
        """
        try:
            messages = [
                {
                    "role": "user",
                    "content": f"""The user said: "{parsing_result['original_input']}"
                    
We parsed it as:
- Amount: {parsing_result.get('amount')}
- Category: {parsing_result.get('category_hint')}
- Type: {parsing_result.get('type')}

But we're not 100% sure. Generate a friendly clarification question.
Available categories: {', '.join(suggestions)}

Keep it short and friendly."""
                }
            ]

            response = self.client.chat.completions.create(
                model=self.model,
                messages=messages,
                temperature=0.7,
                max_tokens=200
            )

            return response.choices[0].message.content
        except Exception as e:
            logger.error(f"Error generating clarification: {e}")
            return f"Did you mean: {parsing_result.get('category_hint')}?"


llm_service = LLMService()
