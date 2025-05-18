package offthecob.chocolate

import com.bitwig.extension.controller.ControllerExtension
import com.bitwig.extension.controller.api.ControllerHost
import com.bitwig.extension.controller.api.Transport
import offthecob.common.BitwigExtension
import offthecob.common.CommonExtensionDefinition
import offthecob.common.HardwareDefinition
import offthecob.common.MidiHandler
import java.util.UUID

class ChocolateDefinition : CommonExtensionDefinition(
    "chocolate",
    "whodevil",
    UUID.fromString("4c7dde4d-ddbd-4512-b32c-a66257c54a6e"),
    HardwareDefinition("offthecob", "chocolate", "Devo FootCtrl Bluetooth", midiIn = 2, midiOut = 0)
){
    override fun createInstance(host: ControllerHost): ControllerExtension {
        return object : BitwigExtension(this, host) {
            override fun fetchHandler(host: ControllerHost): MidiHandler {
                host.println("boom!")
                val transport: Transport = host.createTransport()
                transport.isPlaying.markInterested()
                val sceneBank = host.createTrackBank(1, 0, 1).sceneBank()
                sceneBank.setIndication(true)
                val trackBank = host.createTrackBank(1, 0, 1)
                val cursorTrack = host.createCursorTrack("chocolate", "Cursor Track", 0, 1, true)
                trackBank.followCursorTrack(cursorTrack)
                trackBank.setShouldShowClipLauncherFeedback(true)
                val clipLauncherSlotBank = trackBank.getItemAt(0).clipLauncherSlotBank()
                return ChocolateMidiHandler(host, transport, sceneBank, trackBank, clipLauncherSlotBank)
            }
        }
    }
}
