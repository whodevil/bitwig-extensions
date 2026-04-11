# Chocolate Module: Keypad Mode & Mode Rework

**Date:** 2026-04-11
**Module:** `offthecob.chocolate`
**Branch:** `device-browser`

## Summary

Rework the Chocolate module's mode system to support three orthogonal mode axes: footswitch mode (renamed from ChocolateMode), encoder mode (unchanged), and a new keypad mode. Keypad mode adds a "device" mode that repurposes the Super16v2 arrow keys and browse button for device chain navigation and device browser interaction.

## Mode Taxonomy

Three independent mode axes, each toggled by a dedicated key:

| Axis | Enum | States | Toggle | Scope |
|---|---|---|---|---|
| **Footswitch** | `FootswitchMode` | `CLIP` (default), `SCENE` | PC 11 | Foot pedal buttons only (PC 0-3) |
| **Encoder** | `EncoderMode` | `VOLUME` (default), `SEND` | PC 25 | Right encoder only (PC 30/40) |
| **Keypad** | `KeypadMode` | `DEFAULT` (default), `DEVICE` | PC 12 | Arrow keys (PC 4/5/6/9), browse (PC 10), cancel (PC 14) |

### Device Mode Sub-state

Within `KeypadMode.DEVICE`, a sub-state tracks whether the device browser popup is open:

| Sub-state | Enum | States | Transitions |
|---|---|---|---|
| **Device state** | `DeviceState` | `NAVIGATION`, `BROWSING` | PC 10 opens browser (NAV->BROWSING), PC 10 commits (BROWSING->NAV), PC 14 cancels (BROWSING->NAV) |

`DeviceState` is only meaningful when `KeypadMode == DEVICE`. When keypad mode toggles back to `DEFAULT`, `DeviceState` resets to `NAVIGATION` (canceling any open browser).

## State Transitions

### Keypad Mode Toggle (PC 12)

- `DEFAULT` -> `DEVICE`: Show popup "Device Mode", open device panel via `Application.setPanelLayout("EDIT")`, set `DeviceState = NAVIGATION`.
- `DEVICE` -> `DEFAULT`: Show popup "Default Mode". If `DeviceState == BROWSING`, cancel the browser first. Panel layout is not restored on exit — the user may have changed it manually while in device mode.

### Device State Transitions

```
NAVIGATION --[PC 10 (browse)]--> BROWSING
    Opens browser at the current insertion point

BROWSING --[PC 10 (browse)]--> NAVIGATION
    Commits the selection (PopupBrowser.commit())

BROWSING --[PC 14 (cancel)]--> NAVIGATION
    Cancels the browser (PopupBrowser.cancel())
```

## Key Dispatch

### Mode-dependent keys

| PC | DEFAULT | DEVICE / NAVIGATION | DEVICE / BROWSING |
|---|---|---|---|
| 9 (up) | Track up (`trackBank.scrollBackwards()`) | Track up (unchanged) | Scroll browser previous (`popupBrowser.selectPreviousFile()`) |
| 5 (down) | Track down (`trackBank.scrollForwards()`) | Track down (unchanged) | Scroll browser next (`popupBrowser.selectNextFile()`) |
| 4 (left) | Clip slot left (`clipLauncherSlotBank.scrollBackwards()`) | Move insertion cursor backward (`cursorDevice.selectPrevious()`) | No-op |
| 6 (right) | Clip slot right (`clipLauncherSlotBank.scrollForwards()`) | Move insertion cursor forward (`cursorDevice.selectNext()`) | No-op |
| 10 (browse) | No-op | Open browser at cursor's insertion point | Commit selection |
| 14 | No-op (unassigned) | No-op | Cancel browser |

**Note — PC 10 behavior change:** PC 10 currently opens the device browser unconditionally. In the new design, it becomes a no-op in DEFAULT mode and is only active in DEVICE mode. This is intentional: device browsing is now scoped exclusively to device mode.

**Note — Track navigation in device mode:** Up/down (PC 9/5) remain as track navigation in DEVICE/NAVIGATION. This is intentional — it allows switching tracks to view different device chains without leaving device mode.

### Mode-independent keys (unchanged across all keypad modes)

- Row 0: Arm (PC 15), Solo (PC 16), Mute (PC 17), Activate (PC 18)
- Row 1: Footswitch mode toggle (PC 11), Keypad mode toggle (PC 12)
- Row 2: Record clip (PC 7), Hard stop (PC 8)
- Fn layer: Encoder mode toggle (PC 25), Delete clip (PC 21), Send scroll (PC 20/23)
- Encoder: Volume/Send (PC 30/40)
- Foot pedals: PC 0-3 (governed by FootswitchMode, not KeypadMode)

## Bitwig API Wiring

### New API Objects

| Object | Creation | Purpose |
|---|---|---|
| `Application` | `host.createApplication()` | `setPanelLayout("EDIT")` when entering device mode |
| `PinnableCursorDevice` | `cursorTrack.createCursorDevice("chocolate-device", "Cursor Device", 0, CursorDeviceFollowMode.FOLLOW_SELECTION)` | Tracks the selected device in the chain; `selectNext()`/`selectPrevious()` moves between devices; `afterDeviceInsertionPoint().browse()` opens browser at cursor position. The return type is `PinnableCursorDevice` (extends `CursorDevice`); pinning is not used but the type is what the API returns. |
| `PopupBrowser` | `host.createPopupBrowser()` | `selectNextFile()`/`selectPreviousFile()` to scroll results, `commit()` to accept, `cancel()` to dismiss |

