# OpenHands Android App

Mobile companion app for OpenHands Cloud - control your AI agent sessions on Android.

## Features

### Section 4: Mobile Dashboard
- Runtime connection status
- Active sessions display
- Recent tasks
- Logs with level filtering

### Section 5: Prompt Builder
- Template system (Code Review, Debug, Explain, Tests)
- Variable interpolation ({file}, {repo}, {issue}, {pr})
- Save/Load prompts

### Section 6: Skill Builder
- SKILL.md editor
- Trigger phrase editor
- Validation

### Section 7: File Hub
- File browser
- Attach to tasks
- Export

### Section 15+: Swarm Mode
- Multi-instance orchestration
- Task distribution
- Instance management

### Section 16: Connection Suite
- OpenHands, GitHub, GitLab, Bitbucket
- MCP servers
- Custom webhooks

### Section 17: Workspace Windows
- Terminal
- Web Preview
- Screen Viewer

### Section 19: OpenClaw
- OpenClaw API compatibility

## Build

```bash
# Requires Java 21 and Android SDK
./gradlew assembleDebug
```

## APK Location

`app/build/outputs/apk/debug/app-debug.apk`

## Architecture

- Clean Architecture (Data/Domain/Presentation)
- MVVM with Hilt DI
- Jetpack Compose UI
- Material 3

## Screens

| Screen | Section | Status |
|--------|---------|--------|
| Dashboard | 4 | Working |
| Sessions | 4 | Working |
| Prompt | 5 | Working |
| Skills | 6 | Working |
| Files | 7 | Working |
| Swarm | 15 | Working |
| Terminal | 17 | Working |
| Web Preview | 17 | Working |
| Screen Viewer | 18 | Working |
| Connection Suite | 16 | Working |
| OpenClaw | 19 | Working |

## API Integration

- OpenHands Cloud V1 API
- Connection profiles with secure storage
- Real-time status

## Capability-First UI

All features display capability states:
- ✅ supported
- 🟡 partially supported  
- 🔴 unsupported
- 🔵 adapter required
---

## How to Build

```bash
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

---

## Connect to OpenHands

1. Install APK on Android device
2. Go to **Connection** screen
3. Enter your OpenHands server URL (e.g., `https://app.all-hands.dev`)
4. Enter your API key/token
5. Tap **Test Connection** - should show "Connected"

---

## Working Screens

| Screen | Status |
|--------|--------|
| Connection | Working |
| Dashboard | Partial (sessions API missing) |
| Prompt | Working |
| Skills | Local Only (cloud sync missing) |
| Files | Working |
| Notifications | Partial (Android 13+ permission) |
| Git | Working (local only) |
| Canvas | Local Only |
| Theme | Working |
| Tool Manager | Working (local) |

## Placeholder Screens

These show "PLACEHOLDER" or "ADAPTER_REQUIRED" badges:

- Sessions - No API
- WebView - Needs adapter
- Settings - Basic UI
- Swarm - Untested

---

## Missing APIs

- `/api/sessions` - Active sessions
- `/api/skills/sync` - Cloud sync
- `/api/workflows/{id}/execute` - Workflow run

---

## Testing Checklist

- [ ] Add and test connection profile
- [ ] Dashboard shows connection status
- [ ] Create, save, run prompt
- [ ] Skills CRUD works, cloud sync shows disabled
- [ ] File picker works
- [ ] Notifications test
- [ ] Git local detection works
- [ ] Canvas workflow save/load
- [ ] Theme export/import

---

## Tech Stack

- Kotlin 1.9.x
- Jetpack Compose
- Hilt (DI)
- DataStore
- OkHttp + Moshi
- Material3

---

## Documentation

| Document | Description |
|----------|-------------|
| [CHANGELOG.md](./CHANGELOG.md) | Release notes, feature list |
| [KNOWN_LIMITATIONS.md](./KNOWN_LIMITATIONS.md) | API gaps, what's stubbed |
| [SECURITY.md](./SECURITY.md) | Token handling, permissions |
| [FINAL_AUDIT.md](./FINAL_AUDIT.md) | Full section completion, status |

---

## Version

**1.0.0** - Pre-alpha development build

---

## Status

**DEVELOPMENT BUILD - NOT PRODUCTION READY**

See [FINAL_AUDIT.md](./FINAL_AUDIT.md) for complete status.

---

## Relay Server Integration

The Android APK can connect to either:

1. **Direct Mode** (default): Connect to OpenHands Cloud at `https://app.all-hands.dev`
2. **Relay Mode**: Connect to local relay server

### Relay Mode Setup

1. **Start relay server**:
```bash
cd /workspace/openhands-relay
uv run python main.py
```

2. **On Android emulator**:
- Server URL: `http://10.0.2.2:8000` (emulator localhost)
- Profile Type: Select **Relay**

3. **On real device**:
- Use LAN IP: `http://<YOUR-LAN-IP>:8000`
- Profile Type: Select **Relay**

### Capabilities (Relay Mode)

| Feature | Status |
|---------|--------|
| Sessions list | Works if backend configured |
| Skills list | Works if backend configured |
| MCP tools | Works if backend configured |
| Capability detection | ✅ Works |
| Workflow execution | ADAPTER_REQUIRED |

### No Fake Data

- Empty lists stay empty (no faked sessions/skills/tools)
- If relay returns empty, Android shows real empty state
- ADAPTER_REQUIRED shown exactly when relay returns adapter_required
