package offthecob.common

import com.bitwig.extension.api.PlatformType
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList
import com.bitwig.extension.controller.ControllerExtensionDefinition
import java.util.*

data class HardwareDefinition(val vendor: String, val model: String, val friendlyName: String)

abstract class CommonExtensionDefinition(
        private val extensionName: String,
        private val extensionAuthor: String,
        private val extensionUuid: UUID,
        val hardwareDefinition: HardwareDefinition) : ControllerExtensionDefinition() {
    override fun getName(): String {
        return extensionName
    }

    override fun getAuthor(): String {
        return extensionAuthor
    }

    override fun getVersion(): String {
        return "0.1"
    }

    override fun getId(): UUID {
        return extensionUuid
    }

    override fun getHardwareVendor(): String {
        return hardwareDefinition.vendor
    }

    override fun getHardwareModel(): String {
        return hardwareDefinition.model
    }

    override fun getRequiredAPIVersion(): Int {
        return 20
    }

    override fun getNumMidiInPorts(): Int {
        return 1
    }

    override fun getNumMidiOutPorts(): Int {
        return 1
    }

    override fun listAutoDetectionMidiPortNames(list: AutoDetectionMidiPortNamesList, platformType: PlatformType) {
        when (platformType) {
            PlatformType.WINDOWS -> list.add(arrayOf(hardwareDefinition.friendlyName), arrayOf(hardwareDefinition.friendlyName))
            PlatformType.MAC -> {
                // TODO: Set the correct names of the ports for auto detection on Windows platform here
                // and uncomment this when port names are correct.
                // list.add(new String[]{"Input Port 0"}, new String[]{"Output Port 0"});
            }
            PlatformType.LINUX -> {
                // TODO: Set the correct names of the ports for auto detection on Windows platform here
                // and uncomment this when port names are correct.
                // list.add(new String[]{"Input Port 0"}, new String[]{"Output Port 0"});
            }
        }
    }
}