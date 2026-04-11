Bitwig MPD 24 script extension
=============================

Early days. I want to integrate my workflow closer with the mpd,
and I'd  also like to learn the bitwig api.

Experiments abound.

---

### Chocolate — Super16v2 + Devo FootCtrl Bluetooth

The `chocolate` extension connects a **1upkeyboards Super16v2** macropad and a **Devo FootCtrl
Bluetooth** foot pedal to the Bitwig cursor track via MIDI in 2. It always follows the selected
cursor track, observing one track, one scene, and one clip launcher slot at a time.

All MIDI messages are **Program Change** on a runtime-configurable channel. PC numbers map directly
to the `data1` byte, so no offset translation is needed.

```
  LAYER 0 — BASE
  ╌╌╌╌╌╌╌╌╌╌╌╌╌╌

    ┌──────┐                                           ┌──────┐
    │      │ ← OS volume (no MIDI)       MIDI ────►   │      │ CW  = vol/send −3%
    │ ENC0 │                                           │ ENC1 │ CCW = vol/send +3%
    └──────┘                                           └──────┘

    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
    │  ARM      │  │  SOLO     │  │  MUTE     │  │ ACTIVATE  │
    │           │  │           │  │           │  │           │  row 0
    │  PC 15    │  │  PC 16    │  │  PC 17    │  │  PC 18    │
    └───────────┘  └───────────┘  └───────────┘  └───────────┘
    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
    │  MODE     │  │           │  │           │  │           │
    │  TOGGLE   │  │     —     │  │     —     │  │     —     │  row 1
    │  PC 11    │  │  PC 12    │  │  PC 13    │  │  PC 14    │
    └───────────┘  └───────────┘  └───────────┘  └───────────┘
    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
    │  REC      │  │  HARD     │  │  TRACK ↑  │  │  BROWSE   │
    │  CLIP     │  │  STOP     │  │           │  │  DEVICES  │  row 2
    │  PC 7     │  │  PC 8     │  │  PC 9     │  │  PC 10    │
    └───────────┘  └───────────┘  └───────────┘  └───────────┘
    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
    │  [FN]     │  │  CLIP     │  │  TRACK ↓  │  │  CLIP     │
    │  hold     │  │  SLOT ←   │  │           │  │  SLOT →   │  row 3
    │           │  │  PC 4     │  │  PC 5     │  │  PC 6     │
    └───────────┘  └───────────┘  └───────────┘  └───────────┘


  LAYER 1 — Fn (hold bottom-left)
  ╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌

    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
    │  KC_ESC   │  │ RGB hue + │  │ RGB sat + │  │ RGB val + │
    │           │  │           │  │           │  │           │  row 0
    │           │  │           │  │           │  │           │
    └───────────┘  └───────────┘  └───────────┘  └───────────┘
    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
    │  ENC MODE │  │ RGB hue − │  │ RGB sat − │  │ RGB val − │
    │  TOGGLE   │  │           │  │           │  │           │  row 1
    │  PC 25    │  │           │  │           │  │           │
    └───────────┘  └───────────┘  └───────────┘  └───────────┘
    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
    │  DELETE   │  │           │  │  SEND     │  │           │
    │  CLIP     │  │     —     │  │  BANK ←   │  │     —     │  row 2
    │  PC 21    │  │  PC 22    │  │  PC 23    │  │  PC 24    │
    └───────────┘  └───────────┘  └───────────┘  └───────────┘
    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
    │  [FN]     │  │           │  │  SEND     │  │ QK_BOOT   │
    │  (held)   │  │     —     │  │  BANK →   │  │           │  row 3
    │           │  │  PC 19    │  │  PC 20    │  │           │
    └───────────┘  └───────────┘  └───────────┘  └───────────┘


  DEVO FOOTCTRL BLUETOOTH
  ╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌╌

    ┌───────────┐  ┌───────────┐  ┌───────────┐  ┌───────────┐
    │     a     │  │     b     │  │     c     │  │     d     │
    │   PC 0    │  │   PC 1    │  │   PC 2    │  │   PC 3    │
    └───────────┘  └───────────┘  └───────────┘  └───────────┘

    CLIP mode:    prev scene    next scene    rec/stop      start/stop
    SCENE mode:   scene ←       scene →       play scene    hard stop
```

#### Modes

Two independent mode axes control context-sensitive behaviour:

| Mode axis | States | Toggle |
|---|---|---|
| **ChocolateMode** | `CLIP` (default) / `SCENE` | PC 11 (Super16v2 Row 1 / Col 0) |
| **EncoderMode** | `VOLUME` (default) / `SEND` | PC 25 (Super16v2 Fn + Row 1 / Col 0) |

The active mode name is shown as a Bitwig popup notification on each toggle.

#### Super16v2 — Layer 0 (Base)

| | Col 0 | Col 1 | Col 2 | Col 3 |
|---|---|---|---|---|
| **Row 0** | Arm (PC 15) | Solo (PC 16) | Mute (PC 17) | Activate (PC 18) |
| **Row 1** | **Mode toggle** (PC 11) | — (PC 12) | — (PC 13) | — (PC 14) |
| **Row 2** | Record clip (PC 7) | Hard stop (PC 8) | Track ↑ (PC 9) | Browse devices (PC 10) |
| **Row 3** | `[Fn hold]` | Clip slot ← (PC 4) | Track ↓ (PC 5) | Clip slot → (PC 6) |

#### Super16v2 — Layer 1 (Fn held, bottom-left)

| | Col 0 | Col 1 | Col 2 | Col 3 |
|---|---|---|---|---|
| **Row 0** | KC_ESC | RGB hue+ | RGB sat+ | RGB val+ |
| **Row 1** | **Enc mode toggle** (PC 25) | RGB hue− | RGB sat− | RGB val− |
| **Row 2** | Delete clip (PC 21) | — (PC 22) | Send ← (PC 23) | — (PC 24) |
| **Row 3** | `[trns]` | — (PC 19) | Send → (PC 20) | QK_BOOT |

#### Super16v2 — Right encoder (encoder 1)

| Direction | VOLUME mode | SEND mode |
|---|---|---|
| CW → (PC 30) | Volume −3% | Send 0 −3% |
| ← CCW (PC 40) | Volume +3% | Send 0 +3% |

The left encoder (encoder 0) controls OS volume directly and sends no MIDI.

#### Devo FootCtrl Bluetooth — foot pedal (PC 0–3)

These four buttons are mode-aware:

| Button | PC | CLIP mode | SCENE mode |
|---|---|---|---|
| a | 0 | Prev scene (scroll back + play) | Scroll scene back |
| b | 1 | Next scene (scroll forward + play) | Scroll scene forward |
| c | 2 | Record / stop recording clip | Play current scene |
| d | 3 | Start / stop transport | Hard stop¹ |

> ¹ **Hard stop**: if transport is playing → stop transport and stop all scenes; if stopped → start
> transport. Plain **start/stop** omits the scene stop.

---

### Developing
Dependencies
- Java 21 or newer
- Set the environment variable `BITWIG_EXTENSIONS_LOCATION` to the location bitwig expects the
extensions to be installed. On Windows, by default, this is `~/Documents/Bitwig\ Studio/Extensions`.

All the extensions ship in a single package, so hitting the install target will put the package where it needs to go
to be picked up by Bitwig.
- `./gradlew install`
