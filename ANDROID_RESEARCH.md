# SECTION 1: RESEARCH + ARCHITECTURE

## Available APIs

### OpenHands Cloud API (V1)
Based on research from `/home/openhands/.openhands/cache/skills/public-skills/plugins/openhands/skills/openhands-api/SKILL.md`:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/users/me` | GET | Validate auth and inspect current account |
| `/api/v1/app-conversations/search` | GET | List recent conversations |
| `/api/v1/app-conversations` | POST | Start a new conversation |
| `/api/v1/app-conversations?ids=` | GET | Fetch conversation records by ID (batch) |
| `/api/v1/app-conversations/start-tasks` | GET | Check async start-task status |
| `/api/v1/conversation/{id}/events/search` | GET | Read conversation events |
| `/api/v1/sandboxes/search` | GET | List sandboxes |
| `/api/v1/sandboxes/{id}/pause` | POST | Pause sandbox |
| `/api/v1/sandboxes/{id}/resume` | POST | Resume sandbox |

**Base URL**: `https://app.all-hands.dev` (production)
**Auth**: Bearer token (`OPENHANDS_API_KEY` or `OPENHANDS_CLOUD_API_KEY`)

### OpenHands Automation API
Based on research from `/home/openhands/.openhands/cache/skills/public-skills/plugins/openhands/skills/openhands-automation/SKILL.md`:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/automation/v1/preset/prompt` | POST | Create automation from prompt (recommended) |
| `/api/automation/v1/preset/plugin` | POST | Create automation with plugins |
| `/api/automation/v1` | GET | List automations |
| `/api/automation/v1/{id}` | GET/PATCH/DELETE | Manage automation |
| `/api/automation/v1/{id}/dispatch` | POST | Trigger run manually |
| `/api/automation/v1/webhooks` | POST/GET | Register/manage custom webhooks |

### OpenHands Software Agent SDK
Based on research from `/home/openhands/.openhands/cache/skills/public-skills/plugins/openhands/skills/openhands-sdk/SKILL.md`:

- Core Classes: `Agent`, `Conversation`, `LLM`, `Tool`, `Skill`, `Workspace`
- Install: `pip install openhands-sdk openhands-tools`
- Full documentation at: https://docs.openhands.dev/sdk

### Git Provider APIs (Available for Extension)
- **GitHub**: Uses `GITHUB_TOKEN` - see `/home/openhands/.openhands/cache/skills/public-skills/skills/github/SKILL.md`
- **GitLab**: Uses `GITLAB_TOKEN` - see `/home/openhands/.openhands/cache/skills/public-skills/skills/gitlab/SKILL.md`
- **Bitbucket**: Uses `BITBUCKET_TOKEN` - see `/home/openhands/.openhands/cache/skills/public-skills/skills/bitbucket/SKILL.md`

---

## Android Integration Plan

### Environment Setup Required
1. **Java JDK** - Need to install (OpenJDK 17 recommended for Android)
2. **Android SDK** - Need to install via sdkmanager
3. **Gradle** - Will use Gradle Wrapper
4. **Kotlin** - Jetpack Compose language

### Architecture Pattern: MVVM + Clean Architecture
```
app/
├── data/
│   ├── local/          # DataStore, Room (if needed)
│   ├── remote/         # OpenHands API client
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use cases
├── presentation/
│   ├── ui/             # Compose screens
│   ├── viewmodel/      # ViewModels
│   └── navigation/     # Navigation setup
└── di/                 # Dependency injection
```

### Key Libraries
- **UI**: Jetpack Compose, Material 3
- **Navigation**: Compose Navigation
- **Networking**: Retrofit2 + OkHttp3 + Moshi
- **Async**: Kotlin Coroutines + Flow
- **DI**: Hilt
- **Storage**: DataStore Preferences
- **Background**: WorkManager
- **Notifications**: AndroidX Core + WorkManager

### API Client Strategy
- Use raw HTTP (OkHttp) for OpenHands Cloud API
- Minimal wrapper classes for REST endpoints
- Implement Bearer token auth
- Handle streaming responses

---

## Risks

| Risk | Severity | Mitigation |
|------|----------|-------------|
| No Java/Android SDK in environment | HIGH | Install via apt + sdkmanager |
| API rate limiting | MEDIUM | Implement exponential backoff |
| Token expiration | MEDIUM | Add token refresh flow |
| Large file handling | MEDIUM | Chunk uploads for files |
| Offline mode | LOW | Cache minimal state locally |
| Session timeout | MEDIUM | Auto-reconnect with status polling |

---

## Feature Matrix

| Feature | Section | Priority | Implementation |
|--------|---------|----------|----------------|
| Server URL + API Key input | S3 | P0 | Direct API call |
| Saved connection profiles | S3 | P0 | DataStore |
| Test connection | S3 | P0 | GET /users/me |
| Live connection status | S3 | P0 | Polling + WebSocket |
| Active sessions list | S4 | P0 | GET /app-conversations/search |
| Current agent/model | S4 | P1 | From conversation record |
| Workspace info | S4 | P1 | From sandbox record |
| Runtime status | S4 | P0 | Sandbox status |
| Recent tasks | S4 | P1 | GET /events |
| Logs stream | S4 | P1 | GET /events (polling) |
| Prompt editor | S5 | P0 | Text field + templates |
| Prompt templates | S5 | P1 | JSON file |
| Variable substitution | S5 | P1 | Regex replacement |
| Save/load prompts | S5 | P0 | DataStore |
| Send to OpenHands | S5 | P0 | POST /app-conversations |
| SKILL.md editor | S6 | P1 | Text editor |
| Trigger editor | S6 | P1 | Form input |
| Import/export skills | S6 | P2 | File picker |
| File picker | S7 | P0 | Android file picker |
| Share-sheet import | S7 | P1 | Intent filter |
| Preview files | S7 | P1 | WebView/Image |
| Attach to tasks | S7 | P0 | POST /file/upload |
| Export files | S7 | P2 | Save to Downloads |
| Notification channels | S8 | P0 | Android NotificationManager |
| Task completion alerts | S8 | P0 | WorkManager |
| Failure alerts | S8 | P1 | WorkManager |
| MCP config import | S9 | P1 | File picker |
| Tool enable/disable | S9 | P2 | Toggle UI |
| Node workflow builder | S10 | P2 | Custom canvas |
| GitHub panel | S11 | P1 | gh CLI / API |
| GitLab panel | S11 | P1 | API |
| Bitbucket panel | S11 | P1 | API |
| Issue-to-task | S11 | P2 | Convert issue to prompt |
| PR viewer | S11 | P1 | API |
| CI status | S11 | P2 | API |
| Sandbox status | S12 | P0 | GET /sandboxes |
| Command history | S12 | P1 | GET /events |
| File action history | S12 | P2 | GET /events |
| Diagnostics export | S12 | P2 | ZIP download |
| Dynamic JSON theme | S13 | P1 | JSON parser |
| Random theme generator | S13 | P2 | Random colors |
| Animated dashboard | S13 | P1 | Compose animations |

---

## Connection Profile Model

```kotlin
data class ConnectionProfile(
    val id: String,
    val name: String,
    val serverUrl: String,          // e.g., "https://app.all-hands.dev"
    val apiKey: String,             // Bearer token
    val isDefault: Boolean = false
)
```

## App Screen Structure

1. **HomeScreen** - Dashboard with connection status
2. **ConnectionScreen** - Server URL + API key input
3. **SessionsScreen** - List of active conversations
4. **PromptScreen** - Prompt editor and templates
5. **SkillsScreen** - Skill editor and management
6. **FilesScreen** - File hub and attachments
7. **SettingsScreen** - App settings and profiles
8. **SkillBuilderScreen** - Create custom skills
9. **ToolManagerScreen** - MCP and tool configs
10. **WorkflowScreen** - Agent canvas
11. **GitPanelScreen** - GitHub/GitLab/Bitbucket integration
12. **RuntimeScreen** - Sandbox monitoring