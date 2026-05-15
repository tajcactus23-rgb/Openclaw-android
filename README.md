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