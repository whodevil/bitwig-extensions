package offthecob.mpd

import com.bitwig.extension.controller.api.ClipLauncherSlotBank
import com.bitwig.extension.controller.api.CursorRemoteControlsPage
import com.bitwig.extension.controller.api.CursorTrack
import com.bitwig.extension.controller.api.PinnableCursorDevice
import com.bitwig.extension.controller.api.Track
import com.bitwig.extension.controller.api.TrackBank

fun initRemoteControlsPage(knobRemoteControlPage: CursorRemoteControlsPage) {
    var i = 0
    while (i < knobRemoteControlPage.parameterCount) {
        val parameter = knobRemoteControlPage.getParameter(i)
        parameter.markInterested()
        parameter.setIndication(true)
        i++
    }
}

fun initCursorTrack(cursorTrack: CursorTrack) {
    cursorTrack.mute().markInterested()
    cursorTrack.solo().markInterested()
    cursorTrack.arm().markInterested()
    cursorTrack.makeVisibleInArranger()
    cursorTrack.makeVisibleInMixer()
}

fun initEffectTracks(effectTrackBank: TrackBank) {
    var i = 0
    while (i < effectTrackBank.sizeOfBank) {
        val effectTrack = effectTrackBank.getItemAt(i)
        val volume = effectTrack.volume()
        volume.markInterested()
        volume.setIndication(true)
        i++
    }
}

fun initTracks(trackBank: TrackBank) {
    var i = 0
    while (i < trackBank.sizeOfBank) {
        markTrackInterested(trackBank.getItemAt(i))
        i++
    }
}

private fun markTrackInterested(track: Track) {
    val volume = track.volume()
    volume.markInterested()
    volume.setIndication(true)
    markClips(track.clipLauncherSlotBank())
    markTrackSends(track)
}

private fun markClips(clipBank: ClipLauncherSlotBank) {
    //clipBank.setIndication(true)
    var i = 0
    while (i < clipBank.sizeOfBank) {
        val clip = clipBank.getItemAt(i)
        clip.hasContent().markInterested()
        clip.isPlaying.markInterested()
        clip.isPlaybackQueued.markInterested()
        clip.isRecording.markInterested()
        clip.isStopQueued.markInterested()
        clip.isRecordingQueued.markInterested()
        clip.sceneIndex().markInterested()
        i++
    }
}

private fun markTrackSends(track: Track) {
    var j = 0
    val sendBank = track.sendBank()
    while (j < sendBank.sizeOfBank) {
        val send = sendBank.getItemAt(j)
        send.markInterested()
        send.setIndication(true)
        j++
    }
}

fun initCursorDevice(knobCursorDevice: PinnableCursorDevice) {
    knobCursorDevice.isEnabled.markInterested()
    knobCursorDevice.isWindowOpen.markInterested()
    knobCursorDevice.hasNext().markInterested()
}
