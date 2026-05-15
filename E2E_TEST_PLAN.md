# Phase 2.5: E2E Test Plan

## Overview

This document defines end-to-end test procedures for validating the full Android + relay stack outside the build environment.

**Test Environment**: Physical Android device or emulator connecting to relay running on same network or localhost.

**Prerequisites**:
- Debug APK installed on device/emulator
- Relay server running and accessible
- Network connectivity between device and relay

---

## Test Categories

### Category A: Connection Flow

| # | Test | Expected Result | Pass/Fail |
|---|------|-----------------|----------|
| A1 | App launches | Splash → Dashboard | - |
| A2 | Enter relay URL | URL saved | - |
| A3 | Enter API key | Key saved | - |
| A4 | Test connection | Success indicator | - |
| A5 | Return to dashboard | Shows connected | - |

### Category B: Capabilities Discovery

| # | Test | Expected Result | Pass/Fail |
|---|------|-----------------|----------|
| B1 | Open Capabilities | Real sessions shown OR PLACEHOLDER | - |
| B2 | Check MCP tools | Tools list loads OR PLACEHOLDER | - |
| B3 | Check skills | Skills list loads OR PLACEHOLDER | - |

### Category C: Runtime SSE

| # | Test | Expected Result | Pass/Fail |
|---|------|-----------------|----------|
| C1 | Open Runtime | Stream state visible | - |
| C2 | Connection quality shown | Quality 0-100% | - |
| C3 | Live queue updates | Queue shows updates | - |
| C4 | Live session changes | Session list updates | - |
| C5 | Disconnect network | Shows disconnected | - |
| C6 | Reconnect | Shows reconnecting → connected | - |

### Category D: Workflow Execution

| # | Test | Expected Result | Pass/Fail |
|---|------|-----------------|----------|
| D1 | Open Canvas | Nodes visible | - |
| D2 | Create prompt node | Node added | - |
| D3 | Execute workflow | Execution starts | - |
| D4 | See live logs | Logs appear | - |
| D5 | Cancel execution | Execution stops | - |
| D6 | Retry execution | Execution restarts | - |

### Category E: Network Resilience

| # | Test | Expected Result | Pass/Fail |
|---|------|-----------------|----------|
| E1 | Kill relay process | App shows error | - |
| E2 | Restart relay | App reconnects | - |
| E3 | Toggle WiFi off/on | Reconnects | - |
| E4 | App backgrounded | SSE maintained | - |
| E5 | App resumed | Stream alive | - |

---

## Test Data

### Relay URL Formats

| Environment | URL |
|--------------|-----|
| Localhost (emulator) | http://10.0.2.2:8000 |
| Localhost (physical) | http://localhost:8000 |
| LAN IP | http://192.168.x.x:8000 |

### Test Credentials

```
OpenHands API Key: test-key-for-e2e (if using test relay)
```

---

## Execution

### Manual Test Steps

1. Install APK on device/emulator
2. Start relay server
3. Open app
4. Navigate to Connection screen
5. Enter relay URL
6. Enter API key (if required)
7. Test connection
8. Navigate through each screen
9. Execute test cases above
10. Record PASS/FAIL for each

### Automated Test Commands

```bash
# Start relay
cd /workspace/openhands-relay && uv run uvicorn main:app --host 0.0.0.0 --port 8000

# Test API endpoints
curl http://localhost:8000/api/v1/health
curl http://localhost:8000/api/v1/runtime/sessions
curl http://localhost:8000/api/v1/runtime/queue
curl http://localhost:8000/api/v1/runtime/events

# Test from device (if network allows)
adb shell am start -n com.openhands.android/com.openhands.android.MainActivity
```

---

## Acceptance Criteria

### Must Pass (Critical)

- [ ] A1: App launches
- [ ] A2-A5: Connection flow
- [ ] C1: Runtime screen shows stream state

### Should Pass (Important)

- [ ] C2-C4: SSE updates
- [ ] D1-D3: Canvas workflow execution

### Nice to Have

- [ ] E1-E5: Network resilience
- [ ] B1-B3: Capabilities discovery

---

## Notes

- Some tests will show PLACEHOLDER or ADAPTER_REQUIRED when relay unavailable
- This is expected behavior - mark as PASS if correct behavior displayed
- SSE tests require real relay connection
- Canvas execution requires active session

---

**Date**: 2026-05-15
**Phase**: 2.5