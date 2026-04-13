# Chocolate Module: Default Keypad Mode -- Toggle Record (PC 13) & Panel Layout Toggle (PC 10)

**Date:** 2026-04-12
**Module:** `offthecob.chocolate`
**Branch:** `toggle-record`

## Summary

Add two new behaviors to the Chocolate module's default keypad mode. PC 13 toggles transport recording (arranger record). PC 10, which is currently a no-op in default mode, toggles between the Arrange and Mix panel layouts. Neither change affects behavior in Device keypad mode.

## PC 13: Toggle Transport Record

### Dispatch

Add a new case in the `handleMessage` `when` block:

```kotlin
13 -> toggleRecord()
```

This is a direct dispatch -- not gated by `modeHandler` -- so it fires regardless of keypad mode.

### Handler Function

```kotlin
private fun toggleRecord() {
    host.println("toggle record")
    transport.record()
}
```

`Transport.record()` toggles the transport record button in Bitwig. When armed, recording begins on the next play; when already recording, it stops recording. This is the arranger record toggle, distinct from the clip recording on PC 7 (`recordCLip()`).

### Bitwig API Wiring

No changes needed. The `Transport` object is already created and passed to `ChocolateMidiHandler`. `Transport.record()` is a fire-and-forget action that does not require any `markInterested()` calls since we are not reading state.

## PC 10: Toggle Arrange/Mix Panel Layout (Default Mode Only)

### Dispatch

The PC 10 entry already uses `modeHandler` with three lambdas. The default-mode lambda changes from a no-op to `::togglePanelLayout`:

**Before:**
```kotlin
10 -> modeHandler(
    {},
    ::openDeviceBrowser,
    ::commitBrowser
)
```

**After:**
```kotlin
10 -> modeHandler(
    ::togglePanelLayout,
    ::openDeviceBrowser,
    ::commitBrowser
)
```

The Device/Navigation lambda (`openDeviceBrowser`) and Device/Browsing lambda (`commitBrowser`) are unchanged.

### State

A new `Boolean` field tracks the current layout assumption:

```kotlin
private var arrangeLayout: Boolean = true
```

Initialized to `true`, assuming the session starts in Arrange view. This is internal state only -- the Bitwig API does not expose the current panel layout for reading.

### Handler Function

```kotlin
private fun togglePanelLayout() {
    if (arrangeLayout) {
        host.showPopupNotification("Mix View")
        application.setPanelLayout("MIX")
    } else {
        host.showPopupNotification("Arrange View")
        application.setPanelLayout("ARRANGE")
    }
    arrangeLayout = !arrangeLayout
}
```

Each press flips between Arrange and Mix, showing a popup notification for feedback.

### Bitwig API Wiring

No changes needed. The `Application` object is already created in `ChocolateDefinition.kt` and passed to `ChocolateMidiHandler`. `setPanelLayout()` is already used by `toggleKeypadMode()` to set `"EDIT"` when entering device mode.

## Error Handling & Edge Cases

**Sync with manual layout changes:** If the user changes the panel layout manually in Bitwig (or via `toggleKeypadMode` entering device mode with `"EDIT"`), the `arrangeLayout` boolean may be out of sync. The next PC 10 press will set the layout based on internal state, which may not match what the user sees. This is an accepted limitation since Bitwig does not provide a readable panel layout property.

**Device mode interaction:** Entering device mode (PC 12) sets the layout to `"EDIT"`. Exiting device mode does **not** auto-restore the Arrange/Mix layout; the user presses PC 10 to switch back. After exiting, PC 10 resumes toggling based on `arrangeLayout`'s internal state. If the user was in Mix view before entering device mode, `arrangeLayout` still reflects that -- the first press after exiting device mode will flip to the other layout as expected from the internal state's perspective.

**Repeated presses:** Each press deterministically flips the layout. No debounce or guard needed -- `setPanelLayout` is idempotent for repeated calls with the same value, and rapid toggling is harmless.

## File Changes

All changes within `ChocolateMidiHandler.kt`. No changes to `ChocolateDefinition.kt`, common module, or any other files. No new files, no new dependencies.

### `ChocolateMidiHandler.kt`

1. Add `private var arrangeLayout: Boolean = true` alongside existing state fields
2. Add `13 -> toggleRecord()` case to the `when(msg.data1)` block
3. Change PC 10's default-mode lambda from `{}` to `::togglePanelLayout`
4. Add `toggleRecord()` private function
5. Add `togglePanelLayout()` private function

## Verification

Manual testing in Bitwig Studio:

1. Press PC 13 when not recording -- verify the transport record button arms (lights up)
2. Press PC 13 again -- verify the transport record button disarms
3. Start playback, press PC 13 -- verify recording begins into the arrangement
4. Press PC 13 during arrangement recording -- verify recording stops
5. In default keypad mode, press PC 10 -- verify view switches to Mix with "Mix View" popup
6. Press PC 10 again -- verify view switches to Arrange with "Arrange View" popup
7. Switch to device mode (PC 12), then back to default (PC 12) -- verify PC 10 still toggles correctly
8. Verify PC 10 in device/navigation mode still opens device browser
9. Verify PC 10 in device/browsing mode still commits browser selection
10. Verify all other PC mappings are unchanged
