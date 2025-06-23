package offthecob.chocolate

import com.bitwig.extension.controller.ControllerExtension
import com.bitwig.extension.controller.api.*
import offthecob.common.BitwigExtension
import offthecob.common.CommonExtensionDefinition
import offthecob.common.HardwareDefinition
import offthecob.common.MidiHandler
import java.util.*


class ChocolateDefinition : CommonExtensionDefinition(
    "chocolate",
    "whodevil",
    UUID.fromString("4c7dde4d-ddbd-4512-b32c-a66257c54a6e"),
    HardwareDefinition("offthecob", "chocolate", "Devo FootCtrl Bluetooth", midiIn = 2, midiOut = 0)
) {
    override fun createInstance(host: ControllerHost): ControllerExtension {
        return object : BitwigExtension(this, host) {
            override fun fetchHandler(host: ControllerHost): MidiHandler {
                host.println("boom!")
                val transport = host.transport()
                val sceneBank = host.sceneBank()
                val trackBank = host.createTrackBank(1, 1, 1)
                val clipLauncherSlotBank = trackBank.init(host)
                return ChocolateMidiHandler(
                    host,
                    transport,
                    sceneBank,
                    trackBank,
                    clipLauncherSlotBank
                )
            }
        }
    }
}

fun ClipLauncherSlot.markInterested() {
    isRecording.markInterested()
    isPlaying.markInterested()
    hasContent().markInterested()
    isStopQueued.markInterested()
    isRecordingQueued.markInterested()
    isSelected.markInterested()
    isPlaybackQueued.markInterested()
}

fun Track.markInterested() {
    solo().markInterested()
    arm().markInterested()
    mute().markInterested()
    volume().markInterested()
    volume().setIndication(true)
    isActivated.markInterested()
}

fun TrackBank.init(host: ControllerHost): ClipLauncherSlotBank {
    val cursorTrack = host.createCursorTrack("chocolate", "Cursor Track", 0, 1, true)
    followCursorTrack(cursorTrack)
    setShouldShowClipLauncherFeedback(true)
    val track = getItemAt(0)
    val send = track.sendBank().getItemAt(0)
    send.markInterested()
    send.setIndication(true)
    track.markInterested()
    val clipLauncherSlotBank = track.clipLauncherSlotBank()
    clipLauncherSlotBank.getItemAt(0).markInterested()
    return clipLauncherSlotBank
}

fun ControllerHost.transport(): Transport {
    val transport = createTransport()
    transport.isPlaying.markInterested()
    return transport
}

fun ControllerHost.sceneBank(): SceneBank {
    val sceneBank = createSceneBank(1)
    sceneBank.setIndication(true)
    return sceneBank
}