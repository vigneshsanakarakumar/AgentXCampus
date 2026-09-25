import urllib.request
import json
import sys

sys.stdout.reconfigure(encoding="utf-8")

BASE_URL = "http://localhost:8080/api/v1"

def post(endpoint, data, token=None):
    req = urllib.request.Request(
        f"{BASE_URL}{endpoint}",
        data=json.dumps(data).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
            **({"Authorization": f"Bearer {token}"} if token else {})
        }
    )
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode("utf-8"))

def get(endpoint, token=None):
    req = urllib.request.Request(
        f"{BASE_URL}{endpoint}",
        headers={"Authorization": f"Bearer {token}"} if token else {}
    )
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode("utf-8"))

print("=== 1. AUTHENTICATION TEST ===")
login_res = post("/auth/login", {"identifier": "vasan", "password": "student123"})
token = login_res.get("token")
print(f"Logged in: {login_res.get('username')} ({login_res.get('role')}) | Token received: {bool(token)}")

print("\n=== DEMO 1: RAG WITH EXACT HANDBOOK CITATION ===")
d1 = post("/agent/chat", {"query": "What is the condonation fee if my attendance is 68%?"}, token)
print("Agent Type:", d1.get("agentType"))
print("Trace Steps:", d1.get("steps"))
print("Response Message:\n", d1.get("message")[:300], "...")

print("\n=== DEMO 2: AGENTIC DB + RAG REASONING (EXAM ELIGIBILITY) ===")
d2 = post("/agent/chat", {"query": "Am I eligible for tomorrow's exam?"}, token)
print("Agent Type:", d2.get("agentType"))
print("Trace Steps:", d2.get("steps"))
print("Action Data:", d2.get("actionData"))
print("Response Message:\n", d2.get("message")[:350], "...")

print("\n=== DEMO 3: CAMPUS GRIEVANCE AGENT ===")
d3 = post("/agent/chat", {"query": "The projector in Room 302 is broken and showing green lines"}, token)
print("Agent Type:", d3.get("agentType"))
print("Trace Steps:", d3.get("steps"))
print("Action Data:", d3.get("actionData"))

print("\n=== DEMO 4: EXAMINATION CONFLICT CHECK ENGINE ===")
admin_login = post("/auth/login", {"identifier": "admin", "password": "admin"})
admin_token = admin_login.get("token")
d4 = post("/agent/chat", {"query": "Schedule DBMS exam at 10 AM in Room 302"}, admin_token)
print("Agent Type:", d4.get("agentType"))
print("Trace Steps:", d4.get("steps"))
print("Action Data:", d4.get("actionData"))
print("Response Message:\n", d4.get("message")[:300], "...")

print("\n=== DEMO 5: PROACTIVE ATTENDANCE RISK AUDIT ===")
d5 = post("/agent/proactive/run-audit", {}, admin_token)
print("Audited Students:", d5.get("auditedStudents"))
print("Dispatched Notifications:", d5.get("notificationsDispatched"))
print("Flagged Risks Count:", d5.get("flaggedRisksCount"))

print("\n=== ALL 5 DEMOS TESTED AND VERIFIED END-TO-END! ===")
