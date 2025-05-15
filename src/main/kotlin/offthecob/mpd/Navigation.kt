package offthecob.mpd

import com.bitwig.extension.controller.api.Arranger
import com.google.inject.Inject

class Navigation
    @Inject
    constructor(private val arranger: Arranger) {
        init {
            arranger.isClipLauncherVisible.markInterested()
        }

        fun toggleClipLauncher() {
            arranger.isClipLauncherVisible.toggle()
        }
    }
