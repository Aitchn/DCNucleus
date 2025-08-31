package io.aitchn.dcnucleus.api

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class DCNucleusPlugin {
    protected lateinit var name: String; private set
    protected lateinit var logger: Logger; private set

    internal fun __inject(name: String) {
        this.name = name
        this.logger = LoggerFactory.getLogger("[$name]")
    }

    open fun onEnable() {

    }

    open fun onDisable() {

    }
}