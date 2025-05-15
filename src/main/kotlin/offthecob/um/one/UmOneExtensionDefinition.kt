package offthecob.um.one

import com.bitwig.extension.controller.api.ControllerHost
import com.google.inject.Guice
import offthecob.common.BitwigExtension
import offthecob.common.CommonExtensionDefinition
import offthecob.common.CommonModule
import offthecob.common.HardwareDefinition
import offthecob.common.MidiHandler
import java.util.UUID

class UmOneExtensionDefinition : CommonExtensionDefinition(
    "um-one",
    "whodevil",
    UUID.fromString("f8830c3e-8757-4a1f-8230-8f31e63b7570"),
    HardwareDefinition("Roland", "um-one", "UM-ONE"),
) {
    override fun createInstance(host: ControllerHost): BitwigExtension {
        return object : BitwigExtension(this, host) {
            override fun fetchHandler(host: ControllerHost): MidiHandler {
                val injector = Guice.createInjector(CommonModule(host))
                return injector.getInstance(UmMidiHandler::class.java)
            }
        }
    }
}
