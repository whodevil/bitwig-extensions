# Toggle Record & Panel Layout Toggle Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add transport record toggle on PC 13 and arrange/mix panel layout toggle on PC 10 (default keypad mode) to the Chocolate module.

**Architecture:** Add two private functions and one boolean state field to `ChocolateMidiHandler`. Wire PC 13 as a new direct-dispatch case and update PC 10's existing `modeHandler` default lambda. No changes to API wiring or other files.

**Tech Stack:** Kotlin, Bitwig Extension API v20

**Spec:** `docs/superpowers/specs/2026-04-12-chocolate-default-keypad-pc13-pc10-design.md`

---

## Chunk 1: Implementation

This feature has no test framework -- verification is `./gradlew build` for compilation and manual testing in Bitwig Studio.

### Task 1: Add `arrangeLayout` state field

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt:52-55`

- [ ] **Step 1: Add the boolean field alongside existing state fields**

After the existing state fields (lines 52-55), add the new field. The state block becomes:

```kotlin
    private var footswitchMode: FootswitchMode = CLIP
    private var encoderMode: EncoderMode = VOLUME
    private var keypadMode: KeypadMode = DEFAULT
    private var deviceState: DeviceState = NAVIGATION
    private var arrangeLayout: Boolean = true
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (the field is unused so far, which is fine)

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "feat: add arrangeLayout state field for panel layout tracking"
```

### Task 2: Add `toggleRecord()` function and PC 13 dispatch

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt:65-126` (handleMessage), and add new function

- [ ] **Step 1: Add `toggleRecord()` private function**

Add the following function after `startHardStop()` (after line 384):

```kotlin
    private fun toggleRecord() {
        host.println("toggle record")
        transport.record()
    }
```

- [ ] **Step 2: Add PC 13 case to the `when(msg.data1)` dispatch**

In `handleMessage()`, add the PC 13 case. Place it near the other transport/clip actions (after PC 8 / before PC 21). The relevant section becomes:

```kotlin
            8 -> startHardStop()
            7 -> recordCLip()
            13 -> toggleRecord()
            21 -> deleteClip()
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "feat: add transport record toggle on PC 13"
```

### Task 3: Add `togglePanelLayout()` function and update PC 10 dispatch

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt:109-113` (PC 10 modeHandler), and add new function

- [ ] **Step 1: Add `togglePanelLayout()` private function**

Add the following function after `toggleRecord()`:

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

- [ ] **Step 2: Update PC 10 default-mode lambda**

Change the PC 10 `modeHandler` call from:

```kotlin
            10 -> modeHandler(
                {},
                ::openDeviceBrowser,
                ::commitBrowser
            )
```

To:

```kotlin
            10 -> modeHandler(
                ::togglePanelLayout,
                ::openDeviceBrowser,
                ::commitBrowser
            )
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "feat: add arrange/mix panel layout toggle on PC 10 in default mode"
```

### Task 4: Build and install extension

- [ ] **Step 1: Full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Install to Bitwig extensions directory**

Run: `./gradlew install`
Expected: Task completes successfully, `.bwextension` file copied to `$BITWIG_EXTENSIONS_LOCATION`

### Task 5: Manual verification in Bitwig Studio

Follow the verification checklist from the spec:

- [ ] **Step 1:** Press PC 13 when not recording -- verify the transport record button arms
- [ ] **Step 2:** Press PC 13 again -- verify the transport record button disarms
- [ ] **Step 3:** Start playback, press PC 13 -- verify recording begins into the arrangement
- [ ] **Step 4:** Press PC 13 during arrangement recording -- verify recording stops
- [ ] **Step 5:** In default keypad mode, press PC 10 -- verify view switches to Mix with "Mix View" popup
- [ ] **Step 6:** Press PC 10 again -- verify view switches to Arrange with "Arrange View" popup
- [ ] **Step 7:** Switch to device mode (PC 12), then back to default (PC 12) -- verify PC 10 still toggles correctly
- [ ] **Step 8:** Verify PC 10 in device/navigation mode still opens device browser
- [ ] **Step 9:** Verify PC 10 in device/browsing mode still commits browser selection
- [ ] **Step 10:** Verify all other PC mappings are unchanged
