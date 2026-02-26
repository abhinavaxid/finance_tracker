"""
Transaction parser tool for extracting structured data from natural language
"""
from typing import Optional, Dict, Any
from datetime import datetime, timedelta
from decimal import Decimal
import re
import logging

logger = logging.getLogger(__name__)


class TransactionParser:
    """
    Extracts structured transaction data from natural language input
    Supports parsing amounts in multiple currency formats
    """

    # Currency symbols and their codes
    CURRENCY_PATTERNS = {
        "₹": ("INR", 1.0),  # Indian Rupee
        "$": ("USD", 1.0),  # US Dollar
        "€": ("EUR", 1.0),  # Euro
        "£": ("GBP", 1.0),  # British Pound
        "¥": ("JPY", 1.0),  # Japanese Yen
    }

    # Common expense keywords
    EXPENSE_KEYWORDS = {
        "spent", "paid", "bought", "purchased", "charged", "cost",
        "expense", "spent money on", "spent on", "paid for", "add expense",
        "debit", "charged", "out", "eating", "eat", "ate", "delivery"
    }

    # Common income keywords
    INCOME_KEYWORDS = {
        "earned", "received", "got", "income", "salary", "bonus",
        "refund", "credited", "credit", "earning", "earn", "payment received",
        "transferred in", "deposit", "deposited", "added", "received money"
    }

    # Common date patterns
    DATE_KEYWORDS = {
        "today": 0,
        "yesterday": -1,
        "tomorrow": 1,
        "now": 0,
        "today morning": 0,
        "last night": -1,
    }

    @staticmethod
    def parse_amount(text: str) -> Optional[float]:
        """
        Extract numeric amount from text
        Handles various formats: "₹1200", "$50.99", "1,000.50"
        """
        # Remove common words that might interfere
        text_cleaned = text.lower()
        
        # Find currency symbols and amounts
        amount_patterns = [
            r'[₹$€£¥]\s*([0-9,]+(?:\.[0-9]{2})?)',  # Currency symbol followed by amount
            r'([0-9,]+(?:\.[0-9]{2})?)\s*(?:inr|usd|eur|gbp|jpy)',  # Amount followed by currency code
            r'(?:amount|of|rupees|dollars|euros)\s*(?:is)?\s*:?\s*([0-9,]+(?:\.[0-9]{2})?)',  # "amount is 1200"
            r'([0-9,]+(?:\.[0-9]{2})?)',  # Plain number
        ]

        for pattern in amount_patterns:
            match = re.search(pattern, text_cleaned)
            if match:
                amount_str = match.group(1).replace(",", "")
                try:
                    amount = float(amount_str)
                    if amount > 0:
                        return amount
                except ValueError:
                    continue

        return None

    @staticmethod
    def parse_date(text: str) -> Optional[datetime]:
        """
        Extract date from text
        Handles: "today", "yesterday", "tomorrow", specific dates
        """
        text_lower = text.lower()

        # Check for keyword dates
        for keyword, offset_days in TransactionParser.DATE_KEYWORDS.items():
            if keyword in text_lower:
                return datetime.now() + timedelta(days=offset_days)

        # Check for month/day patterns: "15 Jan", "Jan 15", "2024-01-15"
        date_patterns = [
            r'(\d{1,2})\s+([a-z]{3,9})',  # "15 January"
            r'([a-z]{3,9})\s+(\d{1,2})',  # "January 15"
            r'(\d{4})-(\d{1,2})-(\d{1,2})',  # "2024-01-15"
            r'(\d{1,2})/(\d{1,2})/(\d{4})',  # "15/01/2024"
        ]

        for pattern in date_patterns:
            match = re.search(pattern, text_lower)
            if match:
                try:
                    # Try to parse the matched date
                    matched_str = match.group(0)
                    for fmt in ["%d %B", "%d %b", "%B %d", "%b %d", "%Y-%m-%d", "%d/%m/%Y"]:
                        try:
                            parsed_date = datetime.strptime(matched_str, fmt)
                            # Set to current year if not specified
                            if parsed_date.year == 1900:
                                parsed_date = parsed_date.replace(year=datetime.now().year)
                            return parsed_date
                        except ValueError:
                            continue
                except Exception as e:
                    logger.warning(f"Could not parse date: {e}")
                    continue

        return None

    @staticmethod
    def detect_type(text: str) -> str:
        """
        Detect transaction type: EXPENSE or INCOME
        """
        text_lower = text.lower()

        for keyword in TransactionParser.INCOME_KEYWORDS:
            if keyword in text_lower:
                return "INCOME"

        for keyword in TransactionParser.EXPENSE_KEYWORDS:
            if keyword in text_lower:
                return "EXPENSE"

        # Default to EXPENSE
        return "EXPENSE"

    @staticmethod
    def extract_category_hint(text: str) -> Optional[str]:
        """
        Extract category hint from text
        Looks for patterns like "for [something]", "on [something]", "at [place]"
        """
        # Common patterns for category extraction
        patterns = [
            r"for\s+(?:a\s+)?([a-z\s&]+?)(?:\s+(?:today|yesterday|tomorrow|on|at)|$)",
            r"on\s+([a-z\s&]+?)(?:\s+(?:today|yesterday|tomorrow)|$)",
            r"(?:at|in)\s+([a-z\s&]+?)(?:\s+(?:today|yesterday|tomorrow)|$)",
            r"(?:bought|spent|paid)\s+(?:a|an)?\s+([a-z\s&]+?)(?:\s+(?:for|on|at)|$)",
        ]

        text_lower = text.lower()
        for pattern in patterns:
            match = re.search(pattern, text_lower)
            if match:
                hint = match.group(1).strip()
                if hint and len(hint) > 1:
                    return hint

        return None

    @staticmethod
    def extract_description(text: str, category_hint: Optional[str] = None) -> str:
        """
        Extract description from text
        Removes amounts, dates, and common transaction words
        """
        description = text
        
        # Remove amounts
        description = re.sub(r'[₹$€£¥]\s*[0-9,]+(?:\.[0-9]{2})?', "", description)
        description = re.sub(r'[0-9,]+(?:\.[0-9]{2})?', "", description)
        
        # Remove dates
        description = re.sub(r'(?:today|yesterday|tomorrow|now|morning|night)', "", description)
        description = re.sub(r'\d{1,2}\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)', "", description)
        
        # Remove transaction type keywords
        for keyword in list(TransactionParser.EXPENSE_KEYWORDS) + list(TransactionParser.INCOME_KEYWORDS):
            description = re.sub(rf'\b{keyword}\b', "", description, flags=re.IGNORECASE)
        
        # Remove prepositions
        description = re.sub(r'\b(for|on|at|in|to|from)\b', "", description, flags=re.IGNORECASE)
        
        # Clean up extra spaces
        description = re.sub(r'\s+', " ", description).strip()
        
        # If category hint is mentioned, include it as description
        if not description and category_hint:
            description = category_hint
        
        return description or "Transaction"

    @classmethod
    def parse(cls, text: str) -> Dict[str, Any]:
        """
        Parse natural language text into structured transaction data
        
        Args:
            text: Natural language input (e.g., "Add ₹1200 for groceries today")
        
        Returns:
            Dictionary with parsed transaction data
        """
        logger.info(f"Parsing transaction input: {text}")
        
        amount = cls.parse_amount(text)
        transaction_date = cls.parse_date(text)
        transaction_type = cls.detect_type(text)
        category_hint = cls.extract_category_hint(text)
        description = cls.extract_description(text, category_hint)
        
        # Calculate confidence based on what was successfully parsed
        confidence = 0.0
        if amount:
            confidence += 0.4
        if category_hint:
            confidence += 0.3
        if transaction_date:
            confidence += 0.2
        if description and description != "Transaction":
            confidence += 0.1

        result = {
            "action": "CREATE",
            "original_input": text,
            "amount": amount,
            "category_hint": category_hint,
            "type": transaction_type,
            "description": description,
            "transaction_date": transaction_date.date().isoformat() if transaction_date else None,
            "payment_method": None,
            "confidence": min(confidence, 1.0),
            "clarification_needed": confidence < 0.5 if amount else True,
        }

        logger.debug(f"Parsed transaction: {result}")
        return result
