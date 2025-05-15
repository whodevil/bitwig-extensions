package offthecob.pedalboard

import com.bitwig.extension.controller.ControllerExtension
import com.bitwig.extension.controller.api.ControllerHost
import com.google.inject.Guice
import offthecob.common.BitwigExtension
import offthecob.common.CommonExtensionDefinition
import offthecob.common.CommonModule
import offthecob.common.HardwareDefinition
import offthecob.common.MidiHandler
import java.util.UUID

class PedalBoardDefinition : CommonExtensionDefinition(
    "pedalboard",
    "whodevil",
    UUID.fromString("8b39b79b-4f08-407c-9a4f-6f269fbd58a8"),
    HardwareDefinition("offthecob", "pedal board", "MIDIIN3 (Midihub MH-2GX5CQK)", midiIn = 1, midiOut = 0)
){
    override fun createInstance(host: ControllerHost): ControllerExtension {
        return object : BitwigExtension(this, host) {
            override fun fetchHandler(host: ControllerHost): MidiHandler {
                host.println("boom!")
                val injector = Guice.createInjector(CommonModule(host))
                return injector.getInstance(PedalBoardMidiHandler::class.java)
            }
        }
    }
}
