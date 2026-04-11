# Chocolate Module: Encoder Transport Mode

**Date:** 2026-04-11
**Module:** `offthecob.chocolate`
**Branch:** `encoder-transport-control`

## Summary

Add a third encoder mode (`TRANSPORT`) to the Chocolate module's existing `EncoderMode` enum. When active, turning the right encoder moves the play start position forward or backward by 1 beat per click. The mode toggle (PC 25) changes from a two-way toggle to a three-way round-robin cycle: VOLUME -> SEND -> TRANSPORT -> VOLUME.

## Encoder Mode Changes

### Enum

`EncoderMode` gains a third value:

| Value | Encoder CW (PC 40) | Encoder CCW (PC 30) |
|---|---|---|
| `VOLUME` (default) | `volumeUp()` | `volumeDown()` |
| `SEND` | `trackBank.getItemAt(0).sendBank().getItemAt(0).inc(.03)` | `trackBank.getItemAt(0).sendBank().getItemAt(0).inc(-.03)` |
| `TRANSPORT` | `transport.playStartPosition().inc(1.0)` | `transport.playStartPosition().inc(-1.0)` |

The `inc` value of `1.0` on a `SettableBeatTimeValue` represents 1 beat. This is a fixed step size regardless of time signature.

### Mode Cycling (PC 25)

The current `toggleEncoderMode()` is a two-way if/else toggle. It becomes a three-way cycle:

```
VOLUME --[PC 25]--> SEND    (popup: "Send Mode")
SEND   --[PC 25]--> TRANSPORT (popup: "Transport Mode")
TRANSPORT --[PC 25]--> VOLUME (popup: "Volume Mode")
```

### Encoder Dispatch

`encoderClockwise()` and `encoderCounterClockwise()` change from `if/else` to a `when` expression with three branches. The VOLUME and SEND branches are unchanged. The TRANSPORT branch calls `transport.playStartPosition().inc(+/-1.0)`.

The encoder nudges the play start position regardless of whether playback is running.

## Bitwig API Wiring

### Existing Objects

The `Transport` object is already created in `ChocolateDefinition.kt` via the `ControllerHost.transport()` extension function and passed to `ChocolateMidiHandler`. No new constructor parameters are needed.

### New markInterested() Call

The `transport()` extension function currently only marks `isPlaying`:

```kotlin
fun ControllerHost.transport(): Transport {
    val transport = createTransport()
    transport.isPlaying.markInterested()
    return transport
}
```

Add `transport.playStartPosition().markInterested()` so Bitwig allows reading and writing the play start position:

```kotlin
fun ControllerHost.transport(): Transport {
    val transport = createTransport()
    transport.isPlaying.markInterested()
    transport.playStartPosition().markInterested()
    return transport
}
```

No other API objects, observers, or wiring changes are required.

## Error Handling & Edge Cases

**Play start position at song start:** Calling `inc(-1.0)` when the position is at or near beat 0 is safe. Bitwig clamps the position to 0 rather than going negative.

**During playback:** The play start position is independent of the playback cursor. Moving it during playback changes where playback will resume from next time the user hits play, which is standard Bitwig behavior.

**No visual feedback beyond Bitwig defaults:** Moving the play start position causes Bitwig to update the arranger marker natively. No additional popup or notification is shown when nudging — only when switching modes.

## File Changes

All changes within the chocolate module. No changes to common, MPD, or other modules. No new files.

### `ChocolateMidiHandler.kt`

- Add `TRANSPORT` to the `EncoderMode` enum
- Rewrite `toggleEncoderMode()` from two-way if/else to three-way `when` cycle
- Rewrite `encoderClockwise()` from if/else to `when` with three branches, adding TRANSPORT branch
- Rewrite `encoderCounterClockwise()` from if/else to `when` with three branches, adding TRANSPORT branch

### `ChocolateDefinition.kt`

- Add `transport.playStartPosition().markInterested()` in the `ControllerHost.transport()` extension function

## Verification

Manual testing in Bitwig Studio:

1. Toggle through all 3 encoder modes via PC 25 and verify popups: "Send Mode", "Transport Mode", "Volume Mode"
2. In TRANSPORT mode, turn encoder CW and verify the play start position marker advances by 1 beat
3. In TRANSPORT mode, turn encoder CCW and verify the marker moves back by 1 beat
4. Turn encoder CCW at beat 0 and verify it clamps (no crash, no negative position)
5. Nudge position during playback and verify it updates the marker without disrupting playback
6. Verify VOLUME and SEND modes still work identically to before
