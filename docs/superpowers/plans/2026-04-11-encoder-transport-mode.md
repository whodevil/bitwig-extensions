# Encoder Transport Mode Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a TRANSPORT encoder mode that moves the play start position by 1 beat per encoder click.

**Architecture:** Extend the existing `EncoderMode` enum with a third value. The mode toggle cycles three ways. The encoder dispatch branches on the new mode to call `transport.playStartPosition().inc()`. The transport extension function registers interest in the play start position.

**Tech Stack:** Kotlin, Bitwig Extension API v20

**Spec:** `docs/superpowers/specs/2026-04-11-encoder-transport-mode-design.md`

---

## Chunk 1: Implementation

This feature has no test framework — verification is `./gradlew build` for compilation and manual testing in Bitwig Studio.

### Task 1: Add TRANSPORT to EncoderMode enum

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt:23-26`

- [ ] **Step 1: Add TRANSPORT value to the enum**

Change the `EncoderMode` enum at lines 23-26 from:

```kotlin
enum class EncoderMode {
    VOLUME,
    SEND
}
```

To:

```kotlin
enum class EncoderMode {
    VOLUME,
    SEND,
    TRANSPORT
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL (the new enum value is unused so far, which is fine)

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "feat: add TRANSPORT value to EncoderMode enum"
```

### Task 2: Wire playStartPosition in ChocolateDefinition

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateDefinition.kt:89-93`

- [ ] **Step 1: Add markInterested call for playStartPosition**

Change the `ControllerHost.transport()` extension function at lines 89-93 from:

```kotlin
fun ControllerHost.transport(): Transport {
    val transport = createTransport()
    transport.isPlaying.markInterested()
    return transport
}
```

To:

```kotlin
fun ControllerHost.transport(): Transport {
    val transport = createTransport()
    transport.isPlaying.markInterested()
    transport.playStartPosition().markInterested()
    return transport
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateDefinition.kt
git commit -m "feat: register interest in transport playStartPosition"
```

### Task 3: Rewrite toggleEncoderMode to three-way cycle

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt:260-268`

- [ ] **Step 1: Replace if/else with when cycle**

Change `toggleEncoderMode()` at lines 260-268 from:

```kotlin
    private fun toggleEncoderMode() {
        if(encoderMode == VOLUME) {
            host.showPopupNotification("Send Mode")
            encoderMode = EncoderMode.SEND
        } else {
            host.showPopupNotification("Volume Mode")
            encoderMode = VOLUME
        }
    }
```

To:

```kotlin
    private fun toggleEncoderMode() {
        when (encoderMode) {
            EncoderMode.VOLUME -> {
                host.showPopupNotification("Send Mode")
                encoderMode = EncoderMode.SEND
            }
            EncoderMode.SEND -> {
                host.showPopupNotification("Transport Mode")
                encoderMode = EncoderMode.TRANSPORT
            }
            EncoderMode.TRANSPORT -> {
                host.showPopupNotification("Volume Mode")
                encoderMode = EncoderMode.VOLUME
            }
        }
    }
```

- [ ] **Step 2: Verify it compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "feat: change encoder mode toggle to three-way cycle"
```

### Task 4: Add TRANSPORT branch to encoder dispatch

**Files:**
- Modify: `src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt:141-155`

- [ ] **Step 1: Rewrite encoderCounterClockwise with when**

Change `encoderCounterClockwise()` at lines 141-147 from:

```kotlin
    private fun encoderCounterClockwise() {
        if(encoderMode == VOLUME) {
            volumeDown()
        } else {
            trackBank.getItemAt(0).sendBank().getItemAt(0).inc(-.03)
        }
    }
```

To:

```kotlin
    private fun encoderCounterClockwise() {
        when (encoderMode) {
            EncoderMode.VOLUME -> volumeDown()
            EncoderMode.SEND -> trackBank.getItemAt(0).sendBank().getItemAt(0).inc(-.03)
            EncoderMode.TRANSPORT -> transport.playStartPosition().inc(-1.0)
        }
    }
```

- [ ] **Step 2: Rewrite encoderClockwise with when**

Change `encoderClockwise()` at lines 149-155 from:

```kotlin
    private fun encoderClockwise() {
        if(encoderMode == VOLUME) {
            volumeUp()
        } else {
            trackBank.getItemAt(0).sendBank().getItemAt(0).inc(.03)
        }
    }
```

To:

```kotlin
    private fun encoderClockwise() {
        when (encoderMode) {
            EncoderMode.VOLUME -> volumeUp()
            EncoderMode.SEND -> trackBank.getItemAt(0).sendBank().getItemAt(0).inc(.03)
            EncoderMode.TRANSPORT -> transport.playStartPosition().inc(1.0)
        }
    }
```

- [ ] **Step 3: Verify it compiles**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add src/main/kotlin/offthecob/chocolate/ChocolateMidiHandler.kt
git commit -m "feat: add transport mode encoder dispatch for play position"
```

### Task 5: Build and install extension

- [ ] **Step 1: Full build**

Run: `./gradlew build`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Install to Bitwig extensions directory**

Run: `./gradlew install`
Expected: Task completes successfully, `.bwextension` file copied to `$BITWIG_EXTENSIONS_LOCATION`

### Task 6: Manual verification in Bitwig Studio

Follow the verification checklist from the spec:

- [ ] **Step 1:** Toggle through all 3 encoder modes via PC 25 and verify popups: "Send Mode", "Transport Mode", "Volume Mode"
- [ ] **Step 2:** In TRANSPORT mode, turn encoder CW and verify the play start position marker advances by 1 beat
- [ ] **Step 3:** In TRANSPORT mode, turn encoder CCW and verify the marker moves back by 1 beat
- [ ] **Step 4:** Turn encoder CCW at beat 0 and verify it clamps (no crash, no negative position)
- [ ] **Step 5:** Nudge position during playback and verify it updates the marker without disrupting playback
- [ ] **Step 6:** Verify VOLUME and SEND modes still work identically to before
