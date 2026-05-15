
---

## Phase 2.0: Relay Server (NEW!)

A FastAPI relay server can provide some features without OpenHands Cloud APIs.

### Relay Server Features

| Feature | Can Provide | Notes |
|---------|-------------|-------|
| Sessions List | ⚠️ PARTIAL | Proxy to backend if configured |
| Skills Sync | ⚠️ PARTIAL | Proxy to backend if configured |
| Workflow Execute | ❌ ADAPTER | Not in relay |
| Capability Detection | ✅ REAL | Works without backend |
| MCP Tools | ⚠️ PARTIAL | Proxy if configured |

### Integration Plan

1. **Run relay locally**: `python main.py`
2. **Point Android to relay**: Set server URL to `http://localhost:8000` 
3. **Or proxy relay online**: Deploy relay to cloud, configure with real backend

### Relay Repository

```
/workspace/openhands-relay/
```

See relay README for setup.
