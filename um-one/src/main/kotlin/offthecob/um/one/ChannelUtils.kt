package offthecob.um.one

const val CHANNEL: String = "Channel"

data class Channel(val hexString: String, val friendlyName: String)

fun channels() =
    listOf<Channel>(
        Channel("A", "$CHANNEL 11"),
        Channel("B", "$CHANNEL 12"),
        Channel("C", "$CHANNEL 13"),
        Channel("D", "$CHANNEL 14"),
        Channel("E", "$CHANNEL 15"),
        Channel("F", "$CHANNEL 16"),
    )

fun generateMaskForChannel(hexChannel: String): List<String> {
    val midiCommandIds = listOf("8", "9", "A", "B", "C", "D", "E")
    return midiCommandIds.map { midiCommand ->
        "${midiCommand}$hexChannel????"
    }
}

/**
 * This filter includes all messages except those coming on channel 5.
 */
val ALL_CHANNELS_BUT_5: Array<String> =
    channels().map { channel ->
        generateMaskForChannel(channel.hexString)
    }.flatten().toTypedArray()
