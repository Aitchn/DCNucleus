package io.aitchn.dcnucleus.api

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class DCNucleusPlugin {
    protected lateinit var name: String; private set
    protected lateinit var logger: Logger; private set
    private var isInjected: Boolean = false

    @Suppress("FunctionName")
    fun __inject(name: String) {
        if (isInjected) {
            throw IllegalStateException("Plugin has already been injected!")
        }

        this.name = name
        this.logger = LoggerFactory.getLogger("[$name]")
        this.isInjected = true
    }

    open fun onEnable() {

    }

    open fun onDisable() {

    }
}