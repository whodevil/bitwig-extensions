package offthecob.mpd.banks

import com.bitwig.extension.controller.api.Application
import com.google.inject.Inject
import offthecob.mpd.TrackHandler

class BankA
    @Inject
    constructor(private val trackHandler: TrackHandler, private val application: Application) : MpdBank {
        override fun handle(note: Int) {
            when (note) {
                36 -> trackHandler.scrollBackward()
                37 -> trackHandler.bankUp()
                38 -> trackHandler.bankDown()
                39 -> trackHandler.scrollForward()
                40 -> trackHandler.select(0)
                41 -> trackHandler.select(1)
                42 -> trackHandler.select(2)
                43 -> trackHandler.select(3)
                44 -> trackHandler.effectBankUp()
                45 -> trackHandler.effectBankDown()
                46 -> trackHandler.trackSendBankUp()
                47 -> trackHandler.trackSendBankDown()
                48 -> trackHandler.toggleArmed()
                49 -> trackHandler.toggleSolo()
                50 -> trackHandler.toggleMute()
                51 -> application.nextPanelLayout()
            }
        }

        override fun isBank(note: Int): Boolean {
            return (36 <= note) && (note <= 51)
        }
    }
