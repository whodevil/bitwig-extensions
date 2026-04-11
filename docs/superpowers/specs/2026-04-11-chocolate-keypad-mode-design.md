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
- `DEVICE` -> `DEFAULT`: Show popup "Default Mode". If `DeviceState == BROWSING`, cancel the browser first. Leave panel as-is.

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
| `CursorDevice` | `cursorTrack.createCursorDevice("chocolate-device", "Cursor Device", 0, CursorDeviceFollowMode.FOLLOW_SELECTION)` | Tracks the selected device in the chain; `selectNext()`/`selectPrevious()` moves between devices; `afterDeviceInsertionPoint().browse()` opens browser at cursor position |
| `PopupBrowser` | `host.createPopupBrowser()` | `selectNextFile()`/`selectPreviousFile()` to scroll results, `commit()` to accept, `cancel()` to dismiss |

### markInterested() Calls

- `cursorDevice.name()` -- know which device the cursor is on
- `cursorDevice.hasNext()` -- boundary detection
- `cursorDevice.hasPrevious()` -- boundary detection
- `popupBrowser.exists()` -- detect external browser close for state consistency

### Insertion Point Strategy

When PC 10 is pressed in DEVICE/NAVIGATION:
- If the cursor device exists (non-empty chain): `cursorDevice.afterDeviceInsertionPoint().browse()`
- If the chain is empty: `trackBank.getItemAt(0).endOfDeviceChainInsertionPoint().browse()`

### CursorTrack

The module already creates a cursor track that the trackBank follows. The `CursorDevice` is created from this same cursor track.

## Error Handling & Edge Cases

**Browser state consistency:** Observe `popupBrowser.exists()` and register a callback. When it transitions to `false` while `deviceState == BROWSING`, automatically reset `deviceState` to `NAVIGATION`.

**Empty device chain:** When in DEVICE/NAVIGATION on an empty track, `cursorDevice` points to nothing. `selectNext()`/`selectPrevious()` are safe no-ops. For PC 10, fall back to `endOfDeviceChainInsertionPoint().browse()`.

**Cursor at chain boundaries:** `selectPrevious()` at the first device and `selectNext()` at the last device are safe no-ops in the Bitwig API.

**Mode toggle while browsing:** PC 12 (toggle keypad mode) while the browser is open calls `cancelBrowser()` before switching to DEFAULT.

**Footswitch/encoder mode toggles during device mode:** Orthogonal and unaffected.

## File Changes

All changes within the chocolate module. No changes to common or MPD modules.

### `ChocolateDefinition.kt`

- Create `Application`, `CursorDevice`, and `PopupBrowser` in `fetchHandler()`
- Add `markInterested()` calls for the new API objects
- Pass these three new objects into `ChocolateMidiHandler`

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
