# Changelog

## [1.0.0] - 2024-XX-XX

### Added

- **Connection Screen**: Profile management with test connection
- **Dashboard**: Connection status display (sessions API pending)
- **Prompt Editor**: CRUD, templates, variables, run to OpenHands
- **Skills Screen**: SKILL.md editor, local CRUD, export/import
- **Files Screen**: Android file picker, preview, attach to tasks
- **Notifications**: Android notification channels, test notification
- **Git Screen**: Local git repo detection, branch/commit history
- **Runtime Monitor**: Network test, device info, diagnostics export
- **Agent Canvas**: Node-based workflow builder, save/load JSON
- **Theme**: JSON theme engine, import/export, live apply
- **Tool Manager**: MCP config import, server list, connection testing

### Partial Features (Local Only)

- Skills cloud sync: `LOCAL_ONLY` badge (API pending)
- Workflow execution: ADAPTER_REQUIRED (API pending)
- Scheduled tasks: ADAPTER_REQUIRED (API pending)

### Placeholder Screens

- Sessions: PLACEHOLDER badge (no `/api/sessions` endpoint)
- WebView: ADAPTER badge needed
- Settings: Basic UI

### Unknown/Untested

- Swarm Mode
- Terminal
- Screen Viewer
- OpenClaw
- Connection Suite

---

## Architecture

- Kotlin 1.9.x
- Jetpack Compose (BOM 2024.02.00)
- Hilt (DI)
- DataStore (persistence)
- OkHttp + Moshi (networking)
- Material 3

---

## Build Instructions

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing config)
./gradlew assembleRelease
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Known APIs Missing

1. `GET /api/v1/sessions` - Active sessions list
2. `POST /api/v1/skills/sync` - Skills cloud sync
3. `POST /api/v1/workflows/{id}/run` - Workflow execution
4. `POST /api/v1/tasks/scheduled` - Scheduled automation
5. `GET/POST /api/v1/mcp/servers` - MCP remote management
