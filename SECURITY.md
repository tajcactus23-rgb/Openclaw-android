# Security

## API Tokens

API keys are stored in:
- **DataStore**: Encrypted preferences storage
- **Memory**: Held in `OpenHandsApi` singleton

**Security Notes**:
- Tokens are sent with every API request via `Authorization: Bearer` header
- Tokens are never logged or displayed in UI (masked in connection status)
- Tokens persist across app restarts (stored in DataStore)

## WebView Safety

The WebView screen shows:
- **ADAPTER badge** - Not connected to any backend
- **No JavaScript interface** exposed
- **Local-only** - No network requests

**Safety**: WebView is a placeholder, safe to use.

## Screen Viewer

The Screen Viewer:
- Currently shows **mock screenshots**
- No real screen capture
- Will require **runtime permission** (READ_EXTERNAL_STORAGE)
- Will require **consent** before capture

**Status**: Stubbed/placeholder only.

## Adapter-Required Features

Features marked ADAPTER_REQUIRED or PLACEHOLDER:
- Do NOT make API calls
- Show clear error messages
- Are NOT functional

See `KNOWN_LIMITATIONS.md` for which features are stubbed.

## Data Storage

- **Profile data**: DataStore (Android encrypted preferences)
- **Skills/workflows**: Internal app storage
- **Files**: Android scoped storage only

**No data leaves the device** except:
- API calls to configured OpenHands server
- When user explicitly runs a prompt

## Permissions

Required permissions:
- `INTERNET` - For API calls
- `POST_NOTIFICATIONS` - Android 13+ (runtime)
- `READ_EXTERNAL_STORAGE` - File picker (Android < 13)
- `READ_MEDIA_*` - File picker (Android 13+)

---

## Reporting Security Issues

Do NOT report security issues in this repo.
This is a development build.
