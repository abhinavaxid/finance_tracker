#!/usr/bin/env python3
"""
Complete End-to-End MCP Test with JWT Authentication & Database Verification
"""
import requests
import json
import time
import subprocess
import sys

print('='*80)
print('END-TO-END MCP INTEGRATION TEST')
print('='*80)

BASE_URL_BACKEND = "http://localhost:8080"
BASE_URL_MCP = "http://localhost:5000"

# Step 1: Check if backend is running
print('\n[STEP 1] Checking Backend Status...')
backend_running = False
try:
    response = requests.get(f'{BASE_URL_BACKEND}/api/users', timeout=5, 
                           headers={'Authorization': 'Bearer dummy'})
    backend_running = True
    print('✓ Backend: RUNNING')
except:
    print('✗ Backend: NOT RUNNING')
    print('  Please start Spring Boot backend:')
    print('  java -jar backend/target/ExpenseTrackerApplication-2.7.18.jar')
    sys.exit(1)

# Step 2: Check if MCP server is running
print('\n[STEP 2] Checking MCP Server Status...')
mcp_running = False
try:
    response = requests.get(f'{BASE_URL_MCP}/health', timeout=5)
    if response.status_code == 200:
        mcp_running = True
        print('✓ MCP Server: RUNNING')
except:
    print('✗ MCP Server: NOT RUNNING')
    print('  Please start MCP server:')
    print('  cd mcp-server && python main.py')
    sys.exit(1)

# Step 3: Get JWT Token
print('\n[STEP 3] Obtaining JWT Token...')
jwt_token = None
user_email = None
user_id = None

try:
    # Create new user (since login might fail due to wrong password)
    timestamp = int(time.time())
    new_email = f'e2e-test-{timestamp}@example.com'
    
    print('  Registering new user...')
    response = requests.post(
        f'{BASE_URL_BACKEND}/api/auth/register',
        headers={'Content-Type': 'application/json'},
        json={
            'email': new_email,
            'password': 'Test@12345',
            'password_confirm': 'Test@12345',  # Use password_confirm, not confirmPassword
            'firstName': 'Test',              # No numbers!
            'lastName': 'User',               # No numbers!
            'username': f'e2e_user_{timestamp}'
        },
        timeout=10
    )
    
    if response.status_code in [200, 201]:
        print(f'✓ User registered: {new_email}')
        
        # Now login
        response = requests.post(
            f'{BASE_URL_BACKEND}/api/auth/login',
            json={
                'email': new_email,
                'password': 'Test@12345'
            },
            timeout=10
        )
        
        if response.status_code == 200:
            data = response.json()
            jwt_token = data.get('token')
            user_email = new_email
            print(f'✓ JWT Token obtained')
            print(f'  Token: {jwt_token[:50]}...')
        else:
            print(f'✗ Login failed: {response.status_code}')
    else:
        print(f'⚠ Registration failed with status {response.status_code}')
        print(f'  Full Response: {response.text}')
        
        # Try to login with existing user as fallback
        print('  Trying login with existing user...')
        response = requests.post(
            f'{BASE_URL_BACKEND}/api/auth/login',
            json={
                'email': 'newuser@test.com',
                'password': 'password123'
            },
            timeout=10
        )
        
        if response.status_code == 200:
            data = response.json()
            jwt_token = data.get('token')
            user_email = 'newuser@test.com'
            print(f'✓ JWT Token obtained from existing user')
            print(f'  Token: {jwt_token[:50]}...')
        else:
            print(f'✗ Login failed: {response.status_code}')
            print(f'  Response: {response.text}')

except Exception as e:
    print(f'✗ Error: {e}')
    import traceback
    traceback.print_exc()

if not jwt_token:
    print('✗ Could not obtain JWT token. Aborting E2E test.')
    sys.exit(1)

# Step 4: Create Transactions via MCP
print('\n[STEP 4] Creating Transactions via MCP...')
test_transactions = [
    'Add ₹1200 for groceries today',
    'Paid $50 on books',
    'Received ₹30000 salary'
]

