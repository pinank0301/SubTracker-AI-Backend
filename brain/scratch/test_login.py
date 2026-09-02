import requests

try:
    r = requests.post('http://localhost:8080/api/auth/login', json={
        'email': 'realuser@example.com',
        'password': 'Password123!'
    }, timeout=5)
    print("Login response:", r.status_code, r.text)
except Exception as e:
    print("Login error:", e)
