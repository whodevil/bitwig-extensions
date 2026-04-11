package offthecob.chocolate

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import com.bitwig.extension.controller.api.Application
import com.bitwig.extension.controller.api.ClipLauncherSlotBank
import com.bitwig.extension.controller.api.ControllerHost
import com.bitwig.extension.controller.api.PinnableCursorDevice
import com.bitwig.extension.controller.api.PopupBrowser
import com.bitwig.extension.controller.api.SceneBank
import com.bitwig.extension.controller.api.TrackBank
import com.bitwig.extension.controller.api.Transport
import offthecob.chocolate.FootswitchMode.CLIP
import offthecob.chocolate.FootswitchMode.SCENE
import offthecob.chocolate.EncoderMode.VOLUME
import offthecob.common.MidiHandler
import offthecob.common.NoteData

enum class FootswitchMode {
    SCENE,
    CLIP,
}

enum class EncoderMode {
    VOLUME,
    SEND,
    TRANSPORT
}

enum class KeypadMode {
    DEFAULT,
    DEVICE,
}

enum class DeviceState {
    NAVIGATION,
    BROWSING,
}

class ChocolateMidiHandler(
    private val host: ControllerHost,
    private val transport: Transport,
    private val sceneBank: SceneBank,
    private val trackBank: TrackBank,
    private val clipLauncherSlotBank: ClipLauncherSlotBank,
    private val application: Application,
    private val cursorDevice: PinnableCursorDevice,
    private val popupBrowser: PopupBrowser
) : MidiHandler {

    private var footswitchMode: FootswitchMode = CLIP
    private var encoderMode: EncoderMode = VOLUME
    private var keypadMode: KeypadMode = KeypadMode.DEFAULT
    private var deviceState: DeviceState = DeviceState.NAVIGATION

    init {
        popupBrowser.exists().addValueObserver { exists ->
            if (!exists && deviceState == DeviceState.BROWSING) {
                deviceState = DeviceState.NAVIGATION
            }
        }
    }

    override fun handleMessage(msg: ShortMidiMessage) {
        host.println("pc: ${msg.isProgramChange}, $msg")
        when (msg.data1) {
            11 -> toggleFootswitchMode()
            12 -> toggleKeypadMode()
            25 -> toggleEncoderMode()

            40 -> encoderClockwise()
            30 -> encoderCounterClockwise()
            23 -> sendScrollUp()
            20 -> sendScrollDown()

            15 -> trackArm()
            16 -> trackSolo()
            17 -> trackMute()
            18 -> deactivate()

            8 -> startHardStop()
            7 -> recordCLip()
            21 -> deleteClip()

            4 -> when (keypadMode) {
                KeypadMode.DEFAULT -> scrollClipBack()
                KeypadMode.DEVICE -> when (deviceState) {
                    DeviceState.NAVIGATION -> moveInsertionCursorBack()
                    DeviceState.BROWSING -> {} // no-op
                }
            }
            5 -> when (keypadMode) {
                KeypadMode.DEFAULT -> scrollClipDown()
                KeypadMode.DEVICE -> when (deviceState) {
                    DeviceState.NAVIGATION -> scrollClipDown()
                    DeviceState.BROWSING -> scrollBrowserDown()
                }
            }
            6 -> when (keypadMode) {
                KeypadMode.DEFAULT -> scrollClipForward()
                KeypadMode.DEVICE -> when (deviceState) {
                    DeviceState.NAVIGATION -> moveInsertionCursorForward()
                    DeviceState.BROWSING -> {} // no-op
                }
            }
            9 -> when (keypadMode) {
                KeypadMode.DEFAULT -> scrollClipUp()
                KeypadMode.DEVICE -> when (deviceState) {
                    DeviceState.NAVIGATION -> scrollClipUp()
                    DeviceState.BROWSING -> scrollBrowserUp()
                }
            }
            10 -> when (keypadMode) {
                KeypadMode.DEFAULT -> {} // no-op
                KeypadMode.DEVICE -> when (deviceState) {
                    DeviceState.NAVIGATION -> openDeviceBrowser()
                    DeviceState.BROWSING -> commitBrowser()
                }
            }
            14 -> when (keypadMode) {
                KeypadMode.DEFAULT -> {} // no-op
                KeypadMode.DEVICE -> when (deviceState) {
                    DeviceState.NAVIGATION -> {} // no-op
                    DeviceState.BROWSING -> cancelBrowser()
                }
            }

            3 -> d()
            2 -> c()
            1 -> b()
            0 -> a()
        }
    }

    private fun sendScrollDown() {
        trackBank.getItemAt(0).sendBank().scrollForwards()
    }

    private fun sendScrollUp() {
        trackBank.getItemAt(0).sendBank().scrollBackwards()
    }

    private fun encoderCounterClockwise() {
        if(encoderMode == VOLUME) {
            volumeDown()
        } else {
            trackBank.getItemAt(0).sendBank().getItemAt(0).inc(-.03)
        }
    }

    private fun encoderClockwise() {
        if(encoderMode == VOLUME) {
            volumeUp()
        } else {
            trackBank.getItemAt(0).sendBank().getItemAt(0).inc(.03)
        }
    }

    private fun d() {
        host.println("d")
        when(footswitchMode) {
           CLIP -> startStop()
           SCENE -> startHardStop()
        }
    }

    private fun c() {
        host.println("c")
        when(footswitchMode) {
           CLIP -> recordCLip()
           SCENE -> playScene()
        }
    }

    private fun b() {
        host.println("b")
        when(footswitchMode) {
           CLIP -> triggerNextScene()
           SCENE -> scrollSceneForward()
        }
    }

    private fun a() {
        host.println("a")
        when(footswitchMode) {
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

    private fun moveInsertionCursorForward() {
        cursorDevice.selectNext()
    }

    private fun moveInsertionCursorBack() {
        cursorDevice.selectPrevious()
    }

    private fun openDeviceBrowser() {
        if (cursorDevice.exists().get()) {
            cursorDevice.afterDeviceInsertionPoint().browse()
        } else {
            trackBank.getItemAt(0).endOfDeviceChainInsertionPoint().browse()
        }
        deviceState = DeviceState.BROWSING
    }

    private fun commitBrowser() {
        popupBrowser.commit()
        deviceState = DeviceState.NAVIGATION
    }

    private fun cancelBrowser() {
        popupBrowser.cancel()
        deviceState = DeviceState.NAVIGATION
    }

    private fun scrollBrowserUp() {
        popupBrowser.selectPreviousFile()
    }

    private fun scrollBrowserDown() {
        popupBrowser.selectNextFile()
    }

    private fun toggleFootswitchMode() {
        if(footswitchMode == CLIP) {
            host.showPopupNotification("Scene Mode")
            footswitchMode = SCENE
        } else {
            host.showPopupNotification("Clip Mode")
            footswitchMode = CLIP
        }
    }

    private fun toggleKeypadMode() {
        if (keypadMode == KeypadMode.DEFAULT) {
            host.showPopupNotification("Device Mode")
            application.setPanelLayout("EDIT")
            keypadMode = KeypadMode.DEVICE
            deviceState = DeviceState.NAVIGATION
        } else {
            if (deviceState == DeviceState.BROWSING) {
                popupBrowser.cancel()
            }
            host.showPopupNotification("Default Mode")
            keypadMode = KeypadMode.DEFAULT
            deviceState = DeviceState.NAVIGATION
        }
    }

    private fun toggleEncoderMode() {
        when (encoderMode) {
            EncoderMode.VOLUME -> {
                host.showPopupNotification("Send Mode")
                encoderMode = EncoderMode.SEND
            }
            EncoderMode.SEND -> {
                host.showPopupNotification("Transport Mode")
                encoderMode = EncoderMode.TRANSPORT
            }
            EncoderMode.TRANSPORT -> {
                host.showPopupNotification("Volume Mode")
                encoderMode = EncoderMode.VOLUME
            }
        }
    }

    private fun deleteClip() {
        host.println("delete clip")
        clipLauncherSlotBank.getItemAt(0).deleteObject()
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
