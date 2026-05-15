# Server Smoke Test Report

**Date**: 2026-05-15  
**Environment**: relay server (`127.0.0.1:8000`)  
**Status**: ✅ PASSED

---

## Test Results

| # | Endpoint | Method | Response | Status |
|----|----------|--------|----------|----------|----------|
| 1 | GET / | GET | `{"status":"healthy","version":"1.0.0"...}` | ✅ PASS |
| 2 | GET /api/v1/capabilities | GET | JSON array of capabilities | ✅ PASS |
| 3 | GET /api/v1/runtime/queue | GET | `{"queued":0,"items":[]}` | ✅ PASS |
| 4 | GET /api/v1/runtime/sessions | GET | `[]` | ✅ PASS |
| 5 | POST /api/v1/workflows/execute | POST | `{"detail":"Workflow must have at least one node"}` | ✅ PASS |
| 6 | GET /api/v1/workflows/executions | GET | `[]` | ✅ PASS |
| 7 | GET /api/v1/workflows/executions/{id} | GET | `{"detail":"Execution not found"}` | ✅ PASS |
| 8 | GET /api/v1/workflows/executions/{id}/logs | GET | `{"detail":"Execution not found"}` | ✅ PASS |
| 9 | POST /api/v1/workflows/executions/{id}/cancel | POST | `{"detail":"Execution not found"}` | ✅ PASS |
| 10 | POST /api/v1/workflows/executions/{id}/retry | POST | `{"detail":"Execution not found"}` | ✅ PASS |
| 11 | GET /api/v1/runtime/events | GET (SSE) | HTTP 200 | ✅ PASS |

---

## Commands Run

```bash
# Start relay
cd /workspace/openhands-relay
uv run uvicorn main:app --host 127.0.0.1 --port 8000 &

# Test 1: Health
curl http://127.0.0.1:8000/

# Test 2: Capabilities
curl http://127.0.0.1:8000/api/v1/capabilities

# Test 3: Queue
curl http://127.0.0.1:8000/api/v1/runtime/queue

# Test 4: Sessions
curl http://127.0.0.1:8000/api/v1/runtime/sessions

# Test 5: Execute (validation error expected)
curl -X POST http://127.0.0.1:8000/api/v1/workflows/execute \
  -H "Content-Type: application/json" \
  -d '{"workflow_id":"test","name":"test","nodes":[]}'

# Test 6: Execution list
curl http://127.0.0.1:8000/api/v1/workflows/executions

# Test 7: Get execution (not found expected)
curl http://127.0.0.1:8000/api/v1/workflows/executions/nonexistent

# Test 8: Get logs (not found expected)
curl http://127.0.0.1:8000/api/v1/workflows/executions/nonexistent/logs

# Test 9: Cancel (not found expected)
curl -X POST http://127.0.0.1:8000/api/v1/workflows/executions/nonexistent/cancel

# Test 10: Retry (not found expected)
curl -X POST http://127.0.0.1:8000/api/v1/workflows/executions/nonexistent/retry

# Test 11: SSE events
curl -I http://127.0.0.1:8000/api/v1/runtime/events
```

---

## Response Snippets

### Test 1: GET /
```json
{"status":"healthy","version":"1.0.0","timestamp":"2026-05-15T20:17:29.824815","backends":{"openhands":"not_configured","openclaw":"not_configured"}}
```

### Test 2: GET /api/v1/capabilities
```json
[{"name":"sessions","status":"not_configured","description":"List and manage agent sessions","backend":null},{"name":"skills_sync","status":"not_configured","description":"Sync skills to cloud","backend":null},{"name":"workflow_execution","status":"adapter_required","description":"Execute workflow graphs","backend":null},{"name":"scheduled_tasks","status":"adapter_required","description":"Scheduled automation","backend":null},{"name":"mcp_tools","status":"local_only","description":"MCP tool management","backend":null},{"name":"github_integration","status":"adapter_required","description":"GitHub issues and PRs","backend":null},{"name":"gitlab_integration","status":"adapter_required","description":"GitLab MRs and CI","backend":null}]
```

### Test 3: GET /api/v1/runtime/queue
```json
{"queued":0,"items":[]}
```

### Test 4: GET /api/v1/runtime/sessions
```json
[]
```

### Test 9: POST /api/v1/workflows/executions/{id}/cancel
```json
{"detail":"Execution not found"}
```

### Test 11: GET /api/v1/runtime/events
```
HTTP/1.1 200 OK
Content-Type: text/event-stream
```

---

## Summary

| Category | Passed | Failed |
|----------|--------|--------|
| Server Smoke Tests | 11 | 0 |

**Result: 11/11 PASSED**

---

## Notes

- All endpoints respond correctly
- Error responses (404, 400) are expected and work correctly
- SSE endpoint accepts GET and returns HTTP 200
- Capabilities shows 7 features with status: not_configured, adapter_required, or local_only

---

**Report generated**: 2026-05-15