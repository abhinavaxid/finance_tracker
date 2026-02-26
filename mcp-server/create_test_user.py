#!/usr/bin/env python3
"""
Create test user directly in database for MCP testing
"""
import psycopg2
from psycopg2.extras import RealDictCursor
import hashlib
import json
from datetime import datetime


def create_test_user():
    """Create a test user in the database"""
    
    try:
        # Connect to PostgreSQL
        conn = psycopg2.connect(
            host="localhost",
            port=5432,
            database="expense_tracker_db",
            user="postgres",
            password="postgres"
        )
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        print("✓ Connected to PostgreSQL database")
        
        # Check if user already exists
        cursor.execute("SELECT id FROM users WHERE email = %s", ("mcptest@example.com",))
        existing_user = cursor.fetchone()
        
        if existing_user:
            print(f"✓ User already exists with ID: {existing_user['id']}")
            cursor.close()
            conn.close()
            return existing_user['id']
        
        # Create new user
        # Note: In production, passwords should be hashed with BCrypt
        # For testing, we'll use a simple hash
        email = "mcptest@example.com"
        password_hash = hashlib.sha256("TestPass@123".encode()).hexdigest()
        first_name = "MCP"
        last_name = "Test"
        
        print(f"\nCreating test user:")
        print(f"  Email: {email}")
        print(f"  Name: {first_name} {last_name}")
        
        cursor.execute("""
            INSERT INTO users (email, password, first_name, last_name, is_active, email_verified, created_at)
            VALUES (%s, %s, %s, %s, true, true, NOW())
            RETURNING id
        """, (email, password_hash, first_name, last_name))
        
        user_id = cursor.fetchone()['id']
        conn.commit()
        
        print(f"✓ User created with ID: {user_id}")
        
        # Assign default role
        cursor.execute("""
            INSERT INTO user_roles (user_id, role_id)
            SELECT %s, id FROM roles WHERE name = 'ROLE_USER' LIMIT 1
        """, (user_id,))
        conn.commit()
        
        print(f"✓ Default role assigned")
        
        cursor.close()
        conn.close()
        
        print("\nYou can now use this user to get a JWT token:")
        print(f"  Email: {email}")
        print(f"  Password: TestPass@123")
        
        return user_id
        
    except psycopg2.OperationalError as e:
        print(f"\n✗ Database connection error:")
        print(f"  Make sure PostgreSQL is running at localhost:5432")
        print(f"  Error: {e}")
        return None
    except Exception as e:
        print(f"\n✗ Error: {e}")
        import traceback
        traceback.print_exc()
        return None


if __name__ == "__main__":
    print("="*60)
    print("Test User Creation Script")
    print("="*60)
    user_id = create_test_user()
    if user_id:
        print("\n✓ Setup complete! User ready for MCP testing.")
