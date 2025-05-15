package offthecob.common

import com.bitwig.extension.api.util.midi.ShortMidiMessage

interface MidiHandler {
    fun handleMessage(msg: ShortMidiMessage)

    fun handleSysexMessage(data: String)

    fun noteInput(): Array<NoteData>
}

data class NoteData(val name: String, val mask: Array<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NoteData

        if (name != other.name) return false
        if (!mask.contentEquals(other.mask)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + mask.contentHashCode()
        return result
    }
}
