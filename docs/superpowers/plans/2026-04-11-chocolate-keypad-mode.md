# Chocolate Keypad Mode Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a keypad mode axis to the Chocolate module that lets users navigate and browse devices in Bitwig's device chain using the Super16v2 macropad.

**Architecture:** Nested state machine with three orthogonal mode axes (footswitch, encoder, keypad). Keypad mode has a DEVICE state with a BROWSING sub-state. New Bitwig API objects (Application, PinnableCursorDevice, PopupBrowser) are created at init and passed to the handler.

**Tech Stack:** Kotlin, Bitwig Extension API v20, Gradle

**Spec:** `docs/superpowers/specs/2026-04-11-chocolate-keypad-mode-design.md`

**Testing:** No unit test framework in this project. The Bitwig Extension API requires a running DAW instance. Verification is: `./gradlew build` compiles without errors, then manual testing in Bitwig.

---

## Chunk 1: Rename and Restructure Modes

### Task 1: Rename ChocolateMode to FootswitchMode

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt`

- [ ] **Step 1: Rename the enum**

Replace the `ChocolateMode` enum with `FootswitchMode`. Values stay the same.

```kotlin
// Before
enum class ChocolateMode() {
    SCENE,
    CLIP,
}

// After
enum class FootswitchMode {
    SCENE,
    CLIP,
}
```

- [ ] **Step 2: Rename the field and all references**

In `ChocolateMidiHandler`:
- Rename `var mode: ChocolateMode = CLIP` to `var footswitchMode: FootswitchMode = FootswitchMode.CLIP`
- Update imports: `CLIP` and `SCENE` now come from `FootswitchMode`
- Update `toggleMode()` to `toggleFootswitchMode()` — replace `mode` with `footswitchMode` in the body
- Update all `when(mode)` expressions in `d()`, `c()`, `b()`, `a()` to `when(footswitchMode)`
- Update the `handleMessage` dispatch: `11 -> toggleFootswitchMode()`

- [ ] **Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "rename ChocolateMode to FootswitchMode"
```

### Task 2: Add KeypadMode and DeviceState enums

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt`

- [ ] **Step 1: Add the new enums**

Add after the `EncoderMode` enum:

```kotlin
enum class KeypadMode {
    DEFAULT,
    DEVICE,
}

enum class DeviceState {
    NAVIGATION,
    BROWSING,
}
```

- [ ] **Step 2: Add fields to ChocolateMidiHandler**

```kotlin
var keypadMode: KeypadMode = KeypadMode.DEFAULT
var deviceState: DeviceState = DeviceState.NAVIGATION
```

- [ ] **Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "add KeypadMode and DeviceState enums"
```

## Chunk 2: API Wiring in ChocolateDefinition

