package offthecob.mpd.banks

import com.bitwig.extension.controller.api.CursorRemoteControlsPage
import com.bitwig.extension.controller.api.PinnableCursorDevice
import jakarta.inject.Inject
import offthecob.mpd.TrackHandler

class BankC
    @Inject
    constructor(private val trackHandler: TrackHandler) : MpdBank {
        override fun handle(note: Int) {
            when (note) {
                /**
                 * 80 81 82 83
                 * 76 77 78 79
                 * 72 73 74 75
                 * 68 69 70 71
                 */
                68 -> trackHandler.knob(advanceCursorDevice)
                69 -> trackHandler.knob(advanceCursorDevicePage)
                70 -> trackHandler.knob(toggleEnabled)

                76 -> trackHandler.slider(advanceCursorDevice)
                77 -> trackHandler.slider(advanceCursorDevicePage)
                78 -> trackHandler.slider(toggleEnabled)
            }
        }

        override fun isBank(note: Int): Boolean {
            return (68 <= note) && (note <= 83)
        }

        private val advanceCursorDevice: (
            cursorDevice: PinnableCursorDevice,
            cursorRemoteControl: CursorRemoteControlsPage,
        ) -> Unit = { cursorDevice, _ ->
            if (cursorDevice.hasNext().get()) {
                cursorDevice.selectNext()
            } else {
                cursorDevice.selectFirst()
            }
        }

        private val advanceCursorDevicePage: (
            cursorDevice: PinnableCursorDevice,
            cursorRemoteControl: CursorRemoteControlsPage,
        ) -> Unit = { _, cursorRemoteControl ->
            cursorRemoteControl.selectNextPage(true)
        }

        private val toggleEnabled: (
            cursorDevice: PinnableCursorDevice,
            cursorRemoteControl: CursorRemoteControlsPage,
        ) -> Unit = { cursorDevice, _ ->
            cursorDevice.isEnabled.toggle()
        }
    }
