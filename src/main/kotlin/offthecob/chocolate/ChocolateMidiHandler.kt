package offthecob.chocolate

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import com.bitwig.extension.controller.api.ClipLauncherSlotBank
import com.bitwig.extension.controller.api.ControllerHost
import com.bitwig.extension.controller.api.SceneBank
import com.bitwig.extension.controller.api.TrackBank
import com.bitwig.extension.controller.api.Transport
import offthecob.common.MidiHandler
import offthecob.common.NoteData

class ChocolateMidiHandler(
    private val host: ControllerHost,
    private val transport: Transport,
    private val sceneBank: SceneBank,
    private val trackBank: TrackBank,
    private val clipLauncherSlotBank: ClipLauncherSlotBank
) : MidiHandler {
    var slot = 0

    override fun handleMessage(msg: ShortMidiMessage) {
        host.println("pc: ${msg.isProgramChange}, ${msg}")
        when(msg.data1) {
            9 -> scrollClipUp()
            6 -> scrollClipForward()
            5 -> scrollClipDown()
            4 -> scrollClipBack()
            3 -> handleStopStart()
            2 -> scrollSceneForward()
            1 -> playScene()
            0 -> scrollSceneBack()
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

    private fun handleStopStart() {
        if(transport.isPlaying.get()) {
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
