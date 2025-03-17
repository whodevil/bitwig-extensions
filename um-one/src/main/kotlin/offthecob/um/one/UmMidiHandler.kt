package offthecob.um.one

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import com.bitwig.extension.controller.api.ControllerHost
import com.google.inject.Inject
import offthecob.common.MidiHandler
import offthecob.common.NoteData

class UmMidiHandler
    @Inject
    constructor(private val host: ControllerHost) : MidiHandler {
        override fun handleMessage(msg: ShortMidiMessage) {
            msg.statusByte
            when {
                msg.isNoteOn -> host.println("channel 5")
            }
        }

        override fun handleSysexMessage(data: String) {
        }

        override fun noteInput(): Array<NoteData> {
            val noteDataArray = mutableListOf(NoteData("all channels except 5", ALL_CHANNELS_BUT_5))
            channels().forEach {
                noteDataArray.add(NoteData(it.friendlyName, generateMaskForChannel(it.hexString).toTypedArray()))
            }
            return noteDataArray.toTypedArray()
        }
    }