### Task 3: Refactor cursorTrack out of TrackBank.init()

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateDefinition.kt`

- [ ] **Step 1: Extract cursorTrack creation**

Currently `TrackBank.init()` is an extension function that creates the cursor track as a local variable. Refactor so the cursor track is created in `fetchHandler()` and passed into `init()`.

Change the `TrackBank.init()` extension function signature to accept a `CursorTrack` parameter:

```kotlin
// Before
fun TrackBank.init(host: ControllerHost): ClipLauncherSlotBank {
    val cursorTrack = host.createCursorTrack("chocolate", "Cursor Track", 0, 1, true)
    followCursorTrack(cursorTrack)
    // ... rest unchanged

// After
fun TrackBank.init(cursorTrack: CursorTrack): ClipLauncherSlotBank {
    followCursorTrack(cursorTrack)
    // ... rest unchanged (remove the createCursorTrack line)
```

In `fetchHandler()`, create the cursor track before the trackBank init call:

```kotlin
val cursorTrack = host.createCursorTrack("chocolate", "Cursor Track", 0, 1, true)
val trackBank = host.createTrackBank(1, 1, 1)
val clipLauncherSlotBank = trackBank.init(cursorTrack)
```

- [ ] **Step 2: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateDefinition.kt
git commit -m "refactor: extract cursorTrack from TrackBank.init()"
```

### Task 4: Create API objects and wire both definition and handler

Both files must be updated together to keep the build compiling — the definition passes new objects to the handler, so the handler must accept them in the same commit.

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateDefinition.kt`
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt`

- [ ] **Step 1: Add imports to ChocolateMidiHandler.kt**

```kotlin
import com.bitwig.extension.controller.api.Application
import com.bitwig.extension.controller.api.PinnableCursorDevice
import com.bitwig.extension.controller.api.PopupBrowser
```

- [ ] **Step 2: Add constructor parameters to ChocolateMidiHandler**

Add three new parameters to the `ChocolateMidiHandler` class:

```kotlin
class ChocolateMidiHandler(
    private val host: ControllerHost,
    private val transport: Transport,
    private val sceneBank: SceneBank,
    private val trackBank: TrackBank,
    private val clipLauncherSlotBank: ClipLauncherSlotBank,
    private val application: Application,
    private val cursorDevice: PinnableCursorDevice,
    private val popupBrowser: PopupBrowser
) : MidiHandler {
```

- [ ] **Step 3: Add browser state observer in an init block**

Add an `init` block to `ChocolateMidiHandler` that registers the popup browser observer:

```kotlin
init {
    popupBrowser.exists().addValueObserver { exists ->
        if (!exists && deviceState == DeviceState.BROWSING) {
            deviceState = DeviceState.NAVIGATION
        }
    }
}
```

- [ ] **Step 4: Create the new API objects in ChocolateDefinition.kt fetchHandler()**

After creating `cursorTrack` and before constructing `ChocolateMidiHandler`, add:

```kotlin
val application = host.createApplication()

val cursorDevice = cursorTrack.createCursorDevice(
    "chocolate-device", "Cursor Device", 0,
    CursorDeviceFollowMode.FOLLOW_SELECTION
)
cursorDevice.exists().markInterested()
cursorDevice.name().markInterested()
cursorDevice.hasNext().markInterested()
cursorDevice.hasPrevious().markInterested()

val popupBrowser = host.createPopupBrowser()
popupBrowser.exists().markInterested()
```

Note: `CursorDeviceFollowMode` is already available via the existing `import com.bitwig.extension.controller.api.*` wildcard import in ChocolateDefinition.kt.

- [ ] **Step 5: Pass new objects to ChocolateMidiHandler**

Update the ChocolateMidiHandler constructor call to include the three new parameters:

```kotlin
return ChocolateMidiHandler(
    host,
    transport,
    sceneBank,
    trackBank,
    clipLauncherSlotBank,
    application,
    cursorDevice,
    popupBrowser
)
```

- [ ] **Step 6: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateDefinition.kt src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "wire Application, CursorDevice, PopupBrowser in definition and handler"
```

## Chunk 3: Keypad Mode Toggle, Device Methods, and Key Dispatch

### Task 5: Add keypad mode toggle method

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt`

- [ ] **Step 1: Add toggleKeypadMode()**

```kotlin
private fun toggleKeypadMode() {
    if (keypadMode == KeypadMode.DEFAULT) {
        host.showPopupNotification("Device Mode")
        application.setPanelLayout("EDIT")
        keypadMode = KeypadMode.DEVICE
        deviceState = DeviceState.NAVIGATION
    } else {
        if (deviceState == DeviceState.BROWSING) {
            popupBrowser.cancel()
        }
        host.showPopupNotification("Default Mode")
        keypadMode = KeypadMode.DEFAULT
        deviceState = DeviceState.NAVIGATION
    }
}
```

- [ ] **Step 2: Wire PC 12 in handleMessage**

In the `when (msg.data1)` block, change:

```kotlin
// Before: 12 is not handled (falls through)
// After:
12 -> toggleKeypadMode()
```

- [ ] **Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "add keypad mode toggle on PC 12"
```

### Task 6: Add device mode action methods

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt`

- [ ] **Step 1: Add device navigation methods**

```kotlin
private fun moveInsertionCursorForward() {
    cursorDevice.selectNext()
}

private fun moveInsertionCursorBack() {
    cursorDevice.selectPrevious()
}
```

- [ ] **Step 2: Add browser interaction methods**

```kotlin
private fun openDeviceBrowser() {
    if (cursorDevice.exists().get()) {
        cursorDevice.afterDeviceInsertionPoint().browse()
    } else {
        trackBank.getItemAt(0).endOfDeviceChainInsertionPoint().browse()
    }
    deviceState = DeviceState.BROWSING
}

private fun commitBrowser() {
    popupBrowser.commit()
    deviceState = DeviceState.NAVIGATION
}

private fun cancelBrowser() {
    popupBrowser.cancel()
    deviceState = DeviceState.NAVIGATION
}

private fun scrollBrowserUp() {
    popupBrowser.selectPreviousFile()
}

private fun scrollBrowserDown() {
    popupBrowser.selectNextFile()
}
```

- [ ] **Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "add device mode action methods"
```

### Task 7: Rewire key dispatch for mode-dependent keys

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt`

- [ ] **Step 1: Replace the existing direct dispatch for PC 4, 5, 6, 9, 10, 14**

In the `when (msg.data1)` block, replace the dispatch for these six PC numbers with mode-aware dispatch. **Note:** these entries are NOT contiguous in the source file — PC 8 (`startHardStop`), PC 7 (`recordCLip`), and PC 21 (`deleteClip`) are interleaved between them. Replace each entry individually.

```kotlin
// Before (scattered across the when block):
// 10 -> insertDevice()
// 9 -> scrollClipUp()
// 6 -> scrollClipForward()
// 5 -> scrollClipDown()
// 4 -> scrollClipBack()
// (14 was not handled)

// After:
4 -> when (keypadMode) {
    KeypadMode.DEFAULT -> scrollClipBack()
    KeypadMode.DEVICE -> when (deviceState) {
        DeviceState.NAVIGATION -> moveInsertionCursorBack()
        DeviceState.BROWSING -> {} // no-op
    }
}
5 -> when (keypadMode) {
    KeypadMode.DEFAULT -> scrollClipDown()
    KeypadMode.DEVICE -> when (deviceState) {
        DeviceState.NAVIGATION -> scrollClipDown()
        DeviceState.BROWSING -> scrollBrowserDown()
    }
}
6 -> when (keypadMode) {
    KeypadMode.DEFAULT -> scrollClipForward()
    KeypadMode.DEVICE -> when (deviceState) {
        DeviceState.NAVIGATION -> moveInsertionCursorForward()
        DeviceState.BROWSING -> {} // no-op
    }
}
9 -> when (keypadMode) {
    KeypadMode.DEFAULT -> scrollClipUp()
    KeypadMode.DEVICE -> when (deviceState) {
        DeviceState.NAVIGATION -> scrollClipUp()
        DeviceState.BROWSING -> scrollBrowserUp()
    }
}
10 -> when (keypadMode) {
    KeypadMode.DEFAULT -> {} // no-op
    KeypadMode.DEVICE -> when (deviceState) {
        DeviceState.NAVIGATION -> openDeviceBrowser()
        DeviceState.BROWSING -> commitBrowser()
    }
}
14 -> when (keypadMode) {
    KeypadMode.DEFAULT -> {} // no-op
    KeypadMode.DEVICE -> when (deviceState) {
        DeviceState.NAVIGATION -> {} // no-op
        DeviceState.BROWSING -> cancelBrowser()
    }
}
```

- [ ] **Step 2: Remove the old `insertDevice()` method**

The `insertDevice()` private method is no longer called. Remove it.

- [ ] **Step 3: Build to verify**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "rewire arrow/browse key dispatch for keypad mode"
```

## Chunk 4: README Update

### Task 8: Update README documentation

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Update the mode table**

Replace the existing mode table (under `#### Modes`) with:

```markdown
#### Modes

Three independent mode axes control context-sensitive behaviour:

| Mode axis | States | Toggle |
|---|---|---|
| **FootswitchMode** | `CLIP` (default) / `SCENE` | PC 11 (Super16v2 Row 1 / Col 0) |
| **EncoderMode** | `VOLUME` (default) / `SEND` | PC 25 (Super16v2 Fn + Row 1 / Col 0) |
| **KeypadMode** | `DEFAULT` (default) / `DEVICE` | PC 12 (Super16v2 Row 1 / Col 1) |

The active mode name is shown as a Bitwig popup notification on each toggle.

##### Device Mode

When keypad mode is set to `DEVICE`:
- The device/edit panel opens automatically
- Left/right keys (PC 4/6) move a cursor through the device chain's insertion points
- Up/down keys (PC 9/5) continue to navigate tracks
- Browse (PC 10) opens the device browser popup at the current insertion point
- While the browser is open, up/down scroll through available devices
- Browse (PC 10) commits the selection; PC 14 cancels
- If the browser is closed externally (via Bitwig UI), the extension returns to navigation state
```

- [ ] **Step 2: Update the Layer 0 key layout diagram**

In the Layer 0 ASCII diagram, update row 1 col 1 and row 1 col 3:

```
    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
    │  FOOTSW   │  │  KEYPAD   │  │           │  │  CANCEL   │
    │  MODE     │  │  MODE     │  │     —     │  │ (browser) │  row 1
    │  PC 11    │  │  PC 12    │  │  PC 13    │  │  PC 14    │
    └───────────┘  └───────────┘  └───────────┘  └───────────┘
```

- [ ] **Step 3: Update the Layer 0 table**

In the `Super16v2 — Layer 0 (Base)` table, update:

```markdown
| **Row 1** | **Footswitch mode toggle** (PC 11) | **Keypad mode toggle** (PC 12) | — (PC 13) | **Cancel browser** (PC 14) |
```

- [ ] **Step 4: Update the row 2 table to reflect PC 10 change**

```markdown
| **Row 2** | Record clip (PC 7) | Hard stop (PC 8) | Track ↑ (PC 9) | Browse devices¹ (PC 10) |
```

Add a footnote:
```markdown
> ¹ **Browse devices** is only active in Device keypad mode. In Default keypad mode, PC 10 is a no-op.
```

- [ ] **Step 5: Build to verify nothing broke**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add README.md
git commit -m "docs: update README for keypad mode and footswitch rename"
```
