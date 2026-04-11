package offthecob.mpd.banks

import com.bitwig.extension.controller.api.ClipLauncherSlot
import com.bitwig.extension.controller.api.Track
import jakarta.inject.Inject
import offthecob.mpd.Mode
import offthecob.mpd.Preview
import offthecob.mpd.TrackHandler

class BankB
    @Inject
    constructor(private val trackHandler: TrackHandler, private val mode: Mode) : MpdBank {
        override fun isBank(note: Int): Boolean {
            return (52 <= note) && (note <= 67)
        }

        override fun handle(note: Int) {
            when (note) {
                52 -> handleClip(3, 0)
                53 -> handleClip(3, 1)
                54 -> handleClip(3, 2)
                55 -> handleClip(3, 3)
                56 -> handleClip(2, 0)
                57 -> handleClip(2, 1)
                58 -> handleClip(2, 2)
                59 -> handleClip(2, 3)
                60 -> handleClip(1, 0)
                61 -> handleClip(1, 1)
                62 -> handleClip(1, 2)
                63 -> handleClip(1, 3)
                64 -> handleClip(0, 0)
                65 -> handleClip(0, 1)
                66 -> handleClip(0, 2)
                67 -> handleClip(0, 3)
            }
        }

        private fun handleClip(
            trackNumber: Int,
            clipIndex: Int,
        ) {
            when (mode.previewButton()) {
                Preview.Pressed -> trackHandler.clip(trackNumber, clipIndex, deleteClip)
                Preview.Released -> trackHandler.clip(trackNumber, clipIndex, playOrRecordClip)
            }
        }

        private val deleteClip: (clip: ClipLauncherSlot, track: Track) -> Unit = { clip, track ->
            track.clipLauncherSlotBank().getItemAt(clip.sceneIndex().get()).deleteObject()
        }

        private val playOrRecordClip: (clip: ClipLauncherSlot, track: Track) -> Unit = { clip, track ->
            if (!clip.hasContent().get()) {
                track.arm().set(true)
                clip.launch()
            } else if (clip.isRecording.get() || !clip.isPlaying.get()) {
                clip.launch()
            } else {
                track.stop()
            }
        }
    }
