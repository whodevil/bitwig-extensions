package offthecob.pedalboard

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import offthecob.common.MidiHandler
import offthecob.common.NoteData

class PedalBoardMidiHandler : MidiHandler {
    override fun handleMessage(msg: ShortMidiMessage) {
        //TODO("Not yet implemented")
    }

    override fun handleSysexMessage(data: String) {
        //TODO("Not yet implemented")
    }

    override fun noteInput(): Array<NoteData> {
        //TODO("Not yet implemented")
        return arrayOf()
    }

}
