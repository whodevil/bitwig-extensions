package com.offthecob

import com.bitwig.extension.controller.api.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackHandler @Inject constructor(host: ControllerHost) {

    private val trackBank: TrackBank = host.createTrackBank(4, 2, 4)
    private val effectTrackBank: TrackBank = host.createEffectTrackBank(2, 2)

    private val cursorTrack: CursorTrack = host.createCursorTrack("MPD_TRACK_ID", "Cursor Track", 0, 0, true)
    private val knobCursorDevice: PinnableCursorDevice =
            cursorTrack.createCursorDevice(
                    "MPD_CURSOR_DEVICE_KNOBS",
                    "Cursor Device Knobs",
                    0,
                    CursorDeviceFollowMode.FOLLOW_SELECTION)
    private val knobRemoteControlPage: CursorRemoteControlsPage = knobCursorDevice.createCursorRemoteControlsPage(8)

    private val sliderCursorDevice: PinnableCursorDevice =
            cursorTrack.createCursorDevice(
                    "MPD_CURSOR_DEVICE_SLIDERS",
                    "Cursor Device Sliders",
                    0,
                    CursorDeviceFollowMode.FIRST_AUDIO_EFFECT)
    private val sliderRemoteControlPage: CursorRemoteControlsPage = sliderCursorDevice.createCursorRemoteControlsPage(6)

    init {
        initTracks(trackBank)
        initEffectTracks(effectTrackBank)
        initCursorTrack(cursorTrack)
        initRemoteControlsPage(knobRemoteControlPage)
        initRemoteControlsPage(sliderRemoteControlPage)
        initCursorDevice(knobCursorDevice)
        initCursorDevice(sliderCursorDevice)
    }

    fun select(i: Int) {
        trackBank.getItemAt(i).selectInEditor()
    }

    fun bankUp() {
        trackBank.scrollBackwards()
    }

    fun bankDown() {
        trackBank.scrollForwards()
    }

    fun toggleArmed() {
        cursorTrack.arm().toggle()
    }

    fun toggleSolo() {
        cursorTrack.solo().toggle()
    }

    fun toggleMute() {
        cursorTrack.mute().toggle()
    }

    fun trackSend(trackNumber: Int, sendNumber: Int, sendValue: Int) {
        val send = trackBank.getItemAt(trackNumber).sendBank().getItemAt(sendNumber)
        send.set(sendValue, 128)
    }

    fun volume(trackNumber: Int, value: Int) {
        trackBank.getItemAt(trackNumber).volume().set(value, 128)
    }

    fun effectTrackVolume(sendNumber: Int, value: Int) {
        effectTrackBank.getItemAt(sendNumber).volume().set(value, 128)
    }

    fun effectBankUp() {
        effectTrackBank.scrollBackwards()
    }

    fun effectBankDown() {
        effectTrackBank.scrollForwards()
    }

    fun trackSendBankUp() {
        var i = 0
        while (i < trackBank.sizeOfBank) {
            val track = trackBank.getItemAt(i)
            track.sendBank().scrollBackwards()
            i++
        }
    }

    fun trackSendBankDown() {
        var i = 0
        while (i < trackBank.sizeOfBank) {
            val track = trackBank.getItemAt(i)
            track.sendBank().scrollForwards()
            i++
        }
    }

    fun scrollForward() {
        trackBank.sceneBank().scrollForwards()
    }

    fun scrollBackward() {
        trackBank.sceneBank().scrollBackwards()
    }

    fun advanceKnobCursorDevice() {
        advanceCursorDevice(knobCursorDevice)
    }

    fun advanceSliderCursorDevice() {
        advanceCursorDevice(sliderCursorDevice)
    }

    private fun advanceCursorDevice(cursorDevice: PinnableCursorDevice) {
        if (cursorDevice.hasNext().get()) {
            cursorDevice.selectNext()
        } else {
            cursorDevice.selectFirst()
        }
    }

    fun deviceKnob(parameterNumber: Int, value: Int) {
        knobRemoteControlPage.getParameter(parameterNumber).set(value, 128)
    }

    fun advanceKnobDevicePage() {
        knobRemoteControlPage.selectNextPage(true)
    }

    fun advanceSliderDevicePage() {
        sliderRemoteControlPage.selectNextPage(true)
    }

    fun deviceSlider(slider: Int, value: Int) {
        sliderRemoteControlPage.getParameter(slider).set(value, 128)
    }

    fun enableKnobDeviceToggle() {
        knobCursorDevice.isEnabled.toggle()
    }

    fun enableSliderDeviceToggle() {
        sliderCursorDevice.isEnabled.toggle()
    }

    fun clip(trackNumber: Int, clipIndex: Int, function: (clip: ClipLauncherSlot, track: Track) -> Unit) {
        val track = trackBank.getItemAt(trackNumber)
        val clipLauncherSlotBank = track.clipLauncherSlotBank()
        val clip = clipLauncherSlotBank.getItemAt(clipIndex)
        function(clip, track)
    }
}
