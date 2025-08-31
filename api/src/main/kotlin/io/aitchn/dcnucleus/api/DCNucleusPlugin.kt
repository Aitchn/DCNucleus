package io.aitchn.dcnucleus.api

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

abstract class DCNucleusPlugin {
    protected lateinit var name: String; private set
    protected lateinit var logger: Logger; private set
    protected lateinit var dataFolder: Path; private set
    private var isInjected: Boolean = false

    @Suppress("FunctionName")
    fun __inject(name: String) {
        if (isInjected) {
            throw IllegalStateException("Plugin has already been injected!")
        }

        this.name = name
        this.logger = LoggerFactory.getLogger("[$name]")
        this.dataFolder = Path.of(System.getProperty("user.dir"), "plugins", name)
        this.isInjected = true
    }

    open fun onEnable() {

    }

    open fun onDisable() {

    }
}