# Known Limitations

## API Limitations

These OpenHands Cloud API endpoints are not available:

| Endpoint | Status | Notes |
|----------|--------|-------|
| `GET /api/v1/sessions` | NOT_AVAILABLE | Sessions list stub |
| `POST /api/v1/skills/sync` | NOT_AVAILABLE | Local-only skills |
| `POST /api/v1/workflows/{id}/execute` | NOT_AVAILABLE | Canvas workflows saved locally only |
| `POST /api/v1/tasks/scheduled` | NOT_AVAILABLE | WorkManager placeholder |
| `GET /api/v1/mcp/servers` | NOT_AVAILABLE | MCP remote placeholder |

## Local-Only Features

Some features work offline but cannot sync to cloud:

1. **Skills**: Create/edit/delete locally, no cloud sync
2. **Workflows**: Build/save/load locally, no execution
3. **Git**: Local repo detection only, no remote push/pull

## Android Limitations

- **Notifications**: Android 13+ requires runtime permission
- **WebView**: Not connected to real backend (shows ADAPTER badge)
- **File Picker**: Limited to internal storage scope

## Untested Features

- Swarm Mode (multi-agent orchestration)
- Terminal screen (basic shell stub)
- Screen Viewer (mock screenshots)
- OpenClaw (untested)
- Connection Suite (untested)

---

## Missing Functionality

### Not Implemented

- Real session data display (Dashboard sessions)
- Cloud skills sync
- Workflow execution via API
- Scheduled automation execution
- MCP server management
- Remote git operations
- Real-time logs streaming

---

## Testing Gaps

- No unit tests for Compose UI screens
- No integration tests for API calls
- No E2E tests
- No instrumented tests on physical device

---

## Documentation Gaps

- No in-app help tooltips
- No user guide
- No video tutorials
- No API reference

---

## Release Readiness

**Status**: PRE-ALPHA

This app is NOT production-ready:

- Debug APK only (no release signing)
- No manual device testing completed
- No real OpenHands server testing completed
- Many features stubbed/placeholder

See FINAL_AUDIT.md for complete status.
