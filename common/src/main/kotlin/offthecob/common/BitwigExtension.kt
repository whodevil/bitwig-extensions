package offthecob.common

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback
import com.bitwig.extension.controller.ControllerExtension
import com.bitwig.extension.controller.api.ControllerHost

abstract class BitwigExtension(val definition: CommonExtensionDefinition, host: ControllerHost) : ControllerExtension(definition, host) {
    abstract fun fetchHandler(host: ControllerHost): MidiHandler

    override fun init() {
        val midiHandler = fetchHandler(host)
        host.getMidiInPort(0).setMidiCallback(
            object : ShortMidiMessageReceivedCallback {
                override fun midiReceived(msg: ShortMidiMessage?) {
                    if (msg != null) {
                        midiHandler.handleMessage(msg)
                    }
                }
            },
        )

        host.getMidiInPort(0).setSysexCallback { data: String -> midiHandler.handleSysexMessage(data) }
        val noteInput = midiHandler.noteInput()
        if (noteInput.isNotEmpty()) {
            noteInput.forEach { noteData ->
                host.getMidiInPort(0).createNoteInput(noteData.name, *noteData.mask)
            }
        }
        host.showPopupNotification("${definition.hardwareDefinition.friendlyName} Initialized")
    }

    override fun exit() {
        host.showPopupNotification("${definition.hardwareDefinition.friendlyName} Exited")
    }

    override fun flush() {
        // TODO Send any updates you need here.
    }
}
