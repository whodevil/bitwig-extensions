# Super16 v2 — whodevil Keymap Analysis

**Board:** `1upkeyboards/super16v2`
**Keymap:** `whodevil`
**Source:** `keyboards/1upkeyboards/super16v2/keymaps/whodevil/keymap.c`

All MIDI events are `Program Change` messages sent via `midi_send_programchange` on `midi_config.channel` (runtime-configurable on the keyboard).

---

## Layer 0 — Base

Physical positions (row × col) → Program Change number:

| | Col 0 | Col 1 | Col 2 | Col 3 |
|---|---|---|---|---|
| **Row 0** | PC 15 | PC 16 | PC 17 | PC 18 |
| **Row 1** | PC 11 | PC 12 | PC 13 | PC 14 |
| **Row 2** | PC 7 | PC 8 | PC 9 | PC 10 |
| **Row 3** | MO(1) | PC 4 | PC 5 | PC 6 |

## Layer 1 — Fn (hold Row 3 / Col 0)

| | Col 0 | Col 1 | Col 2 | Col 3 |
|---|---|---|---|---|
| **Row 0** | KC_ESC | RGB hue+ | RGB sat+ | RGB val+ |
| **Row 1** | PC 25 | RGB hue- | RGB sat- | RGB val- |
| **Row 2** | PC 21 | PC 22 | PC 23 | PC 24 |
| **Row 3** | (trns) | PC 19 | PC 20 | QK_BOOT |

## Encoders

| Encoder | CW | CCW |
|---|---|---|
| 0 (left) | Volume Up (no MIDI) | Volume Down (no MIDI) |
| 1 (right) | PC 30 | PC 40 |

## Static LED Colors

| LED index | R | G | B |
|---|---|---|---|
| 1, 2, 3 | 255 | 0 | 0 (Red) |
| 9, 12, 13, 14 | 0 | 0 | 255 (Blue) |

---

## Computer-Parsable Layout (JSON)

```json
{
  "keyboard": "1upkeyboards/super16v2",
  "keymap": "whodevil",
  "midi": {
    "event_type": "program_change",
    "channel": "midi_config.channel"
  },
  "grid": {
    "rows": 4,
    "cols": 4
  },
  "layers": [
    {
      "index": 0,
      "name": "Base",
      "keys": [
        { "key_index": 0,  "row": 0, "col": 0, "midi": { "type": "program_change", "program": 15 } },
        { "key_index": 1,  "row": 0, "col": 1, "midi": { "type": "program_change", "program": 16 } },
        { "key_index": 2,  "row": 0, "col": 2, "midi": { "type": "program_change", "program": 17 } },
        { "key_index": 3,  "row": 0, "col": 3, "midi": { "type": "program_change", "program": 18 } },
        { "key_index": 4,  "row": 1, "col": 0, "midi": { "type": "program_change", "program": 11 } },
        { "key_index": 5,  "row": 1, "col": 1, "midi": { "type": "program_change", "program": 12 } },
        { "key_index": 6,  "row": 1, "col": 2, "midi": { "type": "program_change", "program": 13 } },
        { "key_index": 7,  "row": 1, "col": 3, "midi": { "type": "program_change", "program": 14 } },
        { "key_index": 8,  "row": 2, "col": 0, "midi": { "type": "program_change", "program": 7  } },
        { "key_index": 9,  "row": 2, "col": 1, "midi": { "type": "program_change", "program": 8  } },
        { "key_index": 10, "row": 2, "col": 2, "midi": { "type": "program_change", "program": 9  } },
        { "key_index": 11, "row": 2, "col": 3, "midi": { "type": "program_change", "program": 10 } },
        { "key_index": 12, "row": 3, "col": 0, "midi": null, "action": "layer_momentary", "layer": 1 },
        { "key_index": 13, "row": 3, "col": 1, "midi": { "type": "program_change", "program": 4  } },
        { "key_index": 14, "row": 3, "col": 2, "midi": { "type": "program_change", "program": 5  } },
        { "key_index": 15, "row": 3, "col": 3, "midi": { "type": "program_change", "program": 6  } }
      ]
    },
    {
      "index": 1,
      "name": "Fn",
      "keys": [
        { "key_index": 0,  "row": 0, "col": 0, "midi": null, "action": "KC_ESC" },
        { "key_index": 1,  "row": 0, "col": 1, "midi": null, "action": "RM_HUEU" },
        { "key_index": 2,  "row": 0, "col": 2, "midi": null, "action": "RM_SATU" },
        { "key_index": 3,  "row": 0, "col": 3, "midi": null, "action": "RM_VALU" },
        { "key_index": 4,  "row": 1, "col": 0, "midi": { "type": "program_change", "program": 25 } },
        { "key_index": 5,  "row": 1, "col": 1, "midi": null, "action": "RM_HUED" },
        { "key_index": 6,  "row": 1, "col": 2, "midi": null, "action": "RM_SATD" },
        { "key_index": 7,  "row": 1, "col": 3, "midi": null, "action": "RM_VALD" },
        { "key_index": 8,  "row": 2, "col": 0, "midi": { "type": "program_change", "program": 21 } },
        { "key_index": 9,  "row": 2, "col": 1, "midi": { "type": "program_change", "program": 22 } },
        { "key_index": 10, "row": 2, "col": 2, "midi": { "type": "program_change", "program": 23 } },
        { "key_index": 11, "row": 2, "col": 3, "midi": { "type": "program_change", "program": 24 } },
        { "key_index": 12, "row": 3, "col": 0, "midi": null, "action": "KC_TRNS" },
        { "key_index": 13, "row": 3, "col": 1, "midi": { "type": "program_change", "program": 19 } },
        { "key_index": 14, "row": 3, "col": 2, "midi": { "type": "program_change", "program": 20 } },
        { "key_index": 15, "row": 3, "col": 3, "midi": null, "action": "QK_BOOT" }
      ]
    }
  ],
  "encoders": [
    {
      "index": 0,
      "name": "left",
      "clockwise":         { "midi": null, "action": "KC_VOLU" },
      "counter_clockwise": { "midi": null, "action": "KC_VOLD" }
    },
    {
      "index": 1,
      "name": "right",
      "clockwise":         { "midi": { "type": "program_change", "program": 30 } },
      "counter_clockwise": { "midi": { "type": "program_change", "program": 40 } }
    }
  ],
  "led_indicators": [
    { "led_index": 1,  "r": 255, "g": 0,   "b": 0   },
    { "led_index": 2,  "r": 255, "g": 0,   "b": 0   },
    { "led_index": 3,  "r": 255, "g": 0,   "b": 0   },
    { "led_index": 9,  "r": 0,   "g": 0,   "b": 255 },
    { "led_index": 12, "r": 0,   "g": 0,   "b": 255 },
    { "led_index": 13, "r": 0,   "g": 0,   "b": 255 },
    { "led_index": 14, "r": 0,   "g": 0,   "b": 255 }
  ]
}
```

---

## Notes for Bitwig Extension

- **Event type:** Listen for `Program Change` (`0xC0 | channel`), not Note On/Off.
- **Program numbers** 4–25 come from keys; 30 and 40 come from encoder 1. No offset adjustment needed — values are used directly.
- **Channel** is runtime-configurable on the keyboard via `midi_config.channel`. The extension should either listen on all channels or expose a channel selector.
- **Fn layer keys** (PC 19–25) are only reachable while holding the bottom-left key (`MO(1)`), so those PC numbers will only appear while that key is held.
- **Encoder 1** values (PC 30, PC 40) are outside the key range (4–25) and can be distinguished easily.