### markInterested() and Observer Registration

All value objects below require `.markInterested()` at init time:

- `cursorDevice.name().markInterested()` -- know which device the cursor is on
- `cursorDevice.hasNext().markInterested()` -- boundary detection
- `cursorDevice.hasPrevious().markInterested()` -- boundary detection
- `popupBrowser.exists().markInterested()` -- detect external browser close

Additionally, `popupBrowser.exists()` requires a value observer for state recovery (see Error Handling). Register during init:

```kotlin
popupBrowser.exists().addValueObserver { exists ->
    if (!exists && deviceState == DeviceState.BROWSING) {
        deviceState = DeviceState.NAVIGATION
    }
}
```

This observer handles the case where the user closes the browser via Bitwig's GUI rather than the keypad. Registration should happen in the `ChocolateMidiHandler` init block or constructor.

### Insertion Point Strategy

When PC 10 is pressed in DEVICE/NAVIGATION:
- If the cursor device exists (non-empty chain): `cursorDevice.afterDeviceInsertionPoint().browse()`
- If the chain is empty: `trackBank.getItemAt(0).endOfDeviceChainInsertionPoint().browse()`

### CursorTrack Refactoring

The module already creates a cursor track inside the `TrackBank.init()` extension function, but it is currently a local variable scoped to that function. The `CursorDevice` must be created from this cursor track, so `TrackBank.init()` needs to be refactored: extract the `host.createCursorTrack()` call into `fetchHandler()` so the cursor track is accessible for both `trackBank.followCursorTrack()` and `cursorTrack.createCursorDevice()`. Pass the cursor track into the modified `init()` function as a parameter.

**Note on Bitwig API Javadoc:** The `Application` interface has swapped Javadoc comments on `PANEL_LAYOUT_MIX` and `PANEL_LAYOUT_EDIT`. Despite this, `"EDIT"` is the correct string value for the detail/device panel.

## Error Handling & Edge Cases

**Browser state consistency:** Observe `popupBrowser.exists()` and register a callback. When it transitions to `false` while `deviceState == BROWSING`, automatically reset `deviceState` to `NAVIGATION`.

**Empty device chain:** When in DEVICE/NAVIGATION on an empty track, `cursorDevice` points to nothing. `selectNext()`/`selectPrevious()` are safe no-ops. For PC 10, fall back to `endOfDeviceChainInsertionPoint().browse()`.

**Cursor at chain boundaries:** `selectPrevious()` at the first device and `selectNext()` at the last device are safe no-ops in the Bitwig API.

**Mode toggle while browsing:** PC 12 (toggle keypad mode) while the browser is open calls `cancelBrowser()` before switching to DEFAULT.

**Footswitch/encoder mode toggles during device mode:** Orthogonal and unaffected.

## File Changes

All changes within the chocolate module. No changes to common or MPD modules.

### `ChocolateDefinition.kt`

- Refactor `TrackBank.init()`: extract `host.createCursorTrack()` into `fetchHandler()` so the cursor track is accessible; pass it as a parameter to a modified `init()` function
- Create `Application` via `host.createApplication()`
- Create `PinnableCursorDevice` via `cursorTrack.createCursorDevice(...)` 
- Create `PopupBrowser` via `host.createPopupBrowser()`
- Add `markInterested()` calls and observer registration for the new API objects
- Pass `application`, `cursorDevice`, and `popupBrowser` into `ChocolateMidiHandler`

### `ChocolateMidiHandler.kt`

- Rename `ChocolateMode` enum to `FootswitchMode` (values stay `CLIP`/`SCENE`)
- Rename the `mode` field to `footswitchMode`
- Add `KeypadMode` enum (`DEFAULT`/`DEVICE`)
- Add `DeviceState` enum (`NAVIGATION`/`BROWSING`)
- Add fields: `keypadMode`, `deviceState`
- Add constructor parameters: `application`, `cursorDevice`, `popupBrowser`
- Rename `toggleMode()` to `toggleFootswitchMode()`
- Add `toggleKeypadMode()` -- handles PC 12, resets `deviceState` to NAVIGATION on exit, cancels browser if BROWSING
- Rewrite the `when` dispatch for PC 4, 5, 6, 9, 10, 14 to route through `keypadMode`/`deviceState`
- Add methods: `moveInsertionCursorBack()`, `moveInsertionCursorForward()`, `openDeviceBrowser()`, `commitBrowser()`, `cancelBrowser()`, `scrollBrowserUp()`, `scrollBrowserDown()`

### `README.md`

- Update the mode table to reflect three axes (footswitch, encoder, keypad)
- Update the key layout diagrams to show PC 12 as "Keypad Mode Toggle" and PC 14 as "Cancel (in browser)"
- Document device mode behavior

No new files are created.