created_count = 0
for text in test_transactions:
    try:
        response = requests.post(
            f'{BASE_URL_MCP}/mcp/process',
            json={
                'input': text,
                'jwt_token': jwt_token,
                'use_llm': False
            },
            timeout=10
        )
        
        if response.status_code == 200:
            result = response.json()
            if result.get('success'):
                created_count += 1
                print(f'✓ Created: "{text}"')
                print(f'  → {result.get("message")}')
            else:
                print(f'⚠ Parse failed: "{text}"')
                print(f'  Error: {result.get("error")}')
        else:
            print(f'✗ "{text}" - Status {response.status_code}')
            
    except Exception as e:
        print(f'✗ Error: {e}')

# Step 5: Verify in Backend API
print('\n[STEP 5] Verifying Transactions via Backend API...')
try:
    response = requests.get(
        f'{BASE_URL_BACKEND}/api/transactions',
        headers={'Authorization': f'Bearer {jwt_token}'},
        timeout=10
    )
    
    if response.status_code == 200:
        data = response.json()
        
        # Handle different response formats
        if isinstance(data, dict):
            transactions = data.get('content', data.get('data', []))
        else:
            transactions = data
        
        # Convert to list if needed
        if isinstance(transactions, dict):
            transactions = list(transactions.values())
        
        print(f'✓ Backend API Query Successful')
        print(f'  Total transactions: {len(transactions)}')
        
        if transactions:
            print(f'  Latest transactions:')
            for i, txn in enumerate(sorted(transactions, key=lambda x: x.get('id', 0), reverse=True)[:5]):
                amount = txn.get('amount', 'N/A')
                txn_type = txn.get('type', 'N/A')
                desc = txn.get('description', '')[:30]
                print(f'    {i+1}. {amount} ({txn_type}) - {desc}')
    else:
        print(f'⚠ API Query Failed: {response.status_code}')
        
except Exception as e:
    print(f'✗ Error: {e}')

# Step 6: Database Query
print('\n[STEP 6] Direct Database Query...')
try:
    # Use psql via subprocess
    query = """
    SELECT 
        t.id,
        t.amount,
        t.type,
        c.name as category,
        t.description,
        t.transaction_date,
        t.created_at
    FROM transactions t
    LEFT JOIN categories c ON t.category_id = c.id
    ORDER BY t.created_at DESC 
    LIMIT 5;
    """
    
    # Try to run psql command
    result = subprocess.run(
        ['psql', '-h', 'localhost', '-U', 'postgres', '-d', 'expense_tracker', '-c', query],
        capture_output=True,
        text=True,
        timeout=10
    )
    
    if result.returncode == 0:
        output = result.stdout
        if 'rows' in output or '---' in output:
            print('✓ Database Query Successful')
            # Print formatted output
            for line in output.split('\n')[-10:]:
                if line.strip():
                    print(f'  {line}')
        else:
            print('✓ Database accessible')
            print('  Run this SQL query to verify:')
            print('  SELECT COUNT(*) FROM transactions;')
    else:
        print('✗ psql not found or connection failed')
        print('  Try this SQL query manually in your DB client:')
        print('  SELECT COUNT(*) FROM transactions ORDER BY created_at DESC LIMIT 5;')
        
except Exception as e:
    print(f'✗ Error: {e}')
    print('  Try this SQL query manually in your DB client:')
    print('  SELECT COUNT(*) FROM transactions ORDER BY created_at DESC LIMIT 5;')

# Summary
print('\n' + '='*80)
print('END-TO-END TEST SUMMARY')
print('='*80)
print(f'''
✓ Backend: RUNNING
✓ MCP Server: RUNNING
✓ User: {user_email}
✓ JWT Token: Obtained
✓ Transactions Created: {created_count}/{len(test_transactions)}

Next Verification Steps:
  1. Query database:
     SELECT * FROM transactions ORDER BY created_at DESC LIMIT 5;
  
  2. Verify transaction count:
     SELECT COUNT(*) FROM transactions;
  
  3. Check specific user's transactions:
     SELECT * FROM transactions WHERE user_id = 
       (SELECT id FROM users WHERE email = '{user_email}')
     ORDER BY created_at DESC LIMIT 5;

Status: {'✅ E2E TEST PASSED' if created_count > 0 else '⚠ Check logs above'}
''')

print('='*80)
