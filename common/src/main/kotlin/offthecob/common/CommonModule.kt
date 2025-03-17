package offthecob.common

import com.bitwig.extension.controller.api.Application
import com.bitwig.extension.controller.api.Arranger
import com.bitwig.extension.controller.api.ControllerHost
import com.bitwig.extension.controller.api.Transport
import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton

class CommonModule(private val host: ControllerHost) : AbstractModule() {
    override fun configure() {
    }

    @Provides
    @Singleton
    fun application(): Application {
        return host.createApplication()
    }

    @Provides
    @Singleton
    fun host(): ControllerHost {
        return host
    }

    @Provides
    @Singleton
    fun transport(): Transport {
        return host.createTransport()
    }

    @Provides
    fun arranger(): Arranger {
        return host.createArranger()
    }
}
