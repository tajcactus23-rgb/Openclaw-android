# Phase 2.5: LAN Setup Guide

## Overview

Guide for running OpenHands Android + relay on LAN for end-to-end testing.

---

## Architecture

```
[Android Device] <---HTTP---> [Relay Server] <---API---> [OpenHands Cloud]
                                    |
                              (localhost or
                               LAN IP)
```

---

## Step 1: Build APK

```bash
# Already built at:
ls -la /workspace/project/app/build/outputs/apk/debug/app-debug.apk

# Copy to accessible location
cp /workspace/project/app/build/outputs/apk/debug/app-debug.apk /workspace/E2E-android-debug.apk
ls -la /workspace/E2E-android-debug.apk
```

---

## Step 2: Start Relay Server

### Option A: Localhost (Development)

```bash
cd /workspace/openhands-relay

# Start on localhost
uv run uvicorn main:app --host 127.0.0.1 --port 8080

# Verify
curl http://127.0.0.1:8080/
```

### Option B: LAN (External Testing)

```bash
cd /workspace/openhands-relay

# Get LAN IP
hostname -I | awk '{print $1}'
# Example: 192.168.1.100

# Start on all interfaces
uv run uvicorn main:app --host 0.0.0.0 --port 8080

# Verify from another machine
curl http://192.168.1.100:8080/

# Verify from device (if network allows)
# See firewall notes below
```

---

## Step 3: Install on Device/Emulator

### Option A: Emulator (Android Studio)

```bash
# Start emulator
emulator -avd <avd_name>

# Install APK
adb install /workspace/E2E-android-debug.apk

# If already installed, reinstall
adb install -r /workspace/E2E-android-debug.apk
```

### Option B: Emulator with Adb Reverse (Recommended)

```bash
# Start emulator
emulator -avd <avd_name>

# Set up port forwarding (device sees localhost:8080)
adb reverse tcp:8080 tcp:8080

# Install APK
adb install /workspace/E2E-android-debug.apk
```

**How it works**: `adb reverse` makes device's port 8080 map to host's port 8080.

**Use URL**: `http://localhost:8080` in the app

### Option C: Physical Device via USB

```bash
# Enable developer mode on device
# Settings > About Phone > Build Number (tap 7 times)
# Settings > Developer Options > USB Debugging

# Connect device via USB
adb devices
# Should show: <device_id> device

# Install APK
adb install /workspace/E2E-android-debug.apk
```

**Use URL**: `http://localhost:8080` or `http://127.0.0.1:8080`

### Option D: Physical Device via LAN

```bash
# Enable developer mode on device
# Settings > About Phone > Build Number (tap 7 times)
# Settings > Developer Options > USB Debugging > Allow ADB over network

# Find device IP
adb shell ip addr show wlan0

# Connect to device via network
adb connect <device_ip>:5555
# Example: adb connect 192.168.1.100:5555

# Install APK
adb connect 192.168.1.100:5555
adb install /workspace/E2E-android-debug.apk
```

**Firewall Note**: Ensure device can reach host IP. If blocked, either:
- Disable firewall: `sudo ufw disable`
- Open port: `sudo ufw allow 8080/tcp`

### Option E: WiFi Transfer (No USB)

```bash
# Send APK via WiFi
# On device: Settings > About Phone > Share > WiFi

# Or use local network file sharing
# Host: python3 -m http.server 8080
# Device: Browser to http://<host_ip>:8080

# Download and install
```

---

## Step 4: Configure App

### Enter Relay URL

| Device Type | URL to Enter |
|-------------|--------------|
| Emulator (adb reverse) | `http://localhost:8080` |
| Emulator (no reverse) | `http://10.0.2.2:8080` |
| Physical (USB) | `http://localhost:8080` |
| Physical (LAN) | `http://192.168.1.x:8080` |

### Test Sequence

1. Open app → Connection screen
2. Enter URL (see table above)
3. Enter API key (optional for local relay)
4. Tap "Test Connection"
5. Should show "Connected" indicator
6. Navigate to Dashboard

---

## Step 5: Run Test Cases

See `E2E_TEST_PLAN.md` for detailed test cases.

### Quick Validation

```bash
# 1. Relay is running?
curl http://localhost:8080/api/v1/health

# 2. Runtime API works?
curl http://localhost:8080/api/v1/runtime/sessions
curl http://localhost:8080/api/v1/runtime/queue

# 3. SSE works? (note: won't show in curl, but endpoint exists)
curl -I http://localhost:8080/api/v1/runtime/events

# 4. From device, test connectivity
adb shell curl http://10.0.2.2:8080/api/v1/health
```

---

## Troubleshooting

### Issue: Cannot connect to relay

**Check**:
1. Relay is running: `curl http://localhost:8080/`
2. Firewall allows: `sudo ufw status`
3. Correct URL entered in app
4. Device can reach host: `adb shell ping <host_ip>`

### Issue: APK won't install

**Check**:
1. Unknown sources enabled: Settings > Security > Unknown Sources
2. Device has space: `df -h`
3. Previous install clean: `adb uninstall com.openhands.android`

### Issue: SSE stream not working

**Check**:
1. Relay version supports SSE: `main.py` has `/events` endpoint
2. Network allows long-lived connections
3. No corporate proxy blocking

### Issue: Connection drops

**Check**:
1. WiFi stable
2. No sleep mode blocking
3. Android battery optimization disabled
4. See E2E_TEST_PLAN.md Category E

---

## Exact Commands Summary

### Host Setup

```bash
# Build and copy APK
cp /workspace/project/app/build/outputs/apk/debug/app-debug.apk /workspace/E2E-android-debug.apk

# Start relay
cd /workspace/openhands-relay
uv run uvicorn main:app --host 0.0.0.0 --port 8080
```

### Emulator Install

```bash
# With adb reverse (recommended)
emulator -avd <avd_name>
adb reverse tcp:8080 tcp:8080
adb install -r /workspace/E2E-android-debug.apk
```

### Physical Device Install

```bash
# Via USB first, then network
adb usb
adb tcpip 5555
adb connect <device_ip>:5555
adb install -r /workspace/E2E-android-debug.apk
```

### App Configuration

```
URL: http://localhost:8080 (emulator)
URL: http://<your_lan_ip>:8080 (physical on LAN)
API Key: <optional>
```

---

## Security Notes

- Debug APK is for testing only
- Not signed for Play Store
- API key stored in SharedPreferences (unencrypted)
- LAN connection not encrypted (use VPN for production)
- Disable USB debugging after testing

---

**Date**: 2026-05-15
**Phase**: 2.5