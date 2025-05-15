package offthecob.mpd.banks

interface MpdBank {
    fun handle(note: Int)

    fun isBank(note: Int): Boolean
}
