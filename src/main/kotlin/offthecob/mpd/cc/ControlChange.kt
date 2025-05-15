package offthecob.mpd.cc

interface ControlChange {
    fun cc(
        note: Int,
        value: Int,
    )
}
