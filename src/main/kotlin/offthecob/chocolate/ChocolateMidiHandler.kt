package offthecob.chocolate

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import com.bitwig.extension.controller.api.ClipLauncherSlotBank
import com.bitwig.extension.controller.api.ControllerHost
import com.bitwig.extension.controller.api.SceneBank
import com.bitwig.extension.controller.api.TrackBank
import com.bitwig.extension.controller.api.Transport
import offthecob.chocolate.ChocolateMode.CLIP
import offthecob.chocolate.ChocolateMode.SCENE
import offthecob.common.MidiHandler
import offthecob.common.NoteData

enum class ChocolateMode() {
    SCENE,
    CLIP,
}

class ChocolateMidiHandler(
    private val host: ControllerHost,
    private val transport: Transport,
    private val sceneBank: SceneBank,
    private val trackBank: TrackBank,
    private val clipLauncherSlotBank: ClipLauncherSlotBank
) : MidiHandler {

    var mode: ChocolateMode = CLIP

    override fun handleMessage(msg: ShortMidiMessage) {
        host.println("pc: ${msg.isProgramChange}, $msg")
        when (msg.data1) {
            11 -> toggleMode()

            40 -> volumeUp()
            30 -> volumeDown()

            15 -> trackArm()
            16 -> trackSolo()
            17 -> trackMute()
            18 -> deactivate()

            10 -> insertDevice()
            8 -> startHardStop()
            7 -> recordCLip()
            19 -> deleteClip()

            9 -> scrollClipUp()
            6 -> scrollClipForward()
            5 -> scrollClipDown()
            4 -> scrollClipBack()

            // foo
            3 -> d()
            2 -> c()
            1 -> b()
            0 -> a()
        }
    }

    private fun d() {
        host.println("d")
        when(mode) {
           CLIP -> startStop()
           SCENE -> startHardStop()
        }
    }

    private fun c() {
        host.println("c")
        when(mode) {
           CLIP -> recordCLip()
           SCENE -> playScene()
        }
    }

    private fun b() {
        host.println("b")
        when(mode) {
            CLIP -> triggerNextScene()
            SCENE -> scrollSceneForward()
        }
    }

    private fun a() {
        host.println("a")
        when(mode) {
            CLIP -> triggerPreviousScene()
            SCENE -> scrollSceneBack()
        }
    }

    private fun triggerPreviousScene() {
        scrollSceneBack()
        playScene()
    }

    private fun triggerNextScene() {
        scrollSceneForward()
        playScene()
    }

    private fun toggleMode() {
        if(mode == CLIP) {
            host.showPopupNotification("Scene Mode")
            mode = SCENE
        } else {
            host.showPopupNotification("Clip Mode")
            mode = CLIP
        }
    }

    private fun deleteClip() {
        host.println("delete clip")
        clipLauncherSlotBank.getItemAt(0).deleteObject()
    }

    private fun insertDevice() {
        host.println("insert device")
        trackBank.getItemAt(0).endOfDeviceChainInsertionPoint().browse()
    }

    private fun volumeDown() {
        host.println("volume down")
        trackBank.getItemAt(0).volume().inc(-.03)
    }

    private fun volumeUp() {
        host.println("volume up")
        trackBank.getItemAt(0).volume().inc(.03)
    }

    private fun deactivate() {
        host.println("toggle active")
        trackBank.getItemAt(0).isActivated.toggle()
    }

    private fun trackMute() {
        host.println("track mute")
        trackBank.getItemAt(0).mute().toggle()
    }

    private fun trackSolo() {
        host.println("track solo")
        trackBank.getItemAt(0).solo().toggle()
    }

    private fun trackArm() {
        host.println("track arm")
        trackBank.getItemAt(0).arm().toggle()
    }

    private fun recordCLip() {
        host.println("record clip")
        val clip = clipLauncherSlotBank.getItemAt(0)
        if (clip.isRecording.get()) {
            clip.launch()
        } else {
            clip.record()
        }
    }

    private fun scrollClipUp() {
        host.println("clip up")
        trackBank.scrollBackwards()
    }

    private fun scrollClipForward() {
        host.println("clip forward")
        clipLauncherSlotBank.scrollForwards()
    }

    private fun scrollClipDown() {
        host.println("clip down")
        trackBank.scrollForwards()
    }

    private fun scrollClipBack() {
        host.println("clip back")
        clipLauncherSlotBank.scrollBackwards()
    }

    private fun scrollSceneBack() {
        host.println("back")
        sceneBank.getScene(0)
        sceneBank.scrollBackwards()
    }

    private fun playScene() {
        host.println("playScene")
        sceneBank.getScene(0).launch()
    }

    private fun scrollSceneForward() {
        host.println("forward")
        sceneBank.scrollForwards()
    }

    private fun startStop() {
        if (transport.isPlaying.get()) {
            transport.stop()
        } else {
            transport.play()
        }
    }

    private fun startHardStop() {
        if (transport.isPlaying.get()) {
            transport.stop()
            sceneBank.stop()
        } else {
            transport.play()
        }
    }

    override fun handleSysexMessage(data: String) {
        //TODO("Not yet implemented")
    }

    override fun noteInput(): Array<NoteData> {
        //TODO("Not yet implemented")
        return arrayOf()
    }

}
