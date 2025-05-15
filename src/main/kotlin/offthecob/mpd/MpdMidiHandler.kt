package offthecob.mpd

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import com.bitwig.extension.controller.api.ControllerHost
import com.bitwig.extension.controller.api.Transport
import com.google.inject.Inject
import offthecob.common.MidiHandler
import offthecob.common.NoteData
import offthecob.mpd.banks.BankA
import offthecob.mpd.banks.BankB
import offthecob.mpd.banks.BankC
import offthecob.mpd.cc.DeviceCc
import offthecob.mpd.cc.VolumeTrackSendsCc

class MpdMidiHandler
    @Inject
    constructor(
        private val host: ControllerHost,
        private val bankA: BankA,
        private val bankB: BankB,
        private val bankC: BankC,
        private val volumeTrackSendsCc: VolumeTrackSendsCc,
        private val deviceCc: DeviceCc,
        private val navigation: Navigation,
        private val transport: Transport,
        private val mode: Mode,
    ) : MidiHandler {
        override fun handleMessage(msg: ShortMidiMessage) {
            when {
                msg.isNoteOn -> handleNote(msg.data1)
                msg.isControlChange -> handleControlChange(msg.data1, msg.data2)
            }
        }

        private fun handleNote(note: Int) {
            host.println("noteon! $note")
            when {
                bankA.isBank(note) -> bankA.handle(note)
                bankB.isBank(note) -> bankB.handle(note)
                bankC.isBank(note) -> bankC.handle(note)
            }
        }

        private fun handleControlChange(
            cc: Int,
            value: Int,
        ) {
            host.println("cc: $cc $value")
            when (mode.knobSlider()) {
                KnobSlider.VolumeSend -> volumeTrackSendsCc.cc(cc, value)
                KnobSlider.Device -> deviceCc.cc(cc, value)
            }
        }

        override fun handleSysexMessage(data: String) {
            host.println("sysex: $data")
            // MMC Transport Controls:
            when (data) {
                MPD_BAND_A -> mode.volumeSends()
                MPD_BANK_B -> {
                    mode.volumeSends()
                    navigation.toggleClipLauncher()
                }
                MPD_BANK_C -> mode.device()
                REWIND -> transport.rewind()
                FAST_FORWARD -> transport.fastForward()
                STOP -> transport.stop()
                PLAY -> transport.play()
                RECORD -> transport.record()
                PREVIEW_PRESS -> mode.previewPressed()
                PREVIEW_RELEASE -> mode.previewRelease()
            }
        }

        override fun noteInput(): Array<NoteData> {
            return arrayOf()
        }
    }
