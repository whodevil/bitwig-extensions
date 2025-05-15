package offthecob.mpd

import com.bitwig.extension.controller.api.ControllerHost
import com.google.inject.Inject
import com.google.inject.Singleton

enum class KnobSlider {
    VolumeSend,
    Device,
}

enum class Preview {
    Pressed,
    Released,
}

@Singleton
class Mode
    @Inject
    constructor(private val host: ControllerHost) {
        fun knobSlider(): KnobSlider {
            return knobSliderMode
        }

        fun volumeSends() {
            knobSliderMode = KnobSlider.VolumeSend
        }

        fun device() {
            knobSliderMode = KnobSlider.Device
        }

        fun previewPressed() {
            host.println("previewPressed")
            preview = Preview.Pressed
        }

        fun previewRelease() {
            host.println("previewReleased")
            preview = Preview.Released
        }

        fun previewButton(): Preview {
            return preview
        }

        private var knobSliderMode: KnobSlider = KnobSlider.VolumeSend
        private var preview: Preview = Preview.Released
    }
